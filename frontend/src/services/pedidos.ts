import { api } from "./anuncios.ts";
export interface Pedido {
  id: number;
  estado: string;
  brecho: string;
  total: number;
  valorIsac: number;
  valorBrecho: number;
  criadoEm: string;
  pagamento?: PagamentoOnline | null;
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

export interface PagamentoOnline {
  pedidoId: number;
  estado: string;
  checkoutUrl: string | null;
  aviso: string | null;
  total: number;
  comissaoIsac: number;
  tarifa: number | null;
  liquidoBrecho: number | null;
  ambiente: "SANDBOX";
}
export const configuracaoPagamento = () =>
  api<{ habilitado: boolean; ambiente: string }>("/pagamentos/configuracao");
export const iniciarPagamento = (id: number, cpf: string) =>
  api<PagamentoOnline>(`/pedidos/${id}/pagamento`, {
    method: "POST", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cpf }),
  }, "sessao");
export const consultarPagamento = (id: number) =>
  api<PagamentoOnline>(`/pedidos/${id}/pagamento`, {}, "sessao");
