import { useRef, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { ArrowRight, CheckCircle2, Store, UserRound } from "./IsacIcons";
import { UsuarioService, type TipoConta } from "../services/usuarios";

const usuarios = new UsuarioService(
  import.meta.env.VITE_API_BASE_URL || "/api",
);

export function Cadastro() {
  const navigate = useNavigate();
  const [tipo, setTipo] = useState<TipoConta>("CLIENTE");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");
  const [nomeCriado, setNomeCriado] = useState("");
  const [visivel, setVisivel] = useState(false);
  const ocupado = useRef(false);

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (ocupado.current) return;
    const form = event.currentTarget;
    const dados = new FormData(form);
    const nome = String(dados.get("nome") || "").trim();
    const senha = String(dados.get("senha") || "");
    const whatsapp = String(dados.get("whatsapp") || "").trim();
    ocupado.current = true;
    setEnviando(true);
    setErro("");
    try {
      const usuario = await usuarios.cadastrar(
        {
          nome,
          email: String(dados.get("email") || ""),
          senha,
          tipo,
          whatsapp: tipo === "LOJA" ? whatsapp : undefined,
        },
        String(dados.get("confirmacao") || ""),
      );
      form.reset();
      setNomeCriado(usuario.nome);
      // Após 2 s redireciona ao perfil (sessão já foi salva no service)
      setTimeout(() => navigate("/perfil"), 2000);
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : "Não foi possível concluir seu cadastro.",
      );
    } finally {
      ocupado.current = false;
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="page-heading">
        <p className="eyebrow">UM NOVO CICLO COMEÇA AQUI</p>
        <h1>Prazer em conhecer você.</h1>
        <p>Crie seu cadastro e faça parte do Bazar Solidário.</p>
      </div>
      {nomeCriado ? (
        <div className="success" role="status">
          <CheckCircle2 size={48} />
          <h2>Bem-vindo, {nomeCriado}!</h2>
          <p>
            Cadastro criado com sucesso. Redirecionando para seu perfil…
          </p>
          <Link className="btn full" to="/perfil">
            Ir para o perfil <ArrowRight size={18} />
          </Link>
        </div>
      ) : (
        <form className="form-card" onSubmit={enviar} aria-busy={enviando}>
          <h2>Seu primeiro passo</h2>

          {/* ── seletor de tipo ── */}
          <div className="chips" role="group" aria-label="Tipo de conta">
            <button
              type="button"
              className={tipo === "CLIENTE" ? "active" : ""}
              onClick={() => setTipo("CLIENTE")}
            >
              <UserRound size={15} style={{ verticalAlign: "middle", marginRight: 5 }} />
              Usuário
            </button>
            <button
              type="button"
              className={tipo === "LOJA" ? "active" : ""}
              onClick={() => setTipo("LOJA")}
            >
              <Store size={15} style={{ verticalAlign: "middle", marginRight: 5 }} />
              Bazar / Brechó
            </button>
          </div>

          <fieldset disabled={enviando} className="signup-fields">
            <label>
              {tipo === "LOJA" ? "Nome do brechó" : "Nome completo"}
              <input
                name="nome"
                autoComplete={tipo === "LOJA" ? "organization" : "name"}
                required
                maxLength={100}
                placeholder={
                  tipo === "LOJA"
                    ? "Nome do seu brechó"
                    : "Como podemos chamar você?"
                }
              />
            </label>
            <label>
              E-mail
              <input
                name="email"
                type="email"
                autoComplete="email"
                required
                maxLength={255}
                placeholder="voce@exemplo.com"
              />
            </label>

            {/* ── campo exclusivo para LOJA ── */}
            {tipo === "LOJA" && (
              <label>
                WhatsApp
                <input
                  name="whatsapp"
                  type="tel"
                  autoComplete="tel"
                  required
                  minLength={10}
                  maxLength={14}
                  placeholder="5581999990000"
                  aria-describedby="wpp-ajuda"
                />
                <p className="field-help" id="wpp-ajuda">
                  DDI + DDD + número, apenas dígitos (10–13 dígitos).
                </p>
              </label>
            )}

            <label>
              Senha
              <span className="password-field">
                <input
                  name="senha"
                  type={visivel ? "text" : "password"}
                  autoComplete="new-password"
                  required
                  minLength={6}
                  maxLength={72}
                  aria-describedby="senha-ajuda"
                />
                <button
                  type="button"
                  onClick={() => setVisivel(!visivel)}
                  aria-label={visivel ? "Ocultar senha" : "Mostrar senha"}
                  aria-pressed={visivel}
                >
                  {visivel ? <EyeOff size={19} /> : <Eye size={19} />}
                </button>
              </span>
            </label>
            <p className="field-help" id="senha-ajuda">
              Use entre 6 e 72 caracteres.
            </p>
            <label>
              Confirmar senha
              <input
                name="confirmacao"
                type="password"
                autoComplete="new-password"
                required
                minLength={6}
                maxLength={72}
              />
            </label>
            <p className="body-copy">
              Ao enviar, seus dados serão encaminhados ao servidor do projeto.
              Consulte as{" "}
              <Link to="/privacidade">informações de privacidade</Link>.
            </p>
            {erro && (
              <p className="form-error" role="alert">
                {erro}
              </p>
            )}
            <button className="btn full" type="submit">
              {enviando ? "Criando cadastro…" : "Criar cadastro"}
              <ArrowRight size={18} />
            </button>
            <p style={{ textAlign: "center", marginTop: 14, fontSize: 13 }}>
              Já tem conta?{" "}
              <Link className="text-button" to="/login">
                Fazer login
              </Link>
            </p>
          </fieldset>
        </form>
      )}
    </>
  );
}

