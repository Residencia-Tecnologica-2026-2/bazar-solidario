import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { UsuarioService } from "../src/services/usuarios.ts";

const dados = { nome: "Ana", email: "ana@example.com", senha: "exemplo123" };
/** Classe de teste: cada cenário usa uma instância independente. */
export class UsuarioServiceTest {
  async ["valida campos antes de fazer qualquer requisição"]() {
    let chamadas = 0;
    const service = new UsuarioService("/api", async () => { chamadas++; return Response.json({}); });
    for (const invalido of [
      { ...dados, nome: "  " }, { ...dados, nome: "a".repeat(101) },
      { ...dados, email: "invalido" }, { ...dados, email: "" },
      { ...dados, email: "a".repeat(250) + "@x.com" },
      { ...dados, senha: "12345" }, { ...dados, senha: "a".repeat(73) },
      { ...dados, senha: "        " },
    ]) await assert.rejects(service.cadastrar(invalido));
    assert.equal(chamadas, 0);
  }
  async ["rejeita confirmação diferente sem enviar dados"]() {
    const service = new UsuarioService("/api", async () => { assert.fail("Não deve acessar a rede"); });
    await assert.rejects(service.cadastrar(dados, "diferente"), /precisam ser iguais/);
  }
  async ["aceita limites de senha e nome e retorna apenas dados públicos"]() {
    for (const tamanho of [6, 72]) {
      const service = new UsuarioService("/api", async () => Response.json({ token: "token-teste", expiraEmMs: 86400000, usuario: { id: 1, nome: "Ana", email: dados.email, role: "CLIENTE" } }));
      const result = await service.cadastrar({ ...dados, nome: "a".repeat(100), senha: "a".repeat(tamanho) });
      assert.equal(result.id, 1);
      assert.ok(!("senha" in result));
    }
  }
  async ["rejeita identificador inválido e perfil inesperado"]() {
    for (const response of [{ id: -1, role: "CLIENTE" }, { id: 1.5, role: "CLIENTE" }, { id: 1, role: "ADMIN" }]) {
      const service = new UsuarioService("/api", async () => Response.json({ ...dados, ...response }));
      await assert.rejects(service.cadastrar(dados), /resposta inesperada/);
    }
  }
  ["trata HTTP 422"]() {
    const service = new UsuarioService("/api", async () => new Response("interno", { status: 422 }));
    return assert.rejects(service.cadastrar(dados), /Confira/);
  }
  ["trata HTTP 503"]() {
    const service = new UsuarioService("/api", async () => new Response("interno", { status: 503 }));
    return assert.rejects(service.cadastrar(dados), /indisponível/);
  }
  ["trata HTTP 504"]() {
    const service = new UsuarioService("/api", async () => new Response("interno", { status: 504 }));
    return assert.rejects(service.cadastrar(dados), /indisponível/);
  }
}

describe("UsuarioService — cadastro integrado ao formulário", () => {
  for (const nome of Object.getOwnPropertyNames(UsuarioServiceTest.prototype)) {
    if (nome === "constructor") continue;
    it(nome, () => {
      const caso = new UsuarioServiceTest();
      return Reflect.apply(Reflect.get(caso, nome), caso, []);
    });
  }
});

