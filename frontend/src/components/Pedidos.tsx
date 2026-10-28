import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarPedidos, mudarPedido, type Pedido } from "../services/pedidos";
import { obterSessao } from "../services/usuarios";
const dinheiro = (n: number) =>
  n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const estados: Record<string, string> = {
  AGUARDANDO_CONFIRMACAO_MANUAL: "Aguardando confirmação manual",
  CONFIRMADO: "Aceito pelo brechó",
  CANCELADO: "Cancelado",
};
export function Pedidos() {
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [erro, setErro] = useState("");
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState<number | null>(null);
  const sessao = obterSessao();
  const loja = sessao?.usuario.role === "LOJA";
  useEffect(() => {
    let ativo = true;
    setLoading(true);
    listarPedidos(page)
      .then((p) => {
        if (ativo) {
          setPedidos(p.content);
          setLast(p.last);
          setErro("");
        }
      })
      .catch((e) => {
        if (ativo) setErro(e.message);
      })
      .finally(() => {
        if (ativo) setLoading(false);
      });
    return () => {
      ativo = false;
    };
  }, [page]);
  if (!sessao)
    return (
      <p>
        Entre para acompanhar seus pedidos. <Link to="/login">Fazer login</Link>
      </p>
    );
  async function atualizar(id: number, acao: "confirmar" | "cancelar") {
    setBusy(id);
    setErro("");
    try {
      const p = await mudarPedido(id, acao);
      setPedidos((v) => v.map((a) => (a.id === id ? p : a)));
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setBusy(null);
    }
  }
  return (
    <section className="merchant-page">
      <h1>{loja ? "Pedidos recebidos" : "Meus pedidos"}</h1>
      <p>
        O aceite da loja não comprova pagamento. Valores e repasses são
        conciliados manualmente.
      </p>
      {erro && <p role="alert">{erro}</p>}
      {loading && <p role="status">Carregando pedidos…</p>}
      {!loading && !erro && !pedidos.length && <p>Nenhum pedido registrado.</p>}
      {pedidos.map((p) => (
        <article className="form-card" key={p.id}>
          <h2>
            Pedido #{p.id} · {p.brecho}
          </h2>
          <p>
            {estados[p.estado]} · {new Date(p.criadoEm).toLocaleString("pt-BR")}
          </p>
          <ul>
            {p.itens.map((i) => (
              <li key={i.anuncioId}>
                {i.quantidade} × {i.titulo} · {dinheiro(i.precoUnitario)}
              </li>
            ))}
          </ul>
          <strong>Total: {dinheiro(p.total)}</strong>
          {p.estado === "CANCELADO" && (
            <p>
              Pedido cancelado: a divisão abaixo é apenas o registro original,
              sem repasse devido.
            </p>
          )}
          <p>
            ISAC (10%): {dinheiro(p.valorIsac)} · Brechó (90%):{" "}
            {dinheiro(p.valorBrecho)}
          </p>
          {p.estado === "AGUARDANDO_CONFIRMACAO_MANUAL" && loja && (
            <button
              className="btn"
              disabled={busy !== null}
              onClick={() => atualizar(p.id, "confirmar")}
            >
              Aceitar pedido
            </button>
          )}
          {p.estado !== "CANCELADO" &&
            (loja || p.estado === "AGUARDANDO_CONFIRMACAO_MANUAL") && (
              <button
                className="text-button"
                disabled={busy !== null}
                onClick={() => atualizar(p.id, "cancelar")}
              >
                Cancelar e devolver estoque
              </button>
            )}
        </article>
      ))}
      <div className="pagination">
        <button disabled={!page || loading} onClick={() => setPage(page - 1)}>
          Anterior
        </button>
        <span>Página {page + 1}</span>
        <button disabled={last || loading} onClick={() => setPage(page + 1)}>
          Próxima
        </button>
      </div>
    </section>
  );
}
