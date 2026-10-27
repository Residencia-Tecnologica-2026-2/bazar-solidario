export interface CadastroUsuario {
  nome: string;
  email: string;
  senha: string;
}

export interface UsuarioCriado {
  id: number;
  nome: string;
  email: string;
  role: "CLIENTE" | "ADMIN";
}

/** Contrato disponível no UsuarioController. Não devolve a senha ao restante da UI. */
export class UsuarioService {
  private readonly baseUrl: string;
  private readonly fetcher: typeof fetch;

  constructor(baseUrl: string, fetcher: typeof fetch = fetch) {
    this.baseUrl = baseUrl;
    this.fetcher = fetcher;
  }

  async cadastrar(dados: CadastroUsuario, confirmacao: string = dados.senha): Promise<UsuarioCriado> {
    if (!dados.nome.trim() || dados.nome.trim().length > 100) throw new Error("Informe seu nome (até 100 caracteres).");
    if (dados.email.trim().length > 255 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(dados.email.trim())) throw new Error("Informe um e-mail válido.");
    if (dados.senha.length < 6 || dados.senha.length > 72 || !dados.senha.trim()) throw new Error("A senha deve ter entre 6 e 72 caracteres.");
    if (dados.senha !== confirmacao) throw new Error("As senhas precisam ser iguais.");
    let resposta: Response;
    try {
      resposta = await this.fetcher(`${this.baseUrl.replace(/\/$/, "")}/usuarios`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify({
          nome: dados.nome.trim(),
          email: dados.email.trim(),
          senha: dados.senha,
        }),
      });
    } catch {
      throw new Error(
        "Não foi possível conectar ao servidor. Confira sua conexão e tente novamente.",
      );
    }
    if (!resposta.ok) {
      if (
        resposta.status === 502 ||
        resposta.status === 503 ||
        resposta.status === 504
      )
        throw new Error(
          "O servidor está indisponível no momento. Tente novamente mais tarde.",
        );
      if (resposta.status === 409)
        throw new Error("Este e-mail já está cadastrado. Use outro e-mail.");
      if (resposta.status === 400 || resposta.status === 422)
        throw new Error("Confira os dados informados e tente novamente.");
      throw new Error(
        "O servidor não conseguiu concluir seu cadastro. Tente novamente mais tarde.",
      );
    }
    let usuario: Partial<UsuarioCriado>;
    try {
      const payload = await resposta.json();
      // AuthResponse atual: expõe somente os dados públicos, sem manter sessão.
      usuario = payload?.usuario ?? payload;
    } catch {
      throw new Error("O servidor retornou uma resposta inesperada.");
    }
    if (
      typeof usuario?.id !== "number" || !Number.isSafeInteger(usuario.id) || usuario.id <= 0 ||
      typeof usuario.nome !== "string" ||
      typeof usuario.email !== "string" ||
      usuario.role !== "CLIENTE"
    ) {
      throw new Error("O servidor retornou uma resposta inesperada.");
    }
    return {
      id: usuario.id,
      nome: usuario.nome,
      email: usuario.email,
      role: usuario.role,
    };
  }
}

/** Compatibilidade com os consumidores anteriores. */
export function criarClienteUsuarios(baseUrl: string, fetcher: typeof fetch = fetch) {
  const service = new UsuarioService(baseUrl, fetcher);
  return (dados: CadastroUsuario) => service.cadastrar(dados);
}
