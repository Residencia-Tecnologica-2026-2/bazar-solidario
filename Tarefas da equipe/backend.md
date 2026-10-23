# Tarefas da equipe: Bazar Solidário (backend)

Este documento tem duas partes: o **contexto comum**, que todos devem ler, e **uma tarefa por pessoa**. Cada tarefa pode ser copiada como uma issue do GitHub.

Base de referência: o PDF "Fluxo do usuário - Plataforma de brechós" (casos de uso UC01 a UC13).

---

# Contexto comum

## O que é o produto

Uma plataforma que hospeda vários brechós. Cada **loja** se cadastra, edita o perfil, publica peças com fotos e valor e cadastra encontros presenciais. O **cliente** navega sem cadastro, vê lojas, peças, encontros e localização, e faz o pedido pelo **WhatsApp** da loja. A plataforma **não processa pagamento, frete nem entrega**, e por isso não existem carrinho nem pedido.

## O que já existe (branch `feat/backend-anuncios`)

| Peça | Onde | Observação |
|---|---|---|
| Cadastro e login com JWT | `UsuarioController`, `AuthController`, `UsuarioService`, pacote `security` | `POST /usuarios` cadastra e devolve token. `POST /auth/login` devolve token. `GET /auth/me` mostra o logado |
| Filtro do token e rotas públicas | `JwtAuthFilter`, `SecurityConfig` | O token guarda o **e-mail** e a **role** |
| Anúncios (as "peças") | `Anuncio`, `AnuncioService`, `AnuncioController`, `AnuncioSpecs` | CRUD, filtros, `/anuncios/meus`, soft delete, só o dono altera |
| Imagens do anúncio | `Imagem` | Hoje só guarda URL, de 0 a 10 |
| Categorias | `Categoria`, `CategoriaSeeder` | 8 categorias iniciais |
| Documentação | `docs/ANUNCIOS-API.md`, `docs/AUTH-API.md` | Atualizar quando mudar uma rota |

## Como as coisas se relacionam

```
Usuario  1 ---- 1  Loja          (a loja é o perfil público de quem faz login)
Loja     1 ---- N  Anuncio       (peças à venda)
Anuncio  1 ---- N  Imagem
Anuncio  N ---- 1  Categoria
Encontro N ---- N  Loja          (brechós que participam do encontro)
Encontro N ---- 1  Loja          (loja que criou o encontro)
```

Quem faz login é sempre um `Usuario`. Para descobrir **quem está logado** dentro de um service:

- `UsuarioAutenticadoService.obter(authentication)` devolve o `Usuario` (busca pelo e-mail do token).
- A tarefa 1 cria `LojaAutenticadaService.obter(authentication)`, que devolve a `Loja` do usuário logado. **Todos os outros módulos usam esse service** em vez de buscar a loja por conta própria.

## Mudança de papéis

O PDF tem dois atores: cliente (sem conta) e loja (com conta). Por isso o enum `Role` passa de `CLIENTE, ADMIN` para **`LOJA, ADMIN`**. Quem se cadastra vira `LOJA`. O cliente só navega, sem login.

Para exigir a role numa rota, no `SecurityConfig`:

```java
.requestMatchers(HttpMethod.POST, "/encontros").hasRole("LOJA")
```

O filtro JWT já entrega a authority como `ROLE_LOJA`.

## Padrões do projeto (seguir)

- **Pastas:** `model`, `dto`, `repository`, `service`, `controller`, `exception`, `config`, `security`.
- **DTOs:** nunca devolver entidade na resposta. Isso evita vazar a senha do `Usuario`.
- **Dono:** o dono vem do token, **nunca** do corpo da requisição. Quem não é o dono recebe 403.
- **Erros:** `ResponseStatusException` nos services. Os handlers de erro (`AnuncioExceptionHandler`, `AuthExceptionHandler`) são **por controller** (`assignableTypes`). Um controller novo precisa ser adicionado a um handler ou ter o próprio. Senão a validação volta no formato padrão do Spring.
- **Rotas públicas:** declarar no `SecurityConfig`. **A ordem importa**: `/anuncios/meus` vem antes de `/anuncios/*`. Vale o mesmo para `/lojas/me` e `/lojas/*`.
- **Testes:** `AuthControllerTest` é o modelo. Ele usa o `SecurityConfig` real, então testa de verdade se a rota é pública ou protegida.
- **Pronto quando:** `./mvnw clean test` passa, há testes novos, e `docs/` e a descrição da PR foram atualizados.

## Como rodar com Docker

**Pré-requisito:** Docker Desktop (ou Docker Engine) instalado e aberto. Cada pessoa sobe o **próprio banco local**. O banco não é compartilhado.

| Para quê | Comando |
|---|---|
| Subir banco e API | `docker compose up --build` |
| Subir em segundo plano | `docker compose up --build -d` |
| Recompilar só a API depois de mudar código | `docker compose up --build -d api` |
| Ver os logs da API | `docker compose logs -f api` |
| Parar tudo (mantém o banco) | `docker compose down` |
| Parar e **apagar o banco** | `docker compose down -v` |
| Entrar no banco pelo terminal | `docker exec -it bazar-db psql -U postgres -d bazar_solidario` |

A API fica em `http://localhost:8080`. Credenciais do banco: banco `bazar_solidario`, usuário `postgres`, senha `postgres`.

**Para usar uma ferramenta gráfica (DBeaver, pgAdmin):** adicione na seção `db` do `docker-compose.yml`, **sem fazer commit**:

```yaml
    ports:
      - "5433:5432"
```

Depois conecte em `localhost:5433`. Usei 5433 para não conflitar com um Postgres que já esteja na porta 5432.

**Atenção, quando precisar de `docker compose down -v`:** o projeto usa `ddl-auto=update`. O Hibernate cria tabelas e colunas novas sozinho, mas **não altera o que já existe**. Se você mudar um **enum** (por exemplo `Role` para `LOJA`, ou um status novo), mudar o nome ou o tipo de uma coluna, o banco antigo continua com a regra antiga e dá erro de constraint. Nesses casos, apague o banco com `down -v` e suba de novo.

**Outras observações:**
- O Docker compila com `-DskipTests`. Para rodar os testes, use `./mvnw clean test` fora do Docker (precisa de Java 21).
- Se a porta 8080 estiver ocupada, troque `"8080:8080"` por `"8081:8080"` no compose.
- Sem Docker, `./mvnw spring-boot:run` exige um Postgres local em `localhost:5432`.

## Swagger

A **Ester** vai configurar o Swagger. Combinados para quando ele estiver pronto:

- Ele fica em `http://localhost:8080/swagger-ui.html`.
- Para liberar no `SecurityConfig`: `/swagger-ui/**`, `/swagger-ui.html` e `/v3/api-docs/**`.
- O botão **Authorize** recebe o token (esquema `bearerAuth`), para testar as rotas protegidas.
- Confirmar uma versão do springdoc compatível com o Spring Boot 4.1.
- Cada tarefa, ao terminar, anota o **controller** com `@Tag` e cada rota com `@Operation(summary = "...")`, para a documentação ficar legível.

## Combinados para não ter conflito

1. **Ordem de merge:** primeiro a autenticação (já está na `feat/backend-anuncios`), depois a Loja, depois o resto. Quem depende da Loja faz rebase depois.
2. **Branch por tarefa**, criada a partir da branch base. Commits pequenos.
3. **Cada um mexe nos próprios pacotes e arquivos.** Evitem editar arquivos de outra tarefa.
4. **`SecurityConfig` é o único arquivo que todos vão tocar.** Adicionem só as suas linhas de rotas, no mesmo bloco, e avisem no grupo.
5. **Hoje, em até 30 minutos:** o Mateus sobe a entidade `Loja` só com os campos, ainda sem rotas, para os outros apontarem para ela.

---

# Tarefa 1: Lojas (Mateus)

**Casos de uso:** UC08 Cadastrar loja, UC10 Editar perfil da loja, UC04 Navegar pelas lojas, UC03 Ver localização dos brechós.

## Objetivo

Dar um perfil público a cada conta de login. Esta é a **base das outras tarefas**: os anúncios passam a pertencer a uma loja, os encontros listam lojas participantes e a tela principal lista lojas.

## Como se relaciona com o que existe

- Hoje o cadastro (`POST /usuarios`, no `UsuarioService`) cria só um `Usuario`. Passa a criar **`Usuario` + `Loja` na mesma transação**.
- A `Loja` fica ligada ao `Usuario` por `@OneToOne`. O login continua igual.
- O `Role` muda para `LOJA, ADMIN`. O token passa a carregar `LOJA`.
- A **Ester** vai trocar o dono do anúncio de `Usuario` para `Loja`. A **Williane** e o **Ruan** usam a `Loja` que você entregar.

## O que fazer

- [ ] **Primeiro, em até 30 minutos:** entidade `Loja` e `LojaRepository`, mesmo sem rotas, e subir para os outros.
- [ ] `Loja` com: `id`, `usuario` (OneToOne, único), `nome` (obrigatório, até 100), `logoUrl`, `descricao` (até 1000), `endereco` (até 255), `latitude` e `longitude` (opcionais, para o mapa), `whatsapp` (obrigatório).
- [ ] Validar o `whatsapp`: só dígitos, com DDI e DDD, de 10 a 13 números.
- [ ] Ajustar `RegistroRequest` e `UsuarioService.cadastrar` para receber o `whatsapp` e criar a loja junto, com o nome da loja igual ao `nome` do cadastro.
- [ ] Trocar `Role.CLIENTE` por `Role.LOJA` (e ajustar o `UsuarioServiceTest` e o `docs/AUTH-API.md`).
- [ ] Criar `LojaAutenticadaService.obter(Authentication)`, que devolve a `Loja` do logado. Os outros vão usar.
- [ ] Rotas abaixo, com DTOs (`LojaResponse` completo e `LojaResumoResponse` com `id`, `nome` e `logoUrl`).
- [ ] Liberar as rotas públicas no `SecurityConfig` (`/lojas/me` **antes** de `/lojas/*`).
- [ ] Testes de service e de controller, e documentação em `docs/LOJAS-API.md`.

## Rotas

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| POST | `/usuarios` | Público | Cadastra usuário e loja e devolve o token (agora pede o WhatsApp) |
| GET | `/lojas` | Público | Lista lojas (nome, logo, endereço), paginada |
| GET | `/lojas/{id}` | Público | Perfil de uma loja |
| GET | `/lojas/me` | Logado | Perfil da própria loja |
| PUT | `/lojas/me` | Logado | Edita o próprio perfil |

## Regras

- A loja **só edita o próprio perfil**, e o dono vem do token, nunca do corpo da requisição.
- E-mail repetido retorna 409. Campos obrigatórios vazios retornam 400 com a mensagem por campo.
- Loja sem endereço: a resposta vem com o campo vazio (o frontend mostra só o perfil, sem mapa).

## Pronto quando

- Cadastro cria usuário e loja e devolve token com role `LOJA`.
- `GET /lojas` funciona sem token e `PUT /lojas/me` sem token retorna 401.
- `LojaAutenticadaService` está disponível para os outros.

## Teste manual

```
curl -X POST http://localhost:8080/usuarios -H "Content-Type: application/json" \
  -d '{"nome":"Brechó da Ana","email":"ana@email.com","senha":"123456","whatsapp":"5581999999999"}'
curl http://localhost:8080/lojas
curl -X PUT http://localhost:8080/lojas/me -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" \
  -d '{"nome":"Brechó da Ana","descricao":"Peças vintage","endereco":"Rua X, 10, Recife","whatsapp":"5581999999999"}'
```

---

# Tarefa 2: Encontros presenciais (Williane)

**Casos de uso:** UC13 Cadastrar encontro presencial, UC02 Ver encontros presenciais.

## Objetivo

Permitir que lojas divulguem encontros presenciais e que os clientes vejam data, horário, local e os brechós que participam. Os encontros aparecem no **topo da tela principal**.

## Como se relaciona com o que existe

- É um módulo **novo e independente**: crie as classes nos pacotes existentes (`model`, `dto`, `repository`, `service`, `controller`).
- Depende da **`Loja`** (tarefa do Mateus) para o criador e para os participantes. Para não esperar, comece só com o que não depende dela (entidade, validações de data, rotas de leitura) e conecte a `Loja` quando ela subir.
- O criador vem do token, via `LojaAutenticadaService.obter(authentication)` (criado pelo Mateus).
- O **Ruan** usa o seu service na rota da tela principal (`/home`). Exponha um método como `listarProximos(int quantidade)`.

## O que fazer

- [ ] Entidade `Encontro`: `id`, `titulo`, `data` (`LocalDate`), `horario` (`LocalTime`), `local` (até 255), `descricao` (opcional), `status` (`ATIVO` ou `CANCELADO`), `criador` (ManyToOne `Loja`) e `participantes` (ManyToMany `Loja`, tabela `encontro_lojas`).
- [ ] `EncontroRequest` com `titulo`, `data`, `horario`, `local` e `lojaIds` (outras lojas participantes). O criador entra automaticamente como participante.
- [ ] `EncontroResponse` com os participantes em formato resumido (`id`, `nome` e `logoUrl`).
- [ ] Rotas abaixo e liberação no `SecurityConfig`.
- [ ] Um `EncontroExceptionHandler` próprio (ou incluir o controller num handler existente).
- [ ] Testes e `docs/ENCONTROS-API.md`.

## Rotas

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| GET | `/encontros` | Público | Lista os próximos encontros ativos, por data |
| GET | `/encontros/{id}` | Público | Detalhe: data, horário, local e brechós participantes |
| POST | `/encontros` | Role LOJA | Cadastra um encontro |
| PUT | `/encontros/{id}` | Só o criador | Edita o encontro |
| PATCH | `/encontros/{id}/cancelar` | Só o criador | Cancela o encontro |

## Regras

- **Data no passado** não é aceita (400).
- Encontro **já ocorrido** ou **cancelado** deixa de aparecer na listagem pública.
- Só o criador edita ou cancela. Outra loja recebe 403.
- Cancelar é soft delete (muda o status), como nos anúncios.
- Sem encontros, a listagem devolve lista vazia. A mensagem "Nenhum encontro marcado por enquanto" é do frontend.

## Pronto quando

- Uma loja logada cria um encontro e ele aparece em `GET /encontros` sem token.
- Data no passado retorna 400 e outra loja editando retorna 403.
- Cancelado ou passado não aparece na lista.

## Teste manual

```
curl -X POST http://localhost:8080/encontros -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" \
  -d '{"titulo":"Feira de Brechós","data":"2026-12-05","horario":"14:00","local":"Praça do Derby, Recife","lojaIds":[]}'
curl http://localhost:8080/encontros
```

---

# Tarefa 3: Produtos e pedido pelo WhatsApp (Ester)

**Casos de uso:** UC11 Cadastrar produto, UC12 Editar, vender ou remover produto, UC05 Ver peças de uma loja, UC07 Pedir pelo WhatsApp.

## Objetivo

Adaptar o módulo de anúncios (que já existe) ao fluxo do PDF: o dono do anúncio passa a ser a **loja**, a peça pode ser marcada como vendida, toda peça tem foto, dá para ver as peças de uma loja e o cliente pede pelo WhatsApp.

## Como se relaciona com o que existe

- É a evolução do que você já fez: `Anuncio`, `AnuncioService`, `AnuncioSpecs`, `AnuncioController`.
- Hoje `Anuncio.vendedor` aponta para `Usuario`. Passa a apontar para **`Loja`** (tarefa do Mateus). `UsuarioAutenticadoService` dá lugar ao `LojaAutenticadaService` nas regras de dono.
- O **Ruan** vai mudar a validação de URL das imagens para aceitar também os caminhos de upload (`/uploads/...`). Combinem isso.
- O **Mateus** entrega o `whatsapp` e o nome da loja usados no link.

## O que fazer

- [ ] Trocar `Anuncio.vendedor` (`Usuario`) por `Anuncio.loja` (`Loja`). A coluna `vendedor_id` vira `loja_id`, então **apague o banco** (`docker compose down -v`).
- [ ] Atualizar `AnuncioResponse` (de `vendedor` para `loja`, com `LojaResumoResponse`), `AnuncioService`, `AnuncioSpecs` e os testes (`AnuncioServiceTest`, `AnuncioControllerTest`).
- [ ] **Marcar como vendido:** nova rota `PATCH /anuncios/{id}/vendido`. Só o dono, só de `ATIVO` para `VENDIDO`. Vendido não pode ser reativado (409, regra que já existe).
- [ ] **Foto obrigatória:** exigir pelo menos 1 imagem ao publicar (`@Size(min = 1, max = 10)`), com mensagem clara.
- [ ] **Peças de uma loja:** novo filtro `lojaId` em `GET /anuncios`, dentro do `AnuncioSpecs`.
- [ ] **Link do WhatsApp:** `AnuncioResponse` ganha `whatsappLink`: `https://wa.me/{whatsapp}?text={mensagem codificada}`, com a mensagem pronta contendo **loja, peça e valor**. Se a loja não tiver número, o campo vem `null` (o botão fica indisponível no frontend).
- [ ] Atualizar `docs/ANUNCIOS-API.md` e a descrição da PR (os itens de pedido e carrinho saem).
- [ ] Opcional: status `RASCUNHO` para salvar sem publicar (muda o enum, então apague o banco).

## Rotas (o que muda)

| Método | Rota | Mudança |
|---|---|---|
| GET | `/anuncios` | Novo filtro `lojaId` |
| GET | `/anuncios/{id}` | Resposta com `loja` e `whatsappLink` |
| PATCH | `/anuncios/{id}/vendido` | **Nova**, só o dono |
| POST / PUT | `/anuncios` | Exigem pelo menos 1 imagem |

## Regras

- "Remover" no PDF continua sendo **desativar** (soft delete). Só o dono, e peça vendida não volta.
- Peça vendida deixa de aparecer na lista pública, porque a listagem só mostra `ATIVO`.
- Mensagem do WhatsApp: algo como `Olá! Tenho interesse na peça "Camiseta" (R$ 25,00) da loja Brechó da Ana.`

## Pronto quando

- Anúncio criado por uma loja mostra a loja na resposta, e o `whatsappLink` abre o WhatsApp com a mensagem certa.
- `PATCH .../vendido` tira a peça da lista pública.
- Criar anúncio sem imagem retorna 400.
- `GET /anuncios?lojaId=1` mostra só as peças daquela loja.

## Teste manual

```
curl -X PATCH http://localhost:8080/anuncios/1/vendido -H "Authorization: Bearer SEU_TOKEN"
curl "http://localhost:8080/anuncios?lojaId=1"
curl http://localhost:8080/anuncios/1
```

---

# Tarefa 4: Tela principal, upload de fotos e testes (Ruan)

**Casos de uso:** UC01 Ver tela principal, apoio a UC03, UC10 e UC11 (fotos).

## Objetivo

Juntar os módulos na tela principal, permitir enviar fotos de verdade (hoje só aceita URL) e dar segurança ao time com testes que usam um banco.

## Como se relaciona com o que existe

- A tela principal **usa os services** dos outros: encontros da **Williane** e lojas do **Mateus**. Chame os services deles, não os repositories.
- O upload serve ao **logo da loja** (Mateus) e às **fotos dos anúncios** (Ester). Hoje `AnuncioRequest.imagens` aceita só URLs que começam com `http://` ou `https://`. Combinem com a Ester a mudança para aceitar também `/uploads/...`.
- Os testes com banco cobrem as consultas de filtro do `AnuncioSpecs`, que hoje **nenhum teste executa** num banco de verdade.

## O que fazer

- [ ] **Tela principal:** `GET /home` (público) devolvendo `encontros` (os próximos, por exemplo 5) e `lojas` (resumo com `nome`, `logoUrl`, `endereco`, `latitude` e `longitude`, por exemplo as 12 primeiras). Listas vazias quando não houver nada. Enquanto os outros não entregam, use uma interface ou dados de exemplo.
- [ ] **Upload de fotos:** `POST /uploads` (logado), multipart com o campo `arquivo`. Aceita `jpg`, `png` e `webp`, até 5 MB, e recusa o resto com mensagem clara. Salva numa pasta `uploads/` e devolve `{ "url": "/uploads/{uuid}.jpg" }`.
- [ ] **Servir as fotos:** configurar `GET /uploads/**` como público (um `WebMvcConfigurer` com resource handler).
- [ ] **Docker:** no `docker-compose.yml`, na seção `api`, um volume para a pasta de uploads, para as fotos não sumirem ao recriar o container.
- [ ] **Banco de testes:** dependência do H2 no escopo de teste, `application-test.properties`, e corrigir o `BazarSolidarioApplicationTests` (que hoje exige o Postgres no ar).
- [ ] **Testes de integração** com `@SpringBootTest` e perfil de teste: cadastro, login, criar anúncio, listar com filtros e acesso sem token.
- [ ] Atualizar `docs/` e o README com o passo a passo de rodar e testar.

## Rotas

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| GET | `/home` | Público | Próximos encontros e lojas para a tela principal |
| POST | `/uploads` | Logado | Envia uma foto e devolve a URL |
| GET | `/uploads/{arquivo}` | Público | Serve a foto |

## Regras

- Validar tipo e tamanho do arquivo **no servidor**. Não confiar só na extensão.
- Dar um nome aleatório ao arquivo (`uuid`), para não sobrescrever e não aceitar nomes enviados pelo cliente.
- A tela principal não exige login, e a API não devolve mensagens de tela (só listas vazias).

## Pronto quando

- `GET /home` responde sem token, mesmo sem dados.
- Uma foto enviada abre pela URL devolvida, e continua lá depois de `docker compose up --build -d api`.
- `./mvnw clean test` roda os testes de integração com H2, sem precisar de Postgres.

## Teste manual

```
curl http://localhost:8080/home
curl -X POST http://localhost:8080/uploads -H "Authorization: Bearer SEU_TOKEN" -F "arquivo=@foto.jpg"
```
