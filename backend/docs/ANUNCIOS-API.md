# Módulo Anúncios e Catálogo — API

Base: `http://localhost:8080` · Formato: JSON · Padrão de rotas do projeto (sem prefixo `/api`).

## Resumo

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/anuncios` | Público | Lista anúncios ATIVOS (busca, filtros, ordenação, paginação) |
| GET | `/anuncios/{id}` | Público* | Consulta um anúncio |
| GET | `/anuncios/meus` | Autenticado | Anúncios do usuário logado (inclui inativos) |
| POST | `/anuncios` | Autenticado | Cadastra anúncio (vendedor = usuário logado) |
| PUT | `/anuncios/{id}` | Só o dono | Edita anúncio |
| PATCH | `/anuncios/{id}/desativar` | Só o dono | Desativa (status `INATIVO`, não apaga) |
| PATCH | `/anuncios/{id}/ativar` | Só o dono | Reativa |
| GET | `/categorias` | Público | Lista categorias (ordem alfabética) |

\* Anúncio `INATIVO` só é visível ao próprio dono; para os demais responde 404.

## GET /anuncios — parâmetros (todos opcionais)

| Parâmetro | Padrão | Descrição |
|---|---|---|
| `titulo` | — | Contém o texto, sem diferenciar maiúsculas |
| `categoriaId` | — | Filtra pela categoria |
| `precoMin` / `precoMax` | — | Faixa de preço (inclusiva). `precoMin > precoMax` → 400 |
| `ordenarPor` | `dataCriacao` | `preco`, `dataCriacao` ou `titulo` (outro valor cai no padrão) |
| `direcao` | `desc` | `asc` ou `desc` |
| `page` | `0` | Página (começa em 0) |
| `size` | `12` | Itens por página (mín. 1, máx. 50) |

Exemplo: `GET /anuncios?titulo=camiseta&categoriaId=1&precoMin=10&precoMax=80&ordenarPor=preco&direcao=asc&page=0&size=12`

Resposta `200`:
```json
{
  "content": [
    {
      "id": 5,
      "titulo": "Camiseta",
      "descricao": "Camiseta de algodão",
      "preco": 25.00,
      "estoque": 3,
      "status": "ATIVO",
      "dataCriacao": "2026-10-21T18:30:00",
      "categoria": { "id": 1, "nome": "Roupas", "descricao": null },
      "vendedor": { "id": 1, "nome": "Dona Maria" },
      "imagens": ["https://exemplo.com/a.jpg"]
    }
  ],
  "page": 0, "size": 12, "totalElements": 1, "totalPages": 1, "first": true, "last": true
}
```

## POST /anuncios e PUT /anuncios/{id}

Não existe campo de vendedor: o dono é sempre o usuário autenticado.

```json
{
  "titulo": "Camiseta",
  "descricao": "Camiseta de algodão",
  "preco": 25.00,
  "estoque": 3,
  "categoriaId": 1,
  "imagens": ["https://exemplo.com/a.jpg"]
}
```

| Campo | Regra |
|---|---|
| `titulo` | Obrigatório, até 150 caracteres |
| `descricao` | Opcional, até 2000 caracteres |
| `preco` | Obrigatório, **maior que zero**, até 8 inteiros e 2 decimais |
| `estoque` | Obrigatório, **não negativo** (0 é permitido) |
| `categoriaId` | Obrigatório e **deve existir** (senão 400 "Categoria inválida") |
| `imagens` | Opcional; até 10 URLs `http(s)://` (upload real fica para a 2ª etapa) |

No `PUT`: `imagens` ausente/`null` mantém as atuais; lista informada (mesmo vazia) substitui todas.
`POST` responde `201` com cabeçalho `Location: /anuncios/{id}`.

## Erros

| Status | Quando |
|---|---|
| 400 | Validação (corpo abaixo), categoria inexistente, `precoMin > precoMax` |
| 401 | Não autenticado em rota protegida |
| 403 | Usuário não é o dono (editar/desativar/ativar) |
| 404 | Anúncio inexistente (ou inativo para quem não é o dono) |
| 409 | Tentar ativar/desativar anúncio `VENDIDO` |

```json
{ "status": 400, "erro": "Dados inválidos", "campos": { "preco": "O preço deve ser maior que zero" } }
```

Os demais erros usam o formato padrão do Spring (`ResponseStatusException`).

## Decisões e pontos a alinhar com o time

1. **Segurança:** o `SecurityConfig` não foi alterado. Para as rotas públicas funcionarem, quem cuida da autenticação deve liberar
   (com `/anuncios/meus` **antes** da regra pública, pois `/anuncios/*` também casaria com `/meus`):
   ```java
   .requestMatchers("/usuarios").permitAll()
   .requestMatchers("/anuncios/meus").authenticated()
   .requestMatchers(HttpMethod.GET, "/anuncios", "/anuncios/*", "/categorias").permitAll()
   .anyRequest().authenticated()
   ```
2. **Identificação do usuário:** `UsuarioAutenticadoService` assume `authentication.getName()` = **e-mail**. Se a autenticação usar outra coisa (ex.: id no JWT), só essa classe muda.
3. **`VendedorRepository`** foi criado só para ter `findByEmail` sem editar o `UsuarioRepository`. Se o time adicionar esse método lá, é só remover.
4. **Categorias:** `CategoriaSeeder` cadastra 8 categorias iniciais se não existirem. Definir depois quem gerencia categorias (ex.: ADMIN).
5. **`VENDIDO`:** quem altera esse status é o módulo de pedidos (fora do escopo). Aqui ele só bloqueia ativar/desativar.
6. **Desativar = soft delete**, porque `ItemPedido` referencia `Anuncio`.
7. **Banco:** com `ddl-auto=update`, as tabelas `anuncios`, `categorias` e `imagens` são criadas por quem subir primeiro.

