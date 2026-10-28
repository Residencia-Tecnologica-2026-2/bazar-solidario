import { obterSessao, limparSessao } from "./usuarios.ts";

export interface Categoria {
  id: number;
  nome: string;
}
export interface Anuncio {
  id: number;
  versao?: number;
  titulo: string;
  descricao: string;
  preco: number;
  estoque: number;
  status: string;
  categoria: Categoria;
  imagens: string[];
  vendedor?: { id: number; nome: string };
  loja?: { id: number; nome: string };
}
export interface DadosAnuncio {
  versao?: number;
  titulo: string;
  descricao: string;
  preco: number;
  estoque: number;
  categoriaId: number;
  imagens: string[];
}
const base = (import.meta.env?.VITE_API_BASE_URL || "/api").replace(/\/$/, "");
export function fotoAnuncio(url: string) {
  return url.startsWith("/uploads/") ? base + url : url;
}

export async function api<T>(
  path: string,
  options: RequestInit = {},
  privada: boolean | "sessao" = false,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (privada) {
    const sessao = obterSessao();
    if (!sessao) throw new Error("Entre novamente para continuar.");
    if (privada === true && sessao.usuario.role !== "LOJA")
      throw new Error("Esta área é exclusiva para contas de brechó.");
    headers.set("Authorization", "Bearer " + sessao.token);
  }
  let resposta: Response;
  try {
    resposta = await globalThis.fetch(base + path, { ...options, headers });
  } catch {
    throw new Error("Não foi possível conectar ao servidor. Tente novamente.");
  }
  if (!resposta.ok) {
    if (resposta.status === 401) {
      limparSessao();
      throw new Error("Sua sessão expirou. Entre novamente.");
    }
    if (resposta.status === 403)
      throw new Error("Você não tem permissão para esta operação.");
    if (resposta.status === 404)
      throw new Error("O registro não foi encontrado.");
    if (
      resposta.status === 409 ||
      (resposta.status === 400 && path.startsWith("/pedidos"))
    ) {
      const erro = await resposta.json().catch(() => ({}));
      throw new Error(
        erro.erro ||
          (path.startsWith("/anuncios")
            ? "A peça mudou. Recarregue a lista antes de editar."
            : "O estoque ou o pedido mudou. Atualize sua sacola e tente novamente."),
      );
    }
    if (resposta.status === 400)
      throw new Error(
        "Confira os campos: título, categoria, preço, estoque e foto.",
      );
    throw new Error("Não foi possível concluir a operação. Tente novamente.");
  }
  return resposta.json();
}
export const categorias = () => api<Categoria[]>("/categorias");
export const meusAnuncios = (page = 0) =>
  api<{ content: Anuncio[]; last: boolean }>(
    `/anuncios/meus?page=${page}&size=12`,
    {},
    true,
  );
export const anunciosPublicos = (page = 0) =>
  api<{ content: Anuncio[]; last: boolean }>(`/anuncios?page=${page}&size=12`);
export const salvarAnuncio = (dados: DadosAnuncio, id?: number) =>
  api<Anuncio>(
    id ? `/anuncios/${id}` : "/anuncios",
    {
      method: id ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dados),
    },
    true,
  );
export const alterarStatus = (id: number, ativo: boolean) =>
  api<Anuncio>(
    `/anuncios/${id}/${ativo ? "ativar" : "desativar"}`,
    { method: "PATCH" },
    true,
  );
export async function enviarFoto(file: File) {
  if (
    !["image/jpeg", "image/png", "image/webp"].includes(file.type) ||
    file.size > 5 * 1024 * 1024
  )
    throw new Error("Escolha uma foto JPG, PNG ou WebP de até 5 MB.");
  const body = new FormData();
  body.append("arquivo", file);
  return (
    await api<{ url: string }>("/uploads", { method: "POST", body }, true)
  ).url;
}
