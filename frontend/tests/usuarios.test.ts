import { test } from "node:test";
import assert from "node:assert/strict";
import { criarClienteUsuarios } from "../src/services/usuarios.ts";

test("cadastro respeita o contrato existente e descarta a senha da resposta", async () => {
  const cliente = criarClienteUsuarios("/api/", async (url, init) => {
    assert.equal(url, "/api/usuarios");
    assert.equal(init?.method, "POST");
    assert.deepEqual(JSON.parse(String(init?.body)), {
      nome: "Ana",
      email: "ana@example.com",
      senha: "exemplo123",
      whatsapp: "5581999999999",
    }, { status: 201 });
    return Response.json({
      id: 1,
      nome: "Ana",
      email: "ana@example.com",
      role: "LOJA",
      senha: "exemplo123",
      whatsapp: "5581999999999",
    });
  });
  assert.deepEqual(
    await cliente({
      nome: " Ana ",
      email: " ana@example.com ",
      senha: "exemplo123",
      whatsapp: "5581999999999",
    }),
    {
      id: 1,
      nome: "Ana",
      email: "ana@example.com",
      role: "LOJA",
    },
  );
});

test("falha de rede produz mensagem recuperável", async () => {
  const cliente = criarClienteUsuarios("/api", async () => {
    throw new TypeError("Failed to fetch");
  });
  await assert.rejects(
    cliente({ nome: "Ana", email: "ana@example.com", senha: "exemplo123", whatsapp: "5581999999999" }),
    /conectar ao servidor/,
  );
});

for (const [status, mensagem] of [
  [400, /Confira os dados/],
  [409, /já está cadastrado/],
  [500, /mais tarde/],
  [502, /indisponível/],
] as const) {
  test(`erro HTTP ${status} não expõe detalhes internos do backend`, async () => {
    const cliente = criarClienteUsuarios(
      "/api",
      async () => new Response("stacktrace interno", { status }),
    );
    await assert.rejects(
      cliente({ nome: "Ana", email: "ana@example.com", senha: "exemplo123", whatsapp: "5581999999999" }),
      mensagem,
    );
  });
}

test("resposta inválida não apresenta falso sucesso", async () => {
  for (const resposta of [
    new Response("<html>erro</html>"),
    Response.json({ nome: "Ana" }),
    Response.json(null),
  ]) {
    const cliente = criarClienteUsuarios("/api", async () => resposta);
    await assert.rejects(
      cliente({ nome: "Ana", email: "ana@example.com", senha: "exemplo123", whatsapp: "5581999999999" }),
      /resposta inesperada/,
    );
  }
});


test("cadastro aceita AuthResponse atual sem expor o token", async () => {
  const cliente = criarClienteUsuarios("/api", async () => Response.json({
    token: "token-de-teste", tipo: "Bearer", expiraEmMs: 86400000,
    usuario: { id: 7, nome: "Ana", email: "ana@example.com", role: "LOJA" },
  }, { status: 201 }));
  const resultado = await cliente({ nome: "Ana", email: "ana@example.com", senha: "123456", whatsapp: "5581999999999" });
  assert.deepEqual(resultado, { id: 7, nome: "Ana", email: "ana@example.com", role: "LOJA" });
  assert.ok(!("token" in resultado));
});

test("cadastro rejeita AuthResponse com usuário incompleto", async () => {
  const cliente = criarClienteUsuarios("/api", async () => Response.json({
    token: "token-de-teste", usuario: { id: 7 },
  }));
  await assert.rejects(cliente({ nome: "Ana", email: "ana@example.com", senha: "123456", whatsapp: "5581999999999" }), /resposta inesperada/);
});
