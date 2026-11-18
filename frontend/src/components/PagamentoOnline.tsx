import { useEffect, useState } from "react";
import { configuracaoPagamento, iniciarPagamento, consultarPagamento, type PagamentoOnline as DadosPagamento } from "../services/pedidos";

const estados: Record<string, string> = {
  RECONCILIAR: "Verificando criação da cobrança",
  AGUARDANDO_PAGAMENTO: "Aguardando pagamento de teste",
  CONFIRMADO_PROVEDOR: "Confirmado pelo provedor; aguardando recebimento",
  PAGO: "Pagamento recebido no Sandbox",
  VENCIDO: "Cobrança vencida",
  ESTORNADO: "Pagamento estornado",
  ESTORNO_EM_PROCESSAMENTO: "Estorno em processamento",
  CONTESTADO: "Pagamento contestado",
  EXCLUIDO_PROVEDOR: "Cobrança excluída no provedor",
  EM_ANALISE: "Pagamento em análise",
};
const dinheiro = (n: number) => n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

export function PagamentoOnline({ pedidoId, inicial, cliente, onVinculado }: {
  pedidoId: number; inicial?: DadosPagamento | null; cliente: boolean; onVinculado?: () => void;
}) {
  const [habilitado, setHabilitado] = useState<boolean | null>(null);
  const [pagamento, setPagamento] = useState(inicial ?? null);
  const [cpf, setCpf] = useState("");
  const [erro, setErro] = useState("");
  const [ocupado, setOcupado] = useState(false);
  useEffect(() => {
    let ativo = true;
    configuracaoPagamento().then(c => { if (ativo) setHabilitado(c.habilitado); })
      .catch(() => { if (ativo) setErro("Não foi possível consultar a configuração do pagamento."); });
    return () => { ativo = false; };
  }, []);
  async function executar(iniciar: boolean) {
    setOcupado(true); setErro("");
    try {
      const p = iniciar ? await iniciarPagamento(pedidoId, cpf) : await consultarPagamento(pedidoId);
      setPagamento(p); setCpf(""); onVinculado?.();
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível consultar o pagamento."); }
    finally { setOcupado(false); }
  }
  return <section className="info-box" aria-label="Pagamento de teste">
    <div>
      <h3>Pagamento · ambiente de testes</h3>
      <p>Nenhum dinheiro real será movimentado. Não pague com seu aplicativo bancário.</p>
      {pagamento ? <>
        <p role="status"><strong>{estados[pagamento.estado] ?? "Verificando pagamento"}</strong></p>
        {pagamento.checkoutUrl && pagamento.estado === "AGUARDANDO_PAGAMENTO" && cliente &&
          <a className="btn" href={pagamento.checkoutUrl} target="_blank" rel="noopener noreferrer">Abrir checkout de teste</a>}
        <button type="button" className="text-button" disabled={ocupado} onClick={() => executar(false)}>
          {ocupado ? "Consultando…" : "Consultar confirmação no Asaas"}
        </button>
        <p>Comissão solicitada ao ISAC: {dinheiro(pagamento.comissaoIsac)}.</p>
        {pagamento.tarifa !== null && <p>Tarifa do provedor: {dinheiro(pagamento.tarifa)}.</p>}
        {pagamento.liquidoBrecho !== null && <p>Valor previsto para o brechó após tarifa: {dinheiro(pagamento.liquidoBrecho)}.</p>}
        {pagamento.aviso && <p>{pagamento.aviso}</p>}
        <p>Para cancelar uma cobrança vinculada, solicite conciliação à equipe. O estoque permanece reservado.</p>
      </> : <>
        {habilitado === null && !erro && <p role="status">Verificando disponibilidade…</p>}
        {habilitado === false && <p>Pagamento online ainda indisponível: falta concluir a vinculação das carteiras de teste.</p>}
        {habilitado && cliente && <>
          <label htmlFor={`cpf-pagador-${pedidoId}`}>CPF do pagador de teste</label>
          <input id={`cpf-pagador-${pedidoId}`} value={cpf} inputMode="numeric" autoComplete="off" maxLength={18}
            onChange={e => setCpf(e.target.value)} aria-describedby={`aviso-pagador-${pedidoId}`} />
          <p id={`aviso-pagador-${pedidoId}`}>Use apenas dados fictícios próprios para testes. O CPF será enviado ao Asaas Sandbox para criar o pagador.</p>
          <button className="btn" type="button" disabled={ocupado || cpf.replace(/\D/g, "").length !== 11}
            onClick={() => executar(true)}>{ocupado ? "Preparando…" : "Gerar pagamento Pix de teste"}</button>
        </>}
        {habilitado && !cliente && <p>O cliente pode iniciar o pagamento na área de pedidos.</p>}
      </>}
      {erro && <p role="alert">{erro}</p>}
    </div>
  </section>;
}
