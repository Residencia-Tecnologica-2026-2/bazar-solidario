import { test, afterEach } from "node:test";
import assert from "node:assert/strict";
import { api, salvarAnuncio, enviarFoto, fotoAnuncio, AnuncioSemelhanteErro } from "../src/services/anuncios.ts";
const originalFetch=globalThis.fetch;
const originalStorage=Object.getOwnPropertyDescriptor(globalThis,"localStorage");
function sessao(role="LOJA") {Object.defineProperty(globalThis,"localStorage",{configurable:true,value:{getItem:()=>JSON.stringify({token:"teste-token",expiraEm:Date.now()+60000,usuario:{id:9,nome:"Loja",email:"loja@example.test",role}}),removeItem:()=>{}}});}
afterEach(()=>{globalThis.fetch=originalFetch;if(originalStorage)Object.defineProperty(globalThis,"localStorage",originalStorage);else Reflect.deleteProperty(globalThis,"localStorage");});
test("publicar usa token e não envia identificador de dono",async()=>{sessao();globalThis.fetch=async(url,init)=>{assert.equal(url,"/api/anuncios");assert.equal(new Headers(init?.headers).get("Authorization"),"Bearer teste-token");const body=JSON.parse(String(init?.body));assert.equal(body.titulo,"Camisa");assert.equal(body.vendedor,undefined);assert.equal(body.lojaId,undefined);return Response.json({id:1});};await salvarAnuncio({titulo:"Camisa",descricao:"Linho",preco:25,estoque:1,categoriaId:1,imagens:["/uploads/foto.jpg"]});});
test("conta CLIENTE não envia requisição de gestão",async()=>{sessao("CLIENTE");globalThis.fetch=async()=>{assert.fail("Não deve acessar a rede");};await assert.rejects(api("/anuncios",{},true),/exclusiva/);});
test("upload envia multipart e deixa o navegador definir o boundary",async()=>{sessao();globalThis.fetch=async(url,init)=>{assert.equal(url,"/api/uploads");assert.ok(init?.body instanceof FormData);assert.equal(new Headers(init?.headers).has("Content-Type"),false);return Response.json({url:"/uploads/teste.png"});};assert.equal(await enviarFoto(new File(["foto"],"foto.png",{type:"image/png"})),"/uploads/teste.png");});
test("arquivo inválido é recusado antes do upload",async()=>{await assert.rejects(enviarFoto(new File(["x"],"x.txt",{type:"text/plain"})),/JPG/);});
test("imagem local usa o proxy e erro de permissão é comunicado",async()=>{assert.equal(fotoAnuncio("/uploads/foto.png"),"/api/uploads/foto.png");sessao();globalThis.fetch=async()=>new Response(null,{status:403});await assert.rejects(api("/anuncios/1",{},true),/permissão/);});
test("publicação envia chave estável e código de estoque ao servidor",async()=>{
  sessao();globalThis.fetch=async(url,init)=>{
    assert.equal(new Headers(init?.headers).get("Idempotency-Key"),"publicacao-estavel-1234");
    assert.equal(JSON.parse(String(init?.body)).codigoPeca,"CAM-001");return Response.json({id:1});
  };
  await salvarAnuncio({titulo:"Camisa",descricao:"Linho",preco:25,estoque:1,categoriaId:1,imagens:[],codigoPeca:"CAM-001"},undefined,"publicacao-estavel-1234");
});
test("aviso de semelhantes preserva candidatos para revisão sem tratar como sucesso",async()=>{
  sessao();globalThis.fetch=async()=>Response.json({codigo:"ANUNCIO_SEMELHANTE",erro:"Confira a peça",semelhantes:[{id:7,titulo:"Camisa",codigoPeca:"CAM-001",estado:"ATIVO",motivos:["Mesma foto"]}]},{status:409});
  await assert.rejects(api("/anuncios",{method:"POST"},true),(e: unknown)=>e instanceof AnuncioSemelhanteErro && e.semelhantes[0].id===7);
});
