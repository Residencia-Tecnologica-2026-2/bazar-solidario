import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { SacolaService } from "../src/services/SacolaService.ts";
import { products } from "../src/data/store.ts";
/** Classe de teste: cada cenário usa uma instância independente. */
export class SacolaServiceTest {
  sacola = new SacolaService(products);
  ["adiciona uma peça sem modificar o estado anterior"]() {
    const anterior = [1];
    assert.deepEqual(this.sacola.adicionar(anterior, 2), [1, 2]);
    assert.deepEqual(anterior, [1]);
  }
  ["não duplica uma peça única"]() { return assert.deepEqual(this.sacola.adicionar([1], 1), [1]);
  }
  ["rejeita peça inexistente"]() { return assert.throws(() => this.sacola.adicionar([], 999), /não encontrada/);
  }
  ["remove somente a peça escolhida"]() { return assert.deepEqual(this.sacola.remover([1, 2], 1), [2]);
  }
  ["remover ID ausente mantém os itens válidos"]() { return assert.deepEqual(this.sacola.remover([1], 999), [1]);
  }
  ["restaura IDs válidos e descarta duplicatas e valores inválidos"]() {
    assert.deepEqual(this.sacola.restaurar('[1,1,"2",2,999,null,{},-1]'), [1, 2]);
  }
  ["recupera armazenamento vazio, corrompido ou não-array"]() {
    for (const json of [null, "", "{erro", "null", "{}", '"texto"']) assert.deepEqual(this.sacola.restaurar(json), []);
  }
  ["calcula subtotal apenas de peças válidas, sem duplicatas"]() { return assert.equal(this.sacola.subtotal([1, 2, 2, 999]), 154);
  }
  ["calcula valores monetários em centavos"]() {
    const decimal = new SacolaService([{ ...products[0], price: 0.1 }, { ...products[1], price: 0.2 }]);
    assert.equal(decimal.subtotal([1, 2]), 0.3);
  }
  ["sacola vazia tem subtotal zero"]() { return assert.equal(this.sacola.subtotal([]), 0);
  }
  ["itens retornados não permitem alterar preços internos"]() {
    this.sacola.itens([1])[0].price = 0;
    assert.equal(this.sacola.subtotal([1]), 65);
  }
}

describe("SacolaService — peças únicas e subtotal", () => {
  for (const nome of Object.getOwnPropertyNames(SacolaServiceTest.prototype)) {
    if (nome === "constructor") continue;
    it(nome, () => {
      const caso = new SacolaServiceTest();
      return Reflect.apply(Reflect.get(caso, nome), caso, []);
    });
  }
});
