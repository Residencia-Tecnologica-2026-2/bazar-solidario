import type { Product } from "../data/store.ts";

export interface FiltrosCatalogo {
  query?: string;
  category?: string;
  size?: string;
  sort?: string;
  favoritesOnly?: boolean;
  favorites?: readonly number[];
}

const normalizar = (texto: string) => texto.trim().normalize("NFD")
  .replace(/[\u0300-\u036f]/g, "").toLocaleLowerCase("pt-BR");

/** Consulta as peças sem modificar a coleção original. */
export class CatalogoService {
  private readonly produtos: readonly Product[];

  constructor(produtos: readonly Product[]) {
    this.produtos = produtos.map((produto) => ({ ...produto }));
  }

  buscar(filtros: FiltrosCatalogo = {}): Product[] {
    const { query = "", category = "Todos", size = "Todos", sort = "curadoria",
      favoritesOnly = false, favorites = [] } = filtros;
    return this.produtos.filter((p) =>
      (!favoritesOnly || favorites.includes(p.id)) &&
      (category === "Todos" || category === p.category) &&
      (size === "Todos" || size === p.size) &&
      normalizar(`${p.name} ${p.shop}`).includes(normalizar(query)),
    ).sort((a, b) => sort === "menor" ? a.price - b.price :
      sort === "maior" ? b.price - a.price : a.id - b.id)
      .map((produto) => ({ ...produto }));
  }
}
