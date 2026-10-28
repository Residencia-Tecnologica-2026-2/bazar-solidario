import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import {
  CreditCard,
  Smartphone,
} from "lucide-react";
import { ArrowRight, CheckCircle2, Info, HandHeart, Store } from "./IsacIcons";

interface PagamentoProps {
  subtotal: number;
  onSucesso: () => void;
}

type MetodoPagamento = "cartao" | "pix";

const money = (n: number) =>
  n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

function formatarNumeroCartao(valor: string): string {
  return valor
    .replace(/\D/g, "")
    .slice(0, 16)
    .replace(/(.{4})/g, "$1 ")
    .trim();
}

function formatarValidade(valor: string): string {
  return valor
    .replace(/\D/g, "")
    .slice(0, 4)
    .replace(/(\d{2})(\d{1,2})/, "$1/$2");
}

export function Pagamento({ subtotal, onSucesso }: PagamentoProps) {
  const [metodo, setMetodo] = useState<MetodoPagamento>("cartao");
  const [numeroCartao, setNumeroCartao] = useState("");
  const [nomeCartao, setNomeCartao] = useState("");
  const [validade, setValidade] = useState("");
  const [cvv, setCvv] = useState("");
  const [concluido, setConcluido] = useState(false);
  const [protocolo] = useState(
    () => "BSZ-" + Math.random().toString(36).slice(2, 8).toUpperCase(),
  );

  const valorBrecho = subtotal * 0.9;
  const valorOng = subtotal * 0.1;

  function confirmar(e: FormEvent) {
    e.preventDefault();
    setConcluido(true);
    onSucesso();
  }

  if (concluido) {
    return (
      <div className="success" role="status">
        <CheckCircle2 size={48} />
        <h2>Simulação concluída!</h2>
        <p style={{ fontWeight: 600, fontSize: 13, letterSpacing: 1.2, color: "var(--color-text-secondary)" }}>
          PROTOCOLO: {protocolo}
        </p>
        <p>
          Nenhum pagamento foi processado. Nenhuma peça foi reservada. Esta é
          uma demonstração do fluxo de pagamento.
        </p>
        <div
          style={{
            background: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: 14,
            padding: "16px 18px",
            width: "100%",
            marginTop: 4,
          }}
        >
          <p style={{ fontSize: 13, marginBottom: 8, color: "var(--color-text-secondary)" }}>
            Divisão simulada do valor
          </p>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
            <span style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 13 }}>
              <Store size={14} /> Brechó (90%)
            </span>
            <strong>{money(valorBrecho)}</strong>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <span style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 13 }}>
              <HandHeart size={14} /> ONG ISAC (10%)
            </span>
            <strong>{money(valorOng)}</strong>
          </div>
        </div>
        <Link to="/catalogo" className="btn full">
          Voltar aos achados <ArrowRight size={18} />
        </Link>
      </div>
    );
  }

  return (
    <form className="form-card" onSubmit={confirmar}>
      {/* ── Aviso de demonstração ── */}
      <div
        className="info-box"
        style={{
          background: "var(--color-amber-tint, #fff8e1)",
          borderColor: "var(--color-amber, #f59e0b)",
          gap: 10,
        }}
      >
        <Info size={20} style={{ flexShrink: 0, color: "var(--color-amber, #d97706)" }} />
        <p style={{ fontWeight: 600 }}>
          Esta é uma demonstração. Nenhum pagamento será processado. O sistema
          de pagamento real será integrado em produção.
        </p>
      </div>

      {/* ── Resumo do pedido ── */}
      <h2>Resumo do pedido</h2>
      <div
        style={{
          background: "var(--color-surface)",
          border: "1px solid var(--color-border)",
          borderRadius: 14,
          padding: "16px 18px",
          marginBottom: 4,
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 10 }}>
          <span>Total</span>
          <strong style={{ fontSize: 18 }}>{money(subtotal)}</strong>
        </div>
        <div
          style={{
            borderTop: "1px solid var(--color-border)",
            paddingTop: 10,
            display: "flex",
            flexDirection: "column",
            gap: 7,
          }}
        >
          <p style={{ fontSize: 11, letterSpacing: 1.4, fontWeight: 700, color: "var(--color-text-secondary)", marginBottom: 2 }}>
            IMPACTO SOCIAL
          </p>
          <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13 }}>
            <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <Store size={13} /> Brechó parceiro (90%)
            </span>
            <span>{money(valorBrecho)}</span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13 }}>
            <span style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <HandHeart size={13} /> ONG ISAC (10%)
            </span>
            <span>{money(valorOng)}</span>
          </div>
        </div>
      </div>

      {/* ── Seletor de método ── */}
      <h2>Forma de pagamento</h2>
      <div className="chips" role="group" aria-label="Método de pagamento">
        <button
          type="button"
          className={metodo === "cartao" ? "active" : ""}
          onClick={() => setMetodo("cartao")}
        >
          <CreditCard size={14} style={{ verticalAlign: "middle", marginRight: 5 }} />
          Cartão de crédito
        </button>
        <button
          type="button"
          className={metodo === "pix" ? "active" : ""}
          onClick={() => setMetodo("pix")}
        >
          <Smartphone size={14} style={{ verticalAlign: "middle", marginRight: 5 }} />
          PIX
        </button>
      </div>

      {/* ── Formulário cartão ── */}
      {metodo === "cartao" && (
        <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
          <label>
            Número do cartão
            <input
              type="text"
              inputMode="numeric"
              autoComplete="cc-number"
              required
              placeholder="0000 0000 0000 0000"
              value={numeroCartao}
              onChange={(e) => setNumeroCartao(formatarNumeroCartao(e.target.value))}
              maxLength={19}
            />
          </label>
          <label>
            Nome no cartão
            <input
              type="text"
              autoComplete="cc-name"
              required
              placeholder="Como aparece no cartão"
              value={nomeCartao}
              onChange={(e) => setNomeCartao(e.target.value.toUpperCase())}
              maxLength={60}
            />
          </label>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
            <label>
              Validade
              <input
                type="text"
                inputMode="numeric"
                autoComplete="cc-exp"
                required
                placeholder="MM/AA"
                value={validade}
                onChange={(e) => setValidade(formatarValidade(e.target.value))}
                maxLength={5}
              />
            </label>
            <label>
              CVV
              <input
                type="text"
                inputMode="numeric"
                autoComplete="cc-csc"
                required
                placeholder="123"
                value={cvv}
                onChange={(e) => setCvv(e.target.value.replace(/\D/g, "").slice(0, 4))}
                maxLength={4}
              />
            </label>
          </div>
        </div>
      )}

      {/* ── Instruções PIX ── */}
      {metodo === "pix" && (
        <div
          style={{
            background: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: 14,
            padding: 20,
            textAlign: "center",
          }}
        >
          <div
            style={{
              width: 120,
              height: 120,
              margin: "0 auto 14px",
              background: "var(--color-stage)",
              border: "1px dashed var(--color-border)",
              borderRadius: 12,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexDirection: "column",
              gap: 4,
            }}
          >
            <Smartphone size={32} strokeWidth={1.2} />
            <p style={{ fontSize: 9, letterSpacing: 1.5, fontWeight: 700, color: "var(--color-text-secondary)" }}>
              QR DEMO
            </p>
          </div>
          <p style={{ fontSize: 13, color: "var(--color-text-secondary)" }}>
            Na versão real, um QR Code PIX seria exibido aqui.<br />
            Toque em "Confirmar simulação" para continuar.
          </p>
        </div>
      )}

      <button className="btn full" type="submit" style={{ marginTop: 8 }}>
        Confirmar simulação <CheckCircle2 size={18} />
      </button>
      <p className="demo-note" style={{ marginTop: 8 }}>
        Demonstração sem pagamento real · dados não são armazenados
      </p>
    </form>
  );
}
