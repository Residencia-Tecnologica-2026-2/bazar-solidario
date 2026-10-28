import { X } from "lucide-react";
import { Heart, ShoppingBag, Search, Leaf, HandHeart, Home, UserRound, Shirt, CalendarDays } from "./components/IsacIcons";
import { ClothingIcon, AccessoriesIcon, ShoesIcon, SavedIcon } from "./components/IsacIcons";
import { useEffect, useRef, useState, type ComponentType } from "react";
import {
  Link,
  NavLink,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  Plus,

  SlidersHorizontal,
  MapPin,
  ChevronRight,
  Check,
  Store,
  Info,
  Trash2,
  CheckCircle2,
} from "./components/IsacIcons";
import { events, photo, products, type Product } from "./data/store";
import "./styles/mobile.css";
import { Brand } from "./components/Brand";
import { brand } from "./config/brand";
import "./styles/urban.css";
import { Cadastro } from "./components/Cadastro";
import { Login } from "./components/Login";
import { ProdutosLoja, PecasPublicadas } from "./components/ProdutosLoja";
import { Pagamento } from "./components/Pagamento";
import { obterSessao, limparSessao } from "./services/usuarios";

import { CatalogoService } from "./services/CatalogoService";
import { SacolaService } from "./services/SacolaService";

const catalogo = new CatalogoService(products);
const sacola = new SacolaService(products);

const money = (n: number) =>
  n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
function savedIds(key: string): number[] {
  try {
    return sacola.restaurar(localStorage.getItem(key));
  } catch {
    return [];
  }
}

type CatalogProps = {
  favoritesOnly?: boolean;
  favorites: number[];
  search: string;
  Cards: ComponentType<{ items: Product[] }>;
  Heading: ComponentType<{
    eyebrow?: string;
    title: string;
    subtitle?: string;
  }>;
  Empty: ComponentType<{ title: string; text: string; icon?: string }>;
};
function Catalog({
  favoritesOnly = false,
  favorites,
  Cards,
  Heading,
  Empty,
  search,
}: CatalogProps) {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState(
    new URLSearchParams(search).get("categoria") || "Todos",
  );
  const [sort, setSort] = useState("curadoria");
  const [filters, setFilters] = useState(false);
  const [size, setSize] = useState("Todos");
  useEffect(() => {
    setCategory(new URLSearchParams(search).get("categoria") || "Todos");
  }, [search, favoritesOnly]);
  const items = catalogo.buscar({ query, category, size, sort, favoritesOnly, favorites });
  return (
    <>
      <Heading
        eyebrow="PEÇAS ÚNICAS, NOVOS COMEÇOS"
        title={favoritesOnly ? "Seus favoritos." : "Encontre seu achado."}
        subtitle={
          favoritesOnly
            ? "Guarde o que combina com você."
            : "Uma curadoria para o seu próximo capítulo."
        }
      />
      {!favoritesOnly && <Link className="shop-public-entry text-button" to="/pecas-das-lojas">Ver peças publicadas pelas lojas <ArrowRight size={16}/></Link>}
      <div className="search-field">
        <Search size={19} />
        <input
          aria-label="Buscar peças ou brechós"
          placeholder="Busque uma peça ou brechó"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button
          className="filter-icon"
          aria-label="Abrir filtros"
          aria-expanded={filters}
          onClick={() => setFilters(!filters)}
        >
          <SlidersHorizontal size={18} />
        </button>
      </div>
      <div className="chips">
        {["Todos", "Roupas", "Acessórios", "Calçados"].map((c) => (
          <button
            className={c === category ? "active" : ""}
            key={c}
            onClick={() => setCategory(c)}
          >
            {c}
          </button>
        ))}
      </div>
      {filters && (
        <div className="filter-panel">
          <label>
            Tamanho
            <select value={size} onChange={(e) => setSize(e.target.value)}>
              {["Todos", "P", "M", "G", "38", "Único"].map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </label>
          <button
            className="text-button"
            onClick={() => {
              setCategory("Todos");
              setSize("Todos");
              setQuery("");
              setSort("curadoria");
            }}
          >
            Limpar filtros
          </button>
        </div>
      )}
      <div className="results">
        <span aria-live="polite">{items.length} achados</span>
        <label>
          <span className="visually-hidden">Ordenar produtos</span>
          <select value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="curadoria">Nossa curadoria</option>
            <option value="menor">Menor preço</option>
            <option value="maior">Maior preço</option>
          </select>
        </label>
      </div>
      {items.length ? (
        <Cards items={items} />
      ) : (
        <Empty
          icon="heart"
          title="Nenhum achado por aqui"
          text="Tente outra busca ou explore o catálogo para salvar suas peças favoritas."
        />
      )}
      <p className="demo-note">
        Peças, preços e brechós ilustrativos do projeto.
      </p>
    </>
  );
}

export default function App() {
  const [cart, setCart] = useState(() => savedIds("bs-cart"));
  const [favorites, setFavorites] = useState(() => savedIds("bs-favorites"));
  const [notice, setNotice] = useState("");
  const [intro, setIntro] = useState(0);
  const timer = useRef<ReturnType<typeof setTimeout>>();
  const location = useLocation();
  const navigate = useNavigate();
  useEffect(() => {
    try {
      localStorage.setItem("bs-cart", JSON.stringify(cart));
    } catch {
      /* Storage may be disabled. */
    }
  }, [cart]);
  useEffect(() => {
    try {
      localStorage.setItem("bs-favorites", JSON.stringify(favorites));
    } catch {
      /* Storage may be disabled. */
    }
  }, [favorites]);
  useEffect(() => {
    window.scrollTo(0, 0);
    document.getElementById("conteudo")?.focus({ preventScroll: true });
  }, [location.pathname]);
  useEffect(() => () => clearTimeout(timer.current), []);
  function notify(text: string) {
    setNotice(text);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setNotice(""), 4500);
  }
  function add(id: number) {
    if (cart.includes(id)) notify("Esta peça já está na sua sacola.");
    else {
      setCart((atual) => sacola.adicionar(atual, id));
      notify("Achado adicionado à sacola.");
    }
  }
  function favorite(id: number) {
    setFavorites(
      favorites.includes(id)
        ? favorites.filter((x) => x !== id)
        : [...favorites, id],
    );
  }
  function Cards({ items }: { items: Product[] }) {
    return (
      <div className="product-grid">
        {items.map((p) => (
          <article className="product" key={p.id}>
            <div className="product-image">
              <Link to={`/peca/${p.id}`}>
                <img src={photo(p.image)} alt={p.name} loading="lazy" />
              </Link>
              <span className="size-tag">{p.size}</span>
              <button
                className={`favorite ${favorites.includes(p.id) ? "selected" : ""}`}
                aria-label={`${favorites.includes(p.id) ? "Remover" : "Salvar"} ${p.name} nos favoritos`}
                aria-pressed={favorites.includes(p.id)}
                onClick={() => favorite(p.id)}
              >
                <Heart size={17} />
              </button>
            </div>
            <p className="shop-name">{p.shop}</p>
            <Link to={`/peca/${p.id}`}>
              <h3>{p.name}</h3>
            </Link>
            <div className="product-bottom">
              <strong>{money(p.price)}</strong>
              <button
                aria-label={`Adicionar ${p.name} à sacola`}
                onClick={() => add(p.id)}
              >
                <Plus size={17} />
              </button>
            </div>
          </article>
        ))}
      </div>
    );
  }
  function Heading({
    eyebrow,
    title,
    subtitle,
  }: {
    eyebrow?: string;
    title: string;
    subtitle?: string;
  }) {
    return (
      <div className="page-heading">
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
    );
  }
  function SectionTitle({
    title,
    to,
    label = "Ver todos",
  }: {
    title: string;
    to?: string;
    label?: string;
  }) {
    return (
      <div className="section-title">
        <h2>{title}</h2>
        {to && (
          <Link to={to}>
            {label}
            <ArrowUpRight size={15} />
          </Link>
        )}
      </div>
    );
  }
  function HomePage() {
    return (
      <>
        <div className="welcome">
          <div>
            <p>QUE BOM TER VOCÊ AQUI</p>
            <h1>
              Seu próximo achado
              <br />
              tem <em>propósito.</em>
            </h1>
          </div>
          <Link className="avatar" to="/perfil" aria-label="Abrir perfil">
            <UserRound size={23} />
          </Link>
        </div>
        <Link className="search-field" to="/catalogo">
          <Search size={19} />
          <span>O que vamos descobrir hoje?</span>
          <span className="filter-icon">
            <SlidersHorizontal size={18} />
          </span>
        </Link>
        <section className="hero-card">
          <div className="hero-text">
            <p className="eyebrow">ESTILO NO SEU DIA A DIA</p>
            <h2>
              Bom pra você.
              <br />
              <em>
                Melhor para
                <br />o mundo.
              </em>
            </h2>
            <p>
              Peças únicas que apoiam
              <br />a comunidade.
            </p>
            <Link className="btn aqua" to="/catalogo">
              Explorar achados <ArrowUpRight size={16} />
            </Link>
          </div>
          <img
            src={photo("photo-1598082943135-fe5037a7649f", 900)}
            alt="Fotografia ilustrativa: pessoa de camiseta branca na praia, por Mathias Reding"
          />
          <span className="hero-stamp">
            <Leaf size={16} /> MODA CIRCULAR
          </span>
        </section>
        <div className="impact-note">
          <HandHeart size={24} />
          <p>
            Uma nova vida para cada peça.
            <br />
            <strong>Novas possibilidades para a comunidade.</strong>
          </p>
          <Link to="/sobre" aria-label="Conhecer o propósito">
            <ChevronRight size={20} />
          </Link>
        </div>
        <section>
          <SectionTitle
            title="Qual é o seu estilo?"
            to="/catalogo"
            label="Explorar"
          />
          <div className="categories">
            {[
              ["Roupas", ClothingIcon],
              ["Acessórios", AccessoriesIcon],
              ["Calçados", ShoesIcon],
              ["Favoritos", SavedIcon],
            ].map(([name, Icon]) => {
              const C = Icon as typeof Shirt;
              const n = name as string;
              return (
                <Link
                  key={n}
                  to={
                    n === "Favoritos"
                      ? "/favoritos"
                      : `/catalogo?categoria=${encodeURIComponent(n)}`
                  }
                >
                  <span>
                    <C size={36} strokeWidth={1.7} />
                  </span>
                  {n}
                </Link>
              );
            })}
          </div>
        </section>
        <section>
          <SectionTitle title="Achados da curadoria" to="/catalogo" />
          <Cards items={products.slice(0, 4)} />
        </section>
        <Link to="/sobre" className="purpose-banner">
          <span className="purpose-icon">
            <Leaf size={26} />
          </span>
          <div>
            <p className="eyebrow">MAIS QUE UM BRECHÓ</p>
            <h2>
              Seu estilo pode
              <br />
              fazer a diferença.
            </h2>
            <p>Conheça a nossa razão de existir.</p>
          </div>
          <ArrowUpRight size={23} />
        </Link>
        <section>
          <SectionTitle title="Vamos nos encontrar?" to="/eventos" />
          <EventCards compact />
        </section>
        <Link className="partner-banner" to="/vender">
          <Store size={25} />
          <div>
            <h3>Seu brechó também cabe aqui.</h3>
            <p>Faça parte de uma nova história.</p>
          </div>
          <ArrowUpRight size={20} />
        </Link>
        <p className="demo-note">
          Projeto demonstrativo · catálogo e lojas ilustrativos.
        </p>
      </>
    );
  }
  function ProductPage() {
    const p = products.find(
      (x) => x.id === Number(location.pathname.split("/").pop()),
    );
    if (!p) return <Missing />;
    return (
      <>
        <div className="detail-photo">
          <img src={photo(p.image, 1000)} alt={p.name} />
          <span className="detail-badge">
            <CheckCircle2 size={15} /> Peça única
          </span>
          <button
            className={`favorite ${favorites.includes(p.id) ? "selected" : ""}`}
            onClick={() => favorite(p.id)}
            aria-label="Alternar favorito"
            aria-pressed={favorites.includes(p.id)}
          >
            <Heart size={21} />
          </button>
        </div>
        <div className="detail-title">
          <div>
            <p className="eyebrow">{p.shop}</p>
            <h1>{p.name}</h1>
          </div>
          <strong>{money(p.price)}</strong>
        </div>
        <div className="specs">
          <div>
            <span>Tamanho</span>
            <strong>{p.size}</strong>
          </div>
          <div>
            <span>Cor</span>
            <strong>{p.color}</strong>
          </div>
          <div>
            <span>Condição</span>
            <strong>Muito bom</strong>
          </div>
        </div>
        <h2 className="small-title">Uma peça, novas possibilidades</h2>
        <p className="body-copy">{p.description}</p>
        <div className="info-box">
          <HandHeart size={22} />
          <p>
            O projeto propõe destinar parte das vendas à ONG parceira. Os
            repasses serão definidos no piloto.
          </p>
        </div>
        <details className="disclosure">
          <summary>
            Retirada, entrega e trocas
            <Plus size={16} />
          </summary>
          <p>
            As condições serão informadas por cada parceiro antes de uma venda
            real. Esta peça é ilustrativa; medidas, materiais e disponibilidade
            não estão verificados.
          </p>
        </details>
        <button className="btn full" onClick={() => add(p.id)}>
          <ShoppingBag size={18} />
          {cart.includes(p.id) ? "Já está na sacola" : "Adicionar à sacola"}
          <ArrowRight size={18} />
        </button>
        <p className="demo-note">
          Sem compra ou reserva real nesta demonstração.
        </p>
        <section>
          <SectionTitle title="Outros bons encontros" />
          <Cards items={products.filter((x) => x.id !== p.id).slice(0, 2)} />
        </section>
      </>
    );
  }
  function Empty({
    title,
    text,
    icon = "bag",
  }: {
    title: string;
    text: string;
    icon?: string;
  }) {
    return (
      <div className="empty">
        <span>
          {icon === "heart" ? <Heart size={32} /> : <ShoppingBag size={32} />}
        </span>
        <h2>{title}</h2>
        <p>{text}</p>
        <Link className="btn" to="/catalogo">
          Explorar achados <ArrowRight size={17} />
        </Link>
      </div>
    );
  }
  function CartPage() {
    const items = sacola.itens(cart);
    return (
      <>
        <Heading
          eyebrow="ESTILO NO SEU DIA A DIA A CAMINHO"
          title="Sua sacola."
          subtitle="Peças que você escolheu para um novo ciclo."
        />
        {items.length ? (
          <>
            <div className="cart-items">
              {items.map((p) => (
                <article className="cart-item" key={p.id}>
                  <Link to={`/peca/${p.id}`}>
                    <img src={photo(p.image, 300)} alt={p.name} />
                  </Link>
                  <div>
                    <p className="shop-name">{p.shop}</p>
                    <Link to={`/peca/${p.id}`}>
                      <h3>{p.name}</h3>
                    </Link>
                    <p>Tam. {p.size} · 1 peça</p>
                    <strong>{money(p.price)}</strong>
                  </div>
                  <button
                    aria-label={`Remover ${p.name}`}
                    onClick={() => setCart((atual) => sacola.remover(atual, p.id))}
                  >
                    <Trash2 size={18} />
                  </button>
                </article>
              ))}
            </div>
            <div className="summary">
              <h2>Resumo da sacola</h2>
              <p>
                <span>{items.length} peças</span>
                <strong>{money(sacola.subtotal(cart))}</strong>
              </p>
              <p>
                <span>Entrega ou retirada</span>
                <span>A combinar</span>
              </p>
              <div className="total">
                <span>Subtotal</span>
                <strong>{money(sacola.subtotal(cart))}</strong>
              </div>
              <Link className="btn full" to="/checkout">
                Continuar <ArrowRight size={18} />
              </Link>
              <p className="demo-note">
                Demonstração sem pagamento ou reserva.
              </p>
            </div>
            <Link className="center-link" to="/catalogo">
              Continuar descobrindo
            </Link>
          </>
        ) : (
          <Empty
            title="Sua próxima história começa aqui"
            text="A sacola está vazia. Encontre um achado que combine com você."
          />
        )}
      </>
    );
  }
  function Checkout() {
    if (!cart.length)
      return (
        <Empty
          title="Sua sacola está vazia"
          text="Escolha uma peça antes de simular um pedido."
        />
      );
    return (
      <>
        <Heading
          eyebrow="PRÉVIA DO PEDIDO"
          title="Um passo para um novo ciclo."
        />
        <Pagamento
          subtotal={sacola.subtotal(cart)}
          onSucesso={() => {}}
        />
      </>
    );
  }
  function EventCards({ compact = false }: { compact?: boolean }) {
    return (
      <div className="event-list">
        {events.slice(0, compact ? 1 : 2).map((e) => (
          <Link className="event-card" to={`/eventos/${e.id}`} key={e.id}>
            <img
              src={photo(e.photo, 800)}
              alt="Fotografia ilustrativa de roupas em uma arara de feira ao ar livre"
              loading="lazy"
            />
            <div>
              <p className="eyebrow">{e.kind} · PROPOSTA PILOTO</p>
              <h2>{e.name}</h2>
              <p>
                <CalendarDays size={14} /> Data a confirmar
              </p>
              <p>
                <MapPin size={14} /> Local a definir com a ONG
              </p>
              <span>
                Conhecer o encontro <ArrowUpRight size={16} />
              </span>
            </div>
          </Link>
        ))}
      </div>
    );
  }
  function EventsPage() {
    const event = events.find((e) => location.pathname === `/eventos/${e.id}`);
    return (
      <>
        <Heading
          eyebrow="CONEXÕES FORA DA TELA"
          title={event ? event.name : "Vamos nos encontrar?"}
          subtitle="Um espaço para circular peças e aproximar pessoas."
        />
        {event ? (
          <>
            <img
              className="event-detail"
              src={photo(event.photo, 900)}
              alt="Araras de roupas em uma loja"
            />
            <div className="event-info">
              <p>
                <CalendarDays size={18} /> Data a confirmar
              </p>
              <p>
                <MapPin size={18} /> Local a definir com a ONG parceira
              </p>
            </div>
            <p className="body-copy">{event.description}</p>
            <p className="body-copy">
              As condições de participação serão divulgadas após a confirmação
              do piloto. Nenhum evento está confirmado nesta versão.
            </p>
            <LocalForm title="Quero saber quando acontecer" kind="evento" />
          </>
        ) : (
          <>
            <div className="info-box">
              <Info size={19} />
              <p>
                Encontros propostos para o piloto. Datas e locais ainda serão
                definidos.
              </p>
            </div>
            <EventCards />
          </>
        )}
      </>
    );
  }
  function LocalForm({ title, kind }: { title: string; kind: string }) {
    const [done, setDone] = useState(false);
    return (
      <form
        className="form-card"
        onSubmit={(e) => {
          e.preventDefault();
          setDone(true);
        }}
      >
        <h2>{title}</h2>
        <p className="body-copy">
          Prévia do formulário. Nenhum dado será enviado ou armazenado.
        </p>
        {done ? (
          <div className="success" role="status">
            <CheckCircle2 size={36} />
            <h3>Prévia concluída</h3>
            <p>
              O envio será habilitado na versão integrada. Esta ação não realiza
              cadastro ou inscrição.
            </p>
            <button
              className="text-button"
              type="button"
              onClick={() => setDone(false)}
            >
              Preencher novamente
            </button>
          </div>
        ) : (
          <>
            <label>
              Nome
              <input
                autoComplete="name"
                required
                placeholder="Como podemos chamar você?"
              />
            </label>
            <label>
              E-mail
              <input
                type="email"
                autoComplete="email"
                required
                placeholder="voce@exemplo.com"
              />
            </label>
            {kind === "parceria" && (
              <label>
                Quero participar como
                <select>
                  <option>Brechó independente</option>
                  <option>ONG</option>
                  <option>Voluntário ou doador</option>
                </select>
              </label>
            )}
            {kind !== "evento" && (
              <label>
                Mensagem
                <textarea
                  rows={3}
                  required
                  placeholder="Conte um pouco sobre seu interesse"
                />
              </label>
            )}
            <label className="checkbox">
              <input type="checkbox" required />
              Entendo que esta é uma demonstração sem envio.
            </label>
            <button className="btn full">
              Concluir prévia <ArrowRight size={18} />
            </button>
          </>
        )}
      </form>
    );
  }
  function About() {
    return (
      <>
        <Heading
          eyebrow="MODA CIRCULAR. IMPACTO LOCAL."
          title="Mais que roupas. Uma rede de cuidado."
          subtitle="Um brechó que sustenta a ONG, não só a ajuda."
        />
        <div className="about-art">
          <HandHeart size={56} />
          <span>
            Peças com história.
            <br />
            <em>Escolhas com propósito.</em>
          </span>
        </div>
        <p className="body-copy">
          O Bazar Solidário conecta moradores, brechós independentes e ONGs. A
          proposta é transformar roupas doadas em renda recorrente e aproximar a
          comunidade do trabalho das organizações.
        </p>
        <div className="steps">
          {[
            [
              "01",
              "Descubra uma peça",
              "Explore a curadoria e confira o tamanho e o brechó responsável.",
            ],
            [
              "02",
              "Escolha um novo ciclo",
              "O projeto prevê compra online com entrega ou retirada e encontros presenciais na ONG.",
            ],
            [
              "03",
              "Fortaleça a comunidade",
              "Parte das vendas e da participação nos encontros poderá apoiar a ONG. Os valores serão acordados no piloto.",
            ],
          ].map(([n, t, d]) => (
            <article key={n}>
              <span>{n}</span>
              <div>
                <h3>{t}</h3>
                <p>{d}</p>
              </div>
            </article>
          ))}
        </div>
        <div className="info-box">
          <Leaf size={22} />
          <p>
            Próximo passo: validar o piloto com uma ONG e brechós locais antes
            de ampliar a rede.
          </p>
        </div>
        <Link className="btn full" to="/vender">
          Quero fazer parte <ArrowUpRight size={18} />
        </Link>
      </>
    );
  }
  function Partners({ ngo = false }: { ngo?: boolean }) {
    return (
      <>
        <Heading
          eyebrow="UMA REDE EM FORMAÇÃO"
          title={ngo ? "Quem cuida da comunidade." : "Brechós com propósito."}
          subtitle={
            ngo
              ? "A ONG é o coração dessa história."
              : "Mais visibilidade para seus achados."
          }
        />
        <div className="about-art">
          {ngo ? <HandHeart size={50} /> : <Store size={50} />}
          <span>
            {ngo ? "Novas oportunidades." : "Novas conexões."}
            <br />
            <em>Vamos construir juntos.</em>
          </span>
        </div>
        <p className="body-copy">
          {ngo
            ? "A ONG pode organizar seu catálogo de doações e receber encontros de brechós. O modelo busca criar renda recorrente e aproximar os moradores de suas atividades."
            : "Brechós independentes podem divulgar peças online e participar de encontros na ONG, ampliando seu público com regras claras de participação."}
        </p>
        <div className="info-box">
          <Info size={20} />
          <p>
            O piloto está em formação. Não há parceiros confirmados nesta
            demonstração; os nomes das lojas no catálogo são ilustrativos.
          </p>
        </div>
        <h2 className="small-title">Uma parceria transparente</h2>
        <p className="body-copy">
          Comissões, taxas dos encontros e divisão dos valores serão validadas
          com os parceiros antes das vendas reais. A próxima etapa prevê
          conectar outras ONGs para circulação de peças excedentes.
        </p>
        <Link className="btn full" to="/vender">
          Quero ser parceiro <ArrowUpRight size={18} />
        </Link>
      </>
    );
  }
  function Profile() {
    const [sessao, setSessao] = useState(obterSessao);

    function sair() {
      limparSessao();
      setSessao(null);
    }

    const usuario = sessao?.usuario;
    const isLoja = usuario?.role === "LOJA";

    return (
      <>
        <Heading
          eyebrow="SEU ESPAÇO NO BRECHÓ"
          title={usuario ? `Olá, ${usuario.nome.split(" ")[0]}.` : "Olá, visitante."}
          subtitle="Seu estilo. Suas histórias. Seus próximos achados."
        />
        <div className="profile-card">
          <span className="profile-avatar">
            {isLoja ? <Store size={36} /> : <UserRound size={36} />}
          </span>
          {usuario ? (
            <div>
              <h2>{usuario.nome}</h2>
              <p style={{ fontSize: 12, color: "var(--color-text-secondary)", marginBottom: 6 }}>
                {usuario.email}
              </p>
              <p style={{ fontSize: 11, letterSpacing: 1.2, fontWeight: 700, color: "var(--color-secondary)", marginBottom: 10 }}>
                {isLoja ? "BAZAR / BRECHÓ" : "USUÁRIO"}
              </p>
              <button className="text-button" onClick={sair} style={{ fontSize: 13 }}>
                Sair da conta
              </button>
            </div>
          ) : (
            <div>
              <h2>Vamos nos conhecer?</h2>
              <p>Explore o app sem precisar de cadastro.</p>
              <div style={{ display: "flex", gap: 14, marginTop: 4, flexWrap: "wrap" }}>
                <Link className="text-button" to="/login">
                  Fazer login <ArrowRight size={15} />
                </Link>
                <Link className="text-button" to="/cadastro">
                  Criar cadastro <ArrowRight size={15} />
                </Link>
              </div>
            </div>
          )}
        </div>
        {isLoja && <Link className="btn full shop-entry" to="/loja/produtos"><Plus size={20}/> Cadastrar e gerenciar minhas peças <ArrowRight size={18}/></Link>}
        <Link className="text-button shop-public-entry" to="/pecas-das-lojas">Ver peças publicadas pelos brechós <ArrowRight size={16}/></Link>
        <div className="menu-list">
          {[
            ["/favoritos", "Meus favoritos", Heart, favorites.length],
            ["/carrinho", "Minha sacola", ShoppingBag, cart.length],
            ["/eventos", "Encontros da comunidade", CalendarDays, ""],
            ["/brechos", "Conhecer os brechós", Store, ""],
            ["/ongs", "Conhecer as ONGs", HandHeart, ""],
            ["/sobre", "Como funciona o projeto", Leaf, ""],
            ["/vender", "Quero ser parceiro", Plus, ""],
            ["/faq", "Dúvidas frequentes", Info, ""],
            ["/contato", "Fale com a equipe", UserRound, ""],
          ].map(([url, label, Icon, count]) => {
            const C = Icon as typeof Heart;
            return (
              <Link key={url as string} to={url as string}>
                <C size={20} />
                <span>{label as string}</span>
                {count !== "" && <small>{count as number}</small>}
                <ChevronRight size={18} />
              </Link>
            );
          })}
        </div>
        <div className="legal-links">
          <Link to="/privacidade">Privacidade</Link>
          <Link to="/termos">Termos de uso</Link>
        </div>
        <p className="demo-note">ISAC Brechó · projeto Bazar Solidário</p>
      </>
    );
  }
  function Faq() {
    return (
      <>
        <Heading eyebrow="PODEMOS AJUDAR" title="Dúvidas frequentes." />
        {[
          [
            "Posso comprar de verdade?",
            "Esta é uma demonstração do front-end. Explore peças, salve favoritos e simule a sacola. Pagamentos, reservas e pedidos reais dependem da integração com o backend.",
          ],
          [
            "Como a compra apoia a ONG?",
            "A proposta prevê comissões sobre vendas e renda dos encontros presenciais. Os percentuais serão definidos com os parceiros no piloto.",
          ],
          [
            "Como posso doar roupas?",
            "A coleta e os tipos de peças aceitos serão definidos pela ONG participante. A rede ainda está em formação.",
          ],
          [
            "Como funciona a retirada?",
            "Local, horário, entrega e condições de troca serão informados pelos parceiros antes de uma compra real.",
          ],
          [
            "Como participo dos encontros?",
            "As propostas estão na página de encontros. Inscrições, datas e locais serão divulgados depois da confirmação do piloto.",
          ],
        ].map(([q, a]) => (
          <details className="disclosure" key={q}>
            <summary>
              {q}
              <Plus size={18} />
            </summary>
            <p>{a}</p>
          </details>
        ))}
        <Link className="btn full" to="/contato">
          Tenho outra dúvida <ArrowRight size={18} />
        </Link>
      </>
    );
  }
  function Onboarding() {
    const slides = [
      [
        "Peças com história.",
        "Descubra achados únicos e dê uma nova vida ao que já existe.",
        Shirt,
      ],
      [
        "Escolhas com propósito.",
        "Conheça uma proposta de moda circular que fortalece a comunidade.",
        HandHeart,
      ],
      [
        "Novas conexões.",
        "Encontre brechós independentes e acompanhe os encontros na ONG.",
        Store,
      ],
    ] as const;
    const [title, text, Icon] = slides[intro];
    return (
      <div className="onboarding">
        <button className="text-button" onClick={() => navigate("/")}>
          Pular <ArrowRight size={16} />
        </button>
        <div className="onboarding-art">
          <Icon size={100} strokeWidth={1} />
          <span>
            <Leaf size={35} />
          </span>
        </div>
        <p className="eyebrow">BEM-VINDO AO BAZAR SOLIDÁRIO</p>
        <h1>{title}</h1>
        <p>{text}</p>
        <div className="dots">
          {slides.map((s, i) => (
            <button
              key={s[0]}
              aria-label={`Ver apresentação ${i + 1}`}
              aria-current={i === intro ? "step" : undefined}
              className={i === intro ? "active" : ""}
              onClick={() => setIntro(i)}
            />
          ))}
        </div>
        <button
          className="btn full"
          onClick={() => (intro < 2 ? setIntro(intro + 1) : navigate("/"))}
        >
          {intro < 2 ? "Continuar" : "Começar a descobrir"}
          <ArrowRight size={18} />
        </button>
      </div>
    );
  }
  function Missing() {
    return (
      <>
        <Heading eyebrow="404" title="Esse achado não está por aqui." />
        <Link className="btn full" to="/">
          Voltar ao início <ArrowRight size={18} />
        </Link>
      </>
    );
  }
  const titles: Record<string, string> = {
    "/catalogo": "Explorar",
    "/favoritos": "Favoritos",
    "/carrinho": "Sacola",
    "/perfil": "Meu espaço",
    "/eventos": "Encontros",
    "/sobre": "Nosso propósito",
    "/vender": "Faça parte",
    "/checkout": "Pagamento",
    "/cadastro": "Cadastro",
    "/login": "Entrar",
  };
  return (
    <div className="app-shell">
      <a className="skip-link" href="#conteudo">
        Ir para o conteúdo
      </a>
      <header className="app-header">
        {location.pathname === "/" ? (
          <Link className="brand" to="/" aria-label={`${brand.name} início`}>
            <Brand />
          </Link>
        ) : (
          <>
            <button
              className="back-button"
              aria-label="Voltar"
              onClick={() =>
                location.key === "default" ? navigate("/") : navigate(-1)
              }
            >
              <ArrowLeft size={20} />
            </button>
            <span className="header-title">
              {titles[location.pathname] || "Bazar Solidário"}
            </span>
          </>
        )}
        <Link
          className="header-bag"
          to="/carrinho"
          aria-label={`Sacola com ${cart.length} peças`}
        >
          <ShoppingBag size={21} />
          <span>{cart.length}</span>
        </Link>
      </header>
      <main id="conteudo" tabIndex={-1}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route
            path="/catalogo"
            element={
              <Catalog
                favorites={favorites}
                Cards={Cards}
                Heading={Heading}
                Empty={Empty}
                search={location.search}
              />
            }
          />
          <Route
            path="/favoritos"
            element={
              <Catalog
                favoritesOnly
                favorites={favorites}
                Cards={Cards}
                Heading={Heading}
                Empty={Empty}
                search={location.search}
              />
            }
          />
          <Route path="/peca/:id" element={<ProductPage />} />
          <Route path="/carrinho" element={<CartPage />} />
          <Route path="/checkout" element={<Checkout />} />
          <Route path="/perfil" element={<Profile />} />
          <Route path="/eventos" element={<EventsPage />} />
          <Route path="/eventos/:id" element={<EventsPage />} />
          <Route path="/sobre" element={<About />} />
          <Route path="/como-funciona" element={<About />} />
          <Route path="/ongs" element={<Partners ngo />} />
          <Route path="/brechos" element={<Partners />} />
          <Route
            path="/vender"
            element={
              <>
                <Heading
                  eyebrow="VAMOS CONSTRUIR JUNTOS"
                  title="Sua história também cabe aqui."
                  subtitle="Para brechós, ONGs e pessoas que querem fazer parte."
                />
                <LocalForm title="Vamos nos conhecer" kind="parceria" />
              </>
            }
          />
          <Route
            path="/contato"
            element={
              <>
                <Heading
                  title="Vamos conversar?"
                  subtitle="Um espaço para dúvidas e novas ideias."
                />
                <LocalForm title="Sua mensagem" kind="contato" />
              </>
            }
          />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/login" element={<Login />} />
          <Route path="/loja/produtos" element={<ProdutosLoja />} />
          <Route path="/pecas-das-lojas" element={<PecasPublicadas />} />
          <Route path="/boas-vindas" element={<Onboarding />} />
          <Route path="/faq" element={<Faq />} />
          <Route
            path="/privacidade"
            element={
              <>
                <Heading title="Sua privacidade." />
                <p className="body-copy">
                  A sacola e os favoritos ficam no armazenamento local do seu
                  navegador. O cadastro envia nome, e-mail e senha ao servidor
                  do projeto. A senha não é salva no navegador. Os formulários
                  de contato, parceria e eventos continuam sendo prévias sem
                  envio. Nenhum pagamento é processado.
                </p>
                <h2 className="small-title">Fotografias externas</h2>
                <p className="body-copy">
                  Parte das imagens ilustrativas é carregada do Unsplash. Seu
                  navegador se conecta a esse serviço para exibi-las. As imagens
                  da bolsa caramelo e do tênis casual foram geradas com IA para
                  este catálogo demonstrativo e não representam peças à venda.
                </p>
                <button
                  className="btn full"
                  onClick={() => {
                    setCart([]);
                    setFavorites([]);
                    notify("Sacola e favoritos foram apagados.");
                  }}
                >
                  Apagar meus dados locais <Trash2 size={18} />
                </button>
                <p className="body-copy">
                  Uma política completa deverá ser definida antes do lançamento
                  dos serviços integrados.
                </p>
              </>
            }
          />
          <Route
            path="/termos"
            element={
              <>
                <Heading title="Sobre esta versão." />
                <p className="body-copy">
                  Este app web é um protótipo em React e TypeScript. Produtos,
                  preços e nomes de lojas são exemplos; encontros não têm data
                  ou local confirmados.
                </p>
                <p className="body-copy">
                  A sacola não realiza compra ou reserva. O cadastro de usuários
                  está conectado ao backend. Os formulários de contato, parceria
                  e eventos não efetuam envio ou inscrição. Regras de venda e
                  parceria serão estabelecidas antes do lançamento real.
                </p>
              </>
            }
          />
          <Route path="*" element={<Missing />} />
        </Routes>
      </main>
      <nav className="bottom-nav" aria-label="Navegação principal">
        {[
          ["/", "Início", Home],
          ["/catalogo", "Explorar", Search],
          ["/favoritos", "Salvos", Heart],
          ["/eventos", "Encontros", CalendarDays],
          ["/perfil", "Perfil", UserRound],
        ].map(([url, label, Icon]) => {
          const C = Icon as typeof Home;
          return (
            <NavLink end={url === "/"} key={url as string} to={url as string}>
              <span>
                <C size={21} strokeWidth={1.7} />
              </span>
              {label as string}
            </NavLink>
          );
        })}
      </nav>
      {notice && (
        <div className="toast" role="status">
          <Check size={18} />
          <span>{notice}</span>
          <button aria-label="Fechar aviso" onClick={() => setNotice("")}>
            <X size={17} />
          </button>
        </div>
      )}
    </div>
  );
}

