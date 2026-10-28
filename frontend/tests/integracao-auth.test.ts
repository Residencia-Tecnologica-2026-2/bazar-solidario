import { test } from "node:test";
import assert from "node:assert/strict";
import { UsuarioService } from "../src/services/usuarios.ts";

test("fetch nativo recebe o contexto global, não a instância do serviço", async () => {
  const original = globalThis.fetch;
  globalThis.fetch = async function (this: unknown, url, init) {
    assert.equal(this, globalThis, "fetch com contexto incorreto falha antes de enviar HTTP");
    assert.equal(url, "/api/auth/login");
    assert.equal(init?.method, "POST");
    return Response.json({token: "token", expiraEmMs: 60000, usuario: {id: 1, nome: "Teste", email: "teste@example.test", role: "CLIENTE"}});
  };
  try { await new UsuarioService("/api").login({email:"teste@example.test",senha:"senha123"}); }
  finally { globalThis.fetch = original; }
});

test("cadastro de loja envia WhatsApp normalizado e tipo correto", async () => {
  const service = new UsuarioService("/api", async (_, init) => {
    assert.deepEqual(JSON.parse(String(init?.body)), {nome:"Loja",email:"loja@example.test",senha:"senha123",tipo:"LOJA",whatsapp:"5581999999999"});
    return Response.json({token:"token",usuario:{id:2,nome:"Loja",email:"loja@example.test",role:"LOJA"}});
  });
  assert.equal((await service.cadastrar({nome:"Loja",email:"loja@example.test",senha:"senha123",tipo:"LOJA",whatsapp:"+55 (81) 99999-9999"})).role,"LOJA");
});
