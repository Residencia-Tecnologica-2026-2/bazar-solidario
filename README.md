# Bazar Solidário (backend)

API REST de uma plataforma que hospeda vários brechós. Cada **loja** se cadastra, edita o perfil, publica peças com fotos e valor e divulga encontros presenciais. O **cliente** navega sem cadastro, vê lojas, peças, encontros e localização, e faz o pedido pelo **WhatsApp** da loja.

A plataforma **não processa pagamento, frete nem entrega**: o pedido e a conversa acontecem somente pelo WhatsApp.

**Tecnologias:** Java 21, Spring Boot 4, Spring Data JPA, Spring Security (JWT), PostgreSQL e Docker.

---

## Como rodar

Pré-requisito: Docker instalado e aberto.

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080`, junto com o PostgreSQL.

| Para quê | Comando |
|---|---|
| Subir em segundo plano | `docker compose up --build -d` |
| Ver os logs da API | `docker compose logs -f api` |
| Parar tudo | `docker compose down` |
| Parar e apagar o banco | `docker compose down -v` |
| Entrar no banco | `docker exec -it bazar-db psql -U postgres -d bazar_solidario` |
| Rodar os testes (precisa de Java 21) | `./mvnw clean test` |

Credenciais do banco local: banco `bazar_solidario`, usuário `postgres`, senha `postgres`.

> **Atenção:** o projeto usa `ddl-auto=update`, que não altera tabelas já existentes. Se você mudar um enum ou o nome de uma coluna, rode `docker compose down -v` para recriar o banco.

Em produção, defina a variável de ambiente `JWT_SECRET` (mínimo 32 caracteres).

---

## Como funciona a autenticação

1. `POST /usuarios` cadastra e devolve um **token JWT**.
2. `POST /auth/login` devolve um token novo a partir de e-mail e senha.
3. Nas rotas protegidas, envie o cabeçalho `Authorization: Bearer <token>`.
4. Sem token, ou com token inválido ou expirado, a resposta é `401`.

Detalhes em [docs/AUTH-API.md](docs/AUTH-API.md). As rotas de anúncios estão em [docs/ANUNCIOS-API.md](docs/ANUNCIOS-API.md).

---

## Situação do projeto

Base de referência: documento "Fluxo do usuário - Plataforma de brechós" (casos de uso UC01 a UC13).

### Já está pronto

- [x] Cadastro e login com JWT, senha com BCrypt (UC09)
- [x] Filtro de token e rotas públicas e protegidas
- [x] Anúncios: criar, consultar, listar, editar, desativar e reativar (UC11, UC12 em parte)
- [x] Listagem pública com busca por título, filtro por categoria e preço, ordenação e paginação
- [x] Detalhe de um anúncio (UC06)
- [x] "Meus anúncios" do usuário logado
- [x] Só o dono edita ou desativa o anúncio
- [x] Categorias com 8 valores iniciais
- [x] Docker (banco e API com um comando)
- [x] Testes de service e de controller

### Falta fazer

| Parte | Casos de uso | O que falta | Responsável |
|---|---|---|---|
| **Lojas** | UC08, UC10, UC04, UC03 | Entidade `Loja` (nome, logo, descrição, endereço, WhatsApp), cadastro pedindo o WhatsApp, `GET /lojas`, `GET /lojas/{id}`, `GET` e `PUT /lojas/me`, role `LOJA` | Mateus |
| **Encontros presenciais** | UC13, UC02 | Entidade `Encontro` (data, horário, local, lojas participantes), cadastrar, editar, cancelar, listar os próximos e ver o detalhe | Williane |
| **Produtos e WhatsApp** | UC11, UC12, UC05, UC07 | Anúncio pertencer à loja, marcar como vendido, exigir foto, filtrar peças por loja, link `wa.me` com mensagem pronta | Ester |
| **Tela principal, fotos e testes** | UC01 | `GET /home`, upload de fotos, banco de testes (H2) e testes de integração | Ruan |
| **Documentação** | todos | Configurar o Swagger e anotar as rotas | Ester |

### Pendências menores

- [ ] "Senha esquecida" no login (precisa enviar e-mail)
- [ ] Rascunho de produto (opcional no PDF)
- [ ] Alguém gerenciar as categorias depois (por exemplo, o ADMIN)
- [ ] Remover o `VendedorRepository`, que fica sem uso depois que o anúncio passar a pertencer à `Loja`
- [ ] Teste que execute as consultas de filtro num banco de verdade

### Mudanças de rumo em relação ao desenho inicial

- Não há **carrinho, pedido nem pagamento**. O pedido é feito pelo WhatsApp da loja.
- Quem se cadastra é uma **loja**. O cliente só navega, sem login. O papel `CLIENTE` dá lugar a `LOJA`.
- Marcar uma peça como **vendida** passa a ser ação da própria loja.

---

## Rotas atuais

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| POST | `/usuarios` | Público | Cadastra e devolve o token |
| POST | `/auth/login` | Público | E-mail e senha, devolve o token |
| GET | `/auth/me` | Logado | Dados de quem está logado |
| GET | `/anuncios` | Público | Lista anúncios ativos, com filtros e paginação |
| GET | `/anuncios/{id}` | Público | Consulta um anúncio |
| GET | `/anuncios/meus` | Logado | Anúncios do usuário, inclusive inativos |
| POST | `/anuncios` | Logado | Cria um anúncio |
| PUT | `/anuncios/{id}` | Só o dono | Edita o anúncio |
| PATCH | `/anuncios/{id}/desativar` | Só o dono | Desativa o anúncio |
| PATCH | `/anuncios/{id}/ativar` | Só o dono | Reativa o anúncio |
| GET | `/categorias` | Público | Lista as categorias |

---

## Estrutura do código

```
Cliente -> Filtro JWT -> SecurityConfig -> Controller -> Service -> Repository -> PostgreSQL
```

| Pasta | Responsabilidade |
|---|---|
| `model` | Entidades JPA (as tabelas) |
| `dto` | Dados que entram e saem da API (nunca devolver a entidade, para a senha não vazar) |
| `repository` | Consultas ao banco |
| `service` | Regras de negócio |
| `controller` | Rotas HTTP |
| `security` | Geração e validação do token e filtro de autenticação |
| `exception` | Mensagens de erro em JSON |
| `config` | Segurança e dados iniciais |
