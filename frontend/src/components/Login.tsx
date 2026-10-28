import { useRef, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import { ArrowRight } from "./IsacIcons";
import { UsuarioService } from "../services/usuarios";

const usuarios = new UsuarioService(
  import.meta.env.VITE_API_BASE_URL || "/api",
);

export function Login() {
  const navigate = useNavigate();
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");
  const [visivel, setVisivel] = useState(false);
  const ocupado = useRef(false);

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (ocupado.current) return;
    const form = event.currentTarget;
    const dados = new FormData(form);
    ocupado.current = true;
    setEnviando(true);
    setErro("");
    try {
      await usuarios.login({
        email: String(dados.get("email") || ""),
        senha: String(dados.get("senha") || ""),
      });
      navigate("/perfil");
    } catch (error) {
      setErro(
        error instanceof Error
          ? error.message
          : "Não foi possível fazer login.",
      );
    } finally {
      ocupado.current = false;
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="page-heading">
        <p className="eyebrow">BEM-VINDO DE VOLTA</p>
        <h1>Entre na sua conta.</h1>
        <p>Continue sua jornada no Bazar Solidário.</p>
      </div>
      <form className="form-card" onSubmit={enviar} aria-busy={enviando}>
        <h2>Acesse sua conta</h2>
        <fieldset disabled={enviando} className="signup-fields">
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
            Senha
            <span className="password-field">
              <input
                name="senha"
                type={visivel ? "text" : "password"}
                autoComplete="current-password"
                required
                minLength={6}
                maxLength={72}
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
          {erro && (
            <p className="form-error" role="alert">
              {erro}
            </p>
          )}
          <button className="btn full" type="submit">
            {enviando ? "Entrando…" : "Entrar"}
            <ArrowRight size={18} />
          </button>
          <p style={{ textAlign: "center", marginTop: 14, fontSize: 13 }}>
            Ainda não tem conta?{" "}
            <Link className="text-button" to="/cadastro">
              Criar cadastro
            </Link>
          </p>
        </fieldset>
      </form>
    </>
  );
}

