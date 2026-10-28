import { useState } from "react";
import { Link } from "react-router-dom";
import { obterSessao } from "../services/usuarios";
import { criarPedido, type Pedido } from "../services/pedidos";
import { CheckCircle2, HandHeart } from "./IsacIcons";
const money = (n: number) =>
  n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
export function Pagamento({
  subtotal,
  ids,
  onSucesso,
}: {
  subtotal: number;
  ids: number[];
  onSucesso: () => void;
}) {
  const [pedido, setPedido] = useState<Pedido | null>(null);
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [chave] = useState(() => {
    const fingerprint = [...ids].sort((a, b) => a - b).join(",");
    const key = "bs-checkout-" + obterSessao()?.usuario.id;
    try {
      const old = JSON.parse(sessionStorage.getItem(key) || "null");
      if (old?.fingerprint === fingerprint) return old.chave;
      const chave = crypto.randomUUID();
      sessionStorage.setItem(key, JSON.stringify({ fingerprint, chave }));
      return chave;
    } catch {
      return crypto.randomUUID();
    }
  });
  if (pedido)
    return (
      <div className="success" role="status">
        <CheckCircle2 size={40} />
        <h2>Pedido #{pedido.id} registrado</h2>
        <p>
          Aguardando confirmação manual do brechó. Nenhum pagamento foi
          processado.
        </p>
        <p>
          Total confirmado pelo servidor: <strong>{money(pedido.total)}</strong>
        </p>
        <p>
          ISAC (10%): {money(pedido.valorIsac)} · Brechó (90%):{" "}
          {money(pedido.valorBrecho)}
        </p>
        <Link className="btn full" to="/pedidos" onClick={onSucesso}>
          Acompanhar pedido
        </Link>
        <button className="text-button" onClick={onSucesso}>
          Limpar sacola
        </button>
      </div>
    );
  const sessao = obterSessao();
  if (!sessao)
    return (
      <div className="info-box">
        <p>Entre como cliente para registrar seu pedido.</p>
        <Link to="/login">Fazer login</Link>
      </div>
    );
  if (sessao.usuario.role !== "CLIENTE")
    return (
      <p className="info-box">
        A conta de brechó gerencia anúncios e pedidos recebidos. Para comprar,
        use uma conta de cliente.
      </p>
    );
  return (
    <form
      className="form-card"
      onSubmit={async (e) => {
        e.preventDefault();
        if (enviando) return;
        setErro("");
        setEnviando(true);
        try {
          setPedido(await criarPedido(ids, chave));
        } catch (e) {
          setErro(
            e instanceof Error
              ? e.message
              : "Não foi possível registrar o pedido.",
          );
        } finally {
          setEnviando(false);
        }
      }}
    >
      <h2>Revisar e solicitar</h2>
      <p>
        Estimativa da sacola: <strong>{money(subtotal)}</strong>. O servidor
        verifica preço e estoque no momento da confirmação.
      </p>
      <p>
        Um pedido por brechó. As peças ficam reservadas até a loja confirmar ou
        o pedido ser cancelado.
      </p>
      <div className="info-box">
        <HandHeart size={24} />
        <p>
          10% do valor dos produtos é destinado à ISAC e 90% ao brechó. O
          sistema registra a divisão; o repasse será conciliado manualmente pela
          equipe.
        </p>
      </div>
      <p>
        Não há cobrança online, frete calculado ou pagamento automático neste
        MVP. Retirada, entrega e pagamento serão combinados com a loja após o
        aceite.
      </p>
      {erro && <p role="alert">{erro}</p>}
      <button className="btn full" disabled={enviando}>
        {enviando ? "Registrando…" : "Registrar pedido sem pagamento online"}
      </button>
    </form>
  );
}
