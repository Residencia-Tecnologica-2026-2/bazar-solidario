# Tela principal

GET /home é público e reúne os dados necessários para a tela inicial.

```json
{
  "encontros": [],
  "lojas": []
}
```

- encontros: até cinco encontros ativos a partir da data atual, em ordem de data e horário.
- lojas: até doze lojas, com id, nome, logoUrl, endereco, latitude e longitude.
- Quando não há dados, cada lista é devolvida vazia.

As listas usam os serviços de encontros e lojas para manter as regras de cada módulo centralizadas.
