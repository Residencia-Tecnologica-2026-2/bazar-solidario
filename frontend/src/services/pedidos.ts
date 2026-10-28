import { api } from "./anuncios.ts";
export interface Pedido {
  id: number;
  estado: string;
  brecho: string;
  total: number;
  valorIsac: number;
  valorBrecho: number;
  criadoEm: string;
  itens: {
    anuncioId: number;
    titulo: string;
    quantidade: number;
    precoUnitario: number;
  }[];
}
export const criarPedido = (ids: number[], chave: string) =>
  api<Pedido>(
    "/pedidos",
    {
      method: "POST",
      headers: { "Content-Type": "application/json", "Idempotency-Key": chave },
      body: JSON.stringify({
        itens: ids.map((anuncioId) => ({ anuncioId, quantidade: 1 })),
      }),
    },
    "sessao",
  );
export const listarPedidos = (page = 0) =>
  api<{ content: Pedido[]; last: boolean }>(
    `/pedidos?page=${page}&size=12`,
    {},
    "sessao",
  );
export const mudarPedido = (id: number, acao: "confirmar" | "cancelar") =>
  api<Pedido>(`/pedidos/${id}/${acao}`, { method: "PATCH" }, "sessao");
