// ──────────────────────────────────────────────────────────────
// Tipos públicos
// ──────────────────────────────────────────────────────────────

export type TipoConta = "CLIENTE" | "LOJA";

export interface CadastroUsuario {
  nome: string;
  email: string;
  senha: string;
  tipo?: TipoConta;
  whatsapp?: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioCriado {
  id: number;
  nome: string;
  email: string;
  role: TipoConta | "ADMIN";
}

export interface SessaoUsuario {
  token: string;
  expiraEm: number; // timestamp ms
  usuario: UsuarioCriado;
}

// ──────────────────────────────────────────────────────────────
// Gerenciamento de sessão (localStorage)
// ──────────────────────────────────────────────────────────────

const SESSION_KEY = "bs-sessao";

export function salvarSessao(sessao: SessaoUsuario): void {
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(sessao));
  } catch {
    /* storage pode estar desabilitado */
  }
}

export function obterSessao(): SessaoUsuario | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const s: SessaoUsuario = JSON.parse(raw);
    if (!s?.token || !s?.usuario) return null;
    if (s.expiraEm && Date.now() > s.expiraEm) {
      limparSessao();
      return null;
    }
    return s;
  } catch {
    return null;
  }
}

export function limparSessao(): void {
  try {
    localStorage.removeItem(SESSION_KEY);
  } catch {
    /* ok */
  }
}

export function estaLogado(): boolean {
  return obterSessao() !== null;
}

// ──────────────────────────────────────────────────────────────
// Service
// ──────────────────────────────────────────────────────────────

/** Contrato disponível no UsuarioController e AuthController. */
export class UsuarioService {
  private readonly baseUrl: string;
  private readonly fetcher: typeof fetch;

  constructor(baseUrl: string, fetcher: typeof fetch = (...args) => globalThis.fetch(...args)) {
    this.baseUrl = baseUrl.replace(/\/$/, "");
    this.fetcher = fetcher;
  }

  // ─── helpers ──────────────────────────────────────────────

  private async chamar(path: string, body: unknown): Promise<Response> {
    try {
      return await this.fetcher(`${this.baseUrl}${path}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(body),
      });
    } catch {
      throw new Error(
        "Não foi possível conectar ao servidor. Confira sua conexão e tente novamente.",
      );
    }
  }

  private async tratarResposta(
    resposta: Response,
    contexto: "cadastro" | "login",
  ): Promise<SessaoUsuario> {
    if (!resposta.ok) {
      if ([502, 503, 504].includes(resposta.status))
        throw new Error(
          "O servidor está indisponível no momento. Tente novamente mais tarde.",
        );
      if (resposta.status === 409)
        throw new Error("Este e-mail já está cadastrado. Use outro e-mail.");
      if (resposta.status === 401)
        throw new Error("E-mail ou senha inválidos.");
      if (resposta.status === 400 || resposta.status === 422)
        throw new Error("Confira os dados informados e tente novamente.");
      throw new Error(
        contexto === "cadastro"
          ? "O servidor não conseguiu concluir seu cadastro. Tente novamente mais tarde."
          : "Não foi possível fazer login. Tente novamente.",
      );
    }

    let payload: {
      token?: string;
      expiraEmMs?: number;
      usuario?: Partial<UsuarioCriado>;
    };
    try {
      payload = await resposta.json();
    } catch {
      throw new Error("O servidor retornou uma resposta inesperada.");
    }

    if (!payload || typeof payload !== "object") {
      throw new Error("O servidor retornou uma resposta inesperada.");
    }
    const { token, expiraEmMs, usuario } = payload;

    if (
      typeof token !== "string" || !token.trim() ||
      typeof usuario?.id !== "number" || !Number.isSafeInteger(usuario.id) || usuario.id <= 0 ||
      typeof usuario.nome !== "string" ||
      typeof usuario.email !== "string" ||
      !["CLIENTE", "LOJA", "ADMIN"].includes(usuario.role ?? "") ||
      (contexto === "cadastro" && usuario.role === "ADMIN")
    ) {
      throw new Error("O servidor retornou uma resposta inesperada.");
    }

    const sessao: SessaoUsuario = {
      token,
      expiraEm: expiraEmMs ? Date.now() + expiraEmMs : 0,
      usuario: {
        id: usuario.id,
        nome: usuario.nome,
        email: usuario.email,
        role: (usuario.role as UsuarioCriado["role"]) ?? "CLIENTE",
      },
    };

    salvarSessao(sessao);
    return sessao;
  }

  // ─── cadastrar ─────────────────────────────────────────────

  async cadastrar(
    dados: CadastroUsuario,
    confirmacao: string = dados.senha,
  ): Promise<UsuarioCriado> {
    const nome = dados.nome.trim();
    const email = dados.email.trim();

    if (!nome || nome.length > 100)
      throw new Error("Informe seu nome (até 100 caracteres).");
    if (email.length > 255 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      throw new Error("Informe um e-mail válido.");
    if (
      dados.senha.length < 6 ||
      dados.senha.length > 72 ||
      !dados.senha.trim()
    )
      throw new Error("A senha deve ter entre 6 e 72 caracteres.");
    if (dados.senha !== confirmacao)
      throw new Error("As senhas precisam ser iguais.");
    if (dados.tipo === "LOJA") {
      const wpp = (dados.whatsapp ?? "").replace(/\D/g, "");
      if (wpp.length < 10 || wpp.length > 13)
        throw new Error(
          "Informe o WhatsApp com DDI e DDD (10 a 13 dígitos).",
        );
    }

    const corpo: Record<string, unknown> = {
      nome,
      email,
      senha: dados.senha,
    };
    if (dados.tipo) corpo.tipo = dados.tipo;
    if (dados.tipo === "LOJA" && dados.whatsapp)
      corpo.whatsapp = dados.whatsapp.replace(/\D/g, "");

    const resposta = await this.chamar("/usuarios", corpo);
    const sessao = await this.tratarResposta(resposta, "cadastro");
    return sessao.usuario;
  }

  // ─── login ─────────────────────────────────────────────────

  async login(dados: LoginRequest): Promise<SessaoUsuario> {
    if (!dados.email.trim() || !dados.senha)
      throw new Error("Informe e-mail e senha.");

    const resposta = await this.chamar("/auth/login", {
      email: dados.email.trim(),
      senha: dados.senha,
    });
    return this.tratarResposta(resposta, "login");
  }
}

/** Compatibilidade com os consumidores anteriores. */
export function criarClienteUsuarios(
  baseUrl: string,
  fetcher: typeof fetch = fetch,
) {
  const service = new UsuarioService(baseUrl, fetcher);
  return (dados: CadastroUsuario) => service.cadastrar(dados);
}
