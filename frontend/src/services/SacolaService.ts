import type { Product } from "../data/store.ts";

/** Peças de brechó são únicas: cada ID pode aparecer uma única vez. */
export class SacolaService {
  private readonly produtos: readonly Product[];

  constructor(produtos: readonly Product[]) {
    this.produtos = produtos.map((produto) => ({ ...produto }));
  }

  normalizar(ids: unknown): number[] {
    if (!Array.isArray(ids)) return [];
    return [...new Set(ids.filter((id): id is number =>
      Number.isSafeInteger(id) && this.produtos.some((p) => p.id === id)))];
  }

  restaurar(json: string | null): number[] {
    try { return this.normalizar(JSON.parse(json ?? "[]")); }
    catch { return []; }
  }

  adicionar(ids: readonly number[], id: number): number[] {
    if (!this.produtos.some((p) => p.id === id)) throw new Error("Peça não encontrada.");
    return this.normalizar([...ids, id]);
  }

  remover(ids: readonly number[], id: number): number[] {
    return this.normalizar(ids).filter((item) => item !== id);
  }

  itens(ids: readonly number[]): Product[] {
    const validos = this.normalizar(ids);
    return this.produtos.filter((p) => validos.includes(p.id)).map((p) => ({ ...p }));
  }

  subtotal(ids: readonly number[]): number {
    return this.itens(ids).reduce((centavos, p) => centavos + Math.round(p.price * 100), 0) / 100;
  }
}
