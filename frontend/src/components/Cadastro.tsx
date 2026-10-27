import { useRef, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, CheckCircle2, Eye, EyeOff } from "lucide-react";
import { UsuarioService } from "../services/usuarios";

const usuarios = new UsuarioService(
  import.meta.env.VITE_API_BASE_URL || "/api",
);

export function Cadastro() {
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
      const usuario = await usuarios.cadastrar({
        nome,
        email: String(dados.get("email") || ""),
        senha,
        whatsapp,
      }, String(dados.get("confirmacao") || ""));
      form.reset();
      setNomeCriado(usuario.nome);
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
        <p>Cadastre seu brechó e faça parte do Bazar Solidário.</p>
      </div>
      {nomeCriado ? (
        <div className="success" role="status">
          <CheckCircle2 size={48} />
          <h2>Bem-vindo, {nomeCriado}!</h2>
          <p>
            Seu cadastro foi salvo no servidor. Continue descobrindo peças com
            propósito.
          </p>
          <p>
            O acesso à conta será disponibilizado quando o login estiver
            integrado.
          </p>
          <Link className="btn full" to="/catalogo">
            Explorar achados <ArrowRight size={18} />
          </Link>
        </div>
      ) : (
        <form className="form-card" onSubmit={enviar} aria-busy={enviando}>
          <h2>Seu primeiro passo</h2>
          <fieldset disabled={enviando} className="signup-fields">
            <label>
              Nome completo
              <input
                name="nome"
                autoComplete="name"
                required
                maxLength={100}
                placeholder="Como podemos chamar você?"
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
            <label>
              WhatsApp com DDI e DDD
              <input
                name="whatsapp"
                type="tel"
                inputMode="numeric"
                autoComplete="tel"
                required
                minLength={10}
                maxLength={13}
                pattern="[0-9]{10,13}"
                placeholder="5581999999999"
                aria-describedby="whatsapp-ajuda"
              />
            </label>
            <p className="field-help" id="whatsapp-ajuda">
              Digite somente números, incluindo DDI e DDD.
            </p>
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
              Ao enviar, nome, e-mail, WhatsApp e senha serão encaminhados ao
              servidor do projeto. Consulte as{" "}
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
          </fieldset>
        </form>
      )}
    </>
  );
}
