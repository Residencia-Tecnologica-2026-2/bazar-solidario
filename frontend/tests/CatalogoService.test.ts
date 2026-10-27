import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { CatalogoService } from "../src/services/CatalogoService.ts";
import { products } from "../src/data/store.ts";
/** Classe de teste: cada cenário usa uma instância independente. */
export class CatalogoServiceTest {
  catalogo = new CatalogoService(products);
  ["lista todas as peças por padrão"]() { return assert.equal(this.catalogo.buscar().length, 6);
  }
  ["busca nome ignorando acentos, caixa e espaços externos"]() {
    assert.deepEqual(this.catalogo.buscar({ query: "  TENIS " }).map(p => p.id), [4]);
  }
  ["busca pelo brechó"]() { return assert.deepEqual(this.catalogo.buscar({ query: "raizes" }).map(p => p.id), [1, 3]);
  }
  ["combina categoria, tamanho, favoritos e texto"]() {
    assert.deepEqual(this.catalogo.buscar({ category: "Roupas", size: "G", query: "jeans", favoritesOnly: true, favorites: [1, 3] }).map(p => p.id), [3]);
  }
  ["retorna vazio quando não há correspondência"]() { return assert.deepEqual(this.catalogo.buscar({ query: "inexistente" }), []);
  }
  ["favoritos vazios não exibem todo o catálogo"]() { return assert.deepEqual(this.catalogo.buscar({ favoritesOnly: true }), []);
  }
  ["ordena preços crescentes e decrescentes"]() {
    assert.deepEqual(this.catalogo.buscar({ sort: "menor" }).map(p => p.price), [35, 58, 65, 75, 89, 95]);
    assert.deepEqual(this.catalogo.buscar({ sort: "maior" }).map(p => p.price), [95, 89, 75, 65, 58, 35]);
  }
  ["mantém curadoria após ordenar e não permite modificar o catálogo pela resposta"]() {
    this.catalogo.buscar({ sort: "maior" })[0].name = "alterado";
    assert.deepEqual(this.catalogo.buscar().map(p => p.id), [1, 2, 3, 4, 5, 6]);
    assert.equal(this.catalogo.buscar({ query: "alterado" }).length, 0);
  }
  ["aceita catálogo vazio"]() { return assert.deepEqual(new CatalogoService([]).buscar(), []);
  }
}

describe("CatalogoService — busca e filtros do site", () => {
  for (const nome of Object.getOwnPropertyNames(CatalogoServiceTest.prototype)) {
    if (nome === "constructor") continue;
    it(nome, () => {
      const caso = new CatalogoServiceTest();
      return Reflect.apply(Reflect.get(caso, nome), caso, []);
    });
  }
});
