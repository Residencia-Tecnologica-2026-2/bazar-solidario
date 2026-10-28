import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { obterSessao } from "../services/usuarios";
import {
  alterarStatus,
  anunciosPublicos,
  categorias,
  enviarFoto,
  fotoAnuncio,
  meusAnuncios,
  salvarAnuncio,
  type Anuncio,
  type Categoria,
} from "../services/anuncios";
import { Plus, Store, ArrowRight } from "./IsacIcons";

const money = (valor: number) =>
  valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
export function ProdutosLoja() {
  const sessao = obterSessao();
  const [items, setItems] = useState<Anuncio[]>([]),
    [opcoes, setOpcoes] = useState<Categoria[]>([]);
  const [carregando, setCarregando] = useState(true),
    [enviando, setEnviando] = useState(false),
    [erro, setErro] = useState(""),
    [aviso, setAviso] = useState("");
  const [edicao, setEdicao] = useState<Anuncio | null>(null),
    [pagina, setPagina] = useState(0),
    [ultima, setUltima] = useState(true),
    [foto, setFoto] = useState<File | null>(null),
    [preview, setPreview] = useState("");
  const formRef = useRef<HTMLFormElement>(null),
    formSection = useRef<HTMLElement>(null),
    ocupado = useRef(false);
  const loja = sessao?.usuario.role === "LOJA";
  useEffect(() => {
    if (!foto) {
      setPreview("");
      return;
    }
    const url = URL.createObjectURL(foto);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [foto]);
  useEffect(() => {
    if (!loja) return;
    let ativo = true;
    setCarregando(true);
    setErro("");
    Promise.all([meusAnuncios(pagina), categorias()])
      .then(([lista, cats]) => {
        if (ativo) {
          setItems(lista.content);
          setUltima(lista.last);
          setOpcoes(cats);
        }
      })
      .catch((e) => {
        if (ativo) setErro(e.message);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [loja, pagina]);
  async function atualizar() {
    const lista = await meusAnuncios(pagina);
    setItems(lista.content);
    setUltima(lista.last);
  }
  function editar(item: Anuncio | null) {
    setEdicao(item);
    setFoto(null);
    setErro("");
    setAviso("");
    formRef.current?.reset();
    formSection.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  }
  async function enviar(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (ocupado.current) return;
    ocupado.current = true;
    setEnviando(true);
    setErro("");
    setAviso("");
    const form = e.currentTarget;
    const dados = new FormData(form);
    try {
      const preco = Number(String(dados.get("preco")).replace(",", ".")),
        estoque = Number(dados.get("estoque"));
      if (
        !Number.isFinite(preco) ||
        preco <= 0 ||
        !Number.isInteger(estoque) ||
        estoque < 0
      )
        throw new Error(
          "Informe um preço válido e um estoque inteiro, a partir de zero.",
        );
      if (!foto && !edicao?.imagens.length)
        throw new Error("Adicione uma foto da peça.");
      const imagens = foto ? [await enviarFoto(foto)] : edicao!.imagens;
      await salvarAnuncio(
        {
          versao: edicao?.versao,
          titulo: String(dados.get("titulo")).trim(),
          descricao: String(dados.get("descricao")).trim(),
          preco,
          estoque,
          categoriaId: Number(dados.get("categoria")),
          imagens,
        },
        edicao?.id,
      );
      setAviso(edicao ? "Peça atualizada." : "Peça publicada no catálogo.");
      setEdicao(null);
      setFoto(null);
      form.reset();
      await atualizar();
      window.dispatchEvent(new Event("catalogo-atualizado"));
    } catch (e) {
      setErro(
        e instanceof Error ? e.message : "Não foi possível salvar a peça.",
      );
    } finally {
      ocupado.current = false;
      setEnviando(false);
    }
  }
  async function status(item: Anuncio) {
    if (ocupado.current) return;
    ocupado.current = true;
    setEnviando(true);
    setErro("");
    try {
      await alterarStatus(item.id, item.status !== "ATIVO");
      await atualizar();
      window.dispatchEvent(new Event("catalogo-atualizado"));
      setAviso("Visibilidade da peça atualizada.");
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      ocupado.current = false;
      setEnviando(false);
    }
  }
  if (!sessao)
    return (
      <section className="page-heading">
        <h1>Entre na sua loja.</h1>
        <p>Use uma conta de brechó para publicar e gerenciar suas peças.</p>
        <Link className="btn" to="/login">
          Fazer login <ArrowRight size={18} />
        </Link>
      </section>
    );
  if (!loja)
    return (
      <section className="page-heading">
        <h1>Área do brechó.</h1>
        <p>
          Sua conta é de usuário. O cadastro de peças é exclusivo para contas de
          brechó.
        </p>
        <Link to="/perfil" className="btn">
          Voltar ao perfil
        </Link>
      </section>
    );
  return (
    <>
      <header className="page-heading">
        <p className="eyebrow">PAINEL DO BRECHÓ</p>
        <h1>Peças da sua loja.</h1>
        <p>
          Publique fotos e informações e controle o que aparece no catálogo.
        </p>
        <Link className="text-button" to="/pecas-das-lojas">
          Ver catálogo publicado <ArrowRight size={16} />
        </Link>
      </header>
      {erro && (
        <p role="alert" className="form-error">
          {erro}
        </p>
      )}
      {aviso && (
        <p role="status" className="shop-notice">
          {aviso}
        </p>
      )}
      <section ref={formSection} className="form-card shop-form">
        <h2>{edicao ? "Editar peça" : "Cadastrar uma peça"}</h2>
        <p className="body-copy">
          A peça será vinculada automaticamente ao seu brechó.
        </p>
        <form
          key={edicao?.id ?? "nova"}
          ref={formRef}
          onSubmit={enviar}
          aria-busy={enviando}
        >
          <fieldset disabled={enviando} className="signup-fields">
            <label>
              Nome da peça
              <input
                name="titulo"
                required
                maxLength={150}
                defaultValue={edicao?.titulo ?? ""}
                placeholder="Ex.: Camisa de linho"
              />
            </label>
            <label>
              Categoria
              <select
                name="categoria"
                required
                defaultValue={edicao?.categoria.id ?? ""}
              >
                <option value="" disabled>
                  Selecione uma categoria
                </option>
                {opcoes.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nome}
                  </option>
                ))}
              </select>
            </label>
            <div className="form-pair">
              <label>
                Preço (R$)
                <input
                  name="preco"
                  type="number"
                  inputMode="decimal"
                  min="0.01"
                  max="99999999.99"
                  step="0.01"
                  required
                  defaultValue={edicao?.preco ?? ""}
                />
              </label>
              <label>
                Estoque disponível (sem reservas)
                <input
                  name="estoque"
                  type="number"
                  inputMode="numeric"
                  min="0"
                  max="2147483647"
                  step="1"
                  required
                  defaultValue={edicao?.estoque ?? 1}
                />
              </label>
            </div>
            <label>
              Descrição
              <textarea
                name="descricao"
                maxLength={2000}
                rows={4}
                defaultValue={edicao?.descricao ?? ""}
                placeholder="Informe tamanho, material e estado de conservação."
              />
            </label>
            <label>
              Foto da peça
              <input
                type="file"
                name="foto"
                accept="image/jpeg,image/png,image/webp"
                onChange={(e) => setFoto(e.target.files?.[0] ?? null)}
              />
              <span className="field-help">JPG, PNG ou WebP, até 5 MB.</span>
            </label>
            {(preview || edicao?.imagens[0]) && (
              <img
                className="shop-photo-preview"
                src={preview || fotoAnuncio(edicao!.imagens[0])}
                alt="Prévia da foto da peça"
              />
            )}
            <button
              className="btn full"
              type="submit"
              disabled={!opcoes.length || enviando}
            >
              {enviando
                ? "Salvando…"
                : edicao
                  ? "Salvar alterações"
                  : "Publicar peça"}
              <Plus size={20} />
            </button>
            {edicao && (
              <button
                type="button"
                className="text-button"
                onClick={() => editar(null)}
              >
                Cancelar edição
              </button>
            )}
          </fieldset>
        </form>
      </section>
      <section className="shop-products">
        <h2>Minhas peças</h2>
        <button
          className="text-button"
          disabled={enviando || carregando}
          onClick={async () => {
            setCarregando(true);
            try {
              await atualizar();
              editar(null);
            } catch (e) {
              setErro((e as Error).message);
            } finally {
              setCarregando(false);
            }
          }}
        >
          Recarregar peças e estoque
        </button>
        {carregando ? (
          <p role="status">Carregando suas peças…</p>
        ) : !items.length ? (
          <p>
            Nenhuma peça cadastrada nesta página. Publique sua primeira peça
            acima.
          </p>
        ) : (
          items.map((item) => (
            <article className="shop-product" key={item.id}>
              {item.imagens[0] ? (
                <img src={fotoAnuncio(item.imagens[0])} alt={item.titulo} />
              ) : (
                <Store size={40} />
              )}
              <div>
                <span className="eyebrow">
                  {item.status === "ATIVO"
                    ? "Publicado"
                    : item.status === "INATIVO"
                      ? "Oculto"
                      : "Vendido"}
                </span>
                <h3>{item.titulo}</h3>
                <p>
                  {money(item.preco)} · {item.estoque} unidade(s)
                </p>
                <div className="shop-product-actions">
                  <button
                    type="button"
                    disabled={enviando}
                    onClick={() => editar(item)}
                  >
                    Editar
                  </button>
                  {item.status !== "VENDIDO" && (
                    <button
                      type="button"
                      disabled={enviando}
                      onClick={() => status(item)}
                    >
                      {item.status === "ATIVO" ? "Ocultar" : "Publicar"}
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))
        )}
        <div className="shop-pagination">
          <button
            disabled={pagina === 0 || carregando || enviando}
            onClick={() => setPagina((p) => p - 1)}
          >
            Anterior
          </button>
          <span>Página {pagina + 1}</span>
          <button
            disabled={ultima || carregando || enviando}
            onClick={() => setPagina((p) => p + 1)}
          >
            Próxima
          </button>
        </div>
      </section>
    </>
  );
}

export function PecasPublicadas() {
  const [items, setItems] = useState<Anuncio[]>([]),
    [erro, setErro] = useState(""),
    [carregando, setCarregando] = useState(true),
    [pagina, setPagina] = useState(0),
    [ultima, setUltima] = useState(true);
  useEffect(() => {
    let ativo = true;
    setCarregando(true);
    setErro("");
    anunciosPublicos(pagina)
      .then((r) => {
        if (ativo) {
          setItems(r.content);
          setUltima(r.last);
        }
      })
      .catch((e) => {
        if (ativo) setErro(e.message);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [pagina]);
  return (
    <>
      <header className="page-heading">
        <p className="eyebrow">CATÁLOGO DAS LOJAS</p>
        <h1>Peças publicadas.</h1>
        <p>Anúncios cadastrados pelos brechós, carregados do servidor.</p>
      </header>
      {erro && (
        <p role="alert" className="form-error">
          {erro}
        </p>
      )}
      {carregando ? (
        <p role="status">Carregando catálogo…</p>
      ) : (
        <div className="published-grid">
          {items.map((p) => (
            <article key={p.id}>
              {p.imagens[0] && (
                <img src={fotoAnuncio(p.imagens[0])} alt={p.titulo} />
              )}
              <p className="eyebrow">{p.loja?.nome || p.vendedor?.nome}</p>
              <h2>{p.titulo}</h2>
              <strong>{money(p.preco)}</strong>
              <p>{p.descricao}</p>
            </article>
          ))}
          {!items.length && <p>Ainda não há peças publicadas nesta página.</p>}
        </div>
      )}
      <div className="shop-pagination">
        <button
          disabled={pagina === 0 || carregando}
          onClick={() => setPagina((p) => p - 1)}
        >
          Anterior
        </button>
        <span>Página {pagina + 1}</span>
        <button
          disabled={ultima || carregando}
          onClick={() => setPagina((p) => p + 1)}
        >
          Próxima
        </button>
      </div>
    </>
  );
}
