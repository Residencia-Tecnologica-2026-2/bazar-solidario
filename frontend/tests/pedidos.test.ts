import { test, afterEach } from "node:test";
import assert from "node:assert/strict";
import {
  criarPedido,
  listarPedidos,
  mudarPedido,
} from "../src/services/pedidos.ts";
import { obterSessao } from "../src/services/usuarios.ts";
const originalFetch = globalThis.fetch;
const originalStorage = Object.getOwnPropertyDescriptor(
  globalThis,
  "localStorage",
);
let raw: string | null;
function sessao(expiraEm = Date.now() + 60000) {
  raw = JSON.stringify({
    token: "token-cliente",
    expiraEm,
    usuario: { id: 2, nome: "Cliente", role: "CLIENTE" },
  });
  Object.defineProperty(globalThis, "localStorage", {
    configurable: true,
    value: {
      getItem: () => raw,
      removeItem: () => {
        raw = null;
      },
    },
  });
}
afterEach(() => {
  globalThis.fetch = originalFetch;
  if (originalStorage)
    Object.defineProperty(globalThis, "localStorage", originalStorage);
  else Reflect.deleteProperty(globalThis, "localStorage");
});
test("pedido envia apenas peças, quantidades, token e chave de repetição", async () => {
  sessao();
  globalThis.fetch = async (url, init) => {
    assert.equal(url, "/api/pedidos");
    const h = new Headers(init?.headers);
    assert.equal(h.get("Authorization"), "Bearer token-cliente");
    assert.equal(h.get("Idempotency-Key"), "chave-fixa");
    assert.deepEqual(JSON.parse(String(init?.body)), {
      itens: [{ anuncioId: 42, quantidade: 1 }],
    });
    return Response.json({ id: 9 });
  };
  await criarPedido([42], "chave-fixa");
});
test("sessão expirada impede pedido antes da chamada HTTP", async () => {
  sessao(Date.now() - 1);
  globalThis.fetch = async () => {
    assert.fail("não deve chamar rede");
  };
  await assert.rejects(criarPedido([1], "chave"), /Entre novamente/);
  assert.equal(obterSessao(), null);
});
test("401 limpa sessão e comunica a necessidade de novo login", async () => {
  sessao();
  globalThis.fetch = async () => new Response(null, { status: 401 });
  await assert.rejects(listarPedidos(), /sessão expirou/);
  assert.equal(raw, null);
});
test("conflito de estoque mostra a regra retornada pelo servidor", async () => {
  sessao();
  globalThis.fetch = async () =>
    Response.json({ erro: "Peça indisponível: camisa" }, { status: 409 });
  await assert.rejects(criarPedido([1], "chave"), /Peça indisponível/);
});
test("cancelamento usa rota autenticada sem mandar preço ou dono", async () => {
  sessao();
  globalThis.fetch = async (url, init) => {
    assert.equal(url, "/api/pedidos/9/cancelar");
    assert.equal(init?.method, "PATCH");
    assert.equal(init?.body, undefined);
    return Response.json({ id: 9, estado: "CANCELADO" });
  };
  await mudarPedido(9, "cancelar");
});
