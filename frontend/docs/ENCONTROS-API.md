# API de Encontros Presenciais

Permite o cadastro, listagem e gerenciamento de encontros presenciais organizados pelas lojas.

## Endpoints

### 1. Listar Próximos Encontros
* **GET** `/encontros`
* **Acesso:** Público
* **Descrição:** Retorna encontros ativos a partir da data atual, ordenados por data e horário.

### 2. Detalhes do Encontro
* **GET** `/encontros/{id}`
* **Acesso:** Público

### 3. Cadastrar Encontro
* **POST** `/encontros`
* **Acesso:** Autenticado (`ROLE_LOJA`)
* **Body Exemplo:**
```json
{
  "titulo": "Feira de Brechós",
  "data": "2026-12-05",
  "horario": "14:00",
  "local": "Praça do Derby, Recife",
  "descricao": "Venha conferir peças vintage.",
  "lojaIds": []
}