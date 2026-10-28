# Configuração e implantação

## Configuração da API

Fora do perfil `local`, configure no ambiente (não no código):

| Variável | Uso |
|---|---|
| DB_URL | JDBC PostgreSQL: `jdbc:postgresql://servidor:5432/bazar_solidario` |
| DB_USER / DB_PASSWORD | Usuário e senha exclusivos da aplicação |
| JWT_SECRET | Chave aleatória de pelo menos 32 caracteres; use 48 bytes aleatórios em Base64 |
| JWT_EXPIRATION_MS | Opcional; padrão 28800000 (8 horas) |
| APP_UPLOADS_DIRECTORY | Diretório persistente de fotos |
| PORT | Porta da API; padrão 8080 |
| DDL_AUTO | Padrão validate. Inicialização controlada pode usar update |

Nunca use `VITE_` para segredos. Variáveis com esse prefixo são públicas no bundle. Frontend: `VITE_API_BASE_URL=/api`; no desenvolvimento `API_PROXY_TARGET=http://localhost:8080` é apenas destino do proxy.

## Docker Compose (PostgreSQL + API)

1. Copie `backend/.env.example` para `backend/.env`, preencha DB_USER, DB_PASSWORD e JWT_SECRET com valores exclusivos.
2. Execute `docker compose up --build -d` em backend.
3. O Compose usa volumes `bazar-dados` e `bazar-uploads`. Banco e API são publicados apenas no loopback da máquina. Não use `down -v` se houver dados a preservar.
4. DDL_AUTO=update é destinado à criação inicial de ambiente novo. Em banco existente, faça backup e aplique migrações de esquema revisadas antes de trocar para validate. Esta entrega não instala um gestor de migrações nem comprova atualização de um banco PostgreSQL em produção.
5. Valide cadastro, publicação com foto, pedido, aceite, cancelamento e reinício antes de abrir acesso externo. Docker não foi executado nesta máquina.

## Frontend e proxy

Execute `npm ci` e `npm run build` em frontend. Publique `frontend/dist` num servidor estático com fallback para index.html. O proxy HTTPS deve encaminhar `/api/*` à API removendo `/api`. Um exemplo está em `deploy/nginx.conf`; ajuste caminhos e upstream ao ambiente. Para Vercel, configure uma rewrite `/api/:path*` para o endereço HTTPS da API e fallback do SPA. A API Spring Boot e seus volumes exigem um serviço próprio; não são publicados junto do bundle estático.

Configure TLS no ingresso da infraestrutura. Não exponha PostgreSQL à internet. Preserve e copie os volumes nas rotinas de backup. Rotacionar a chave JWT encerra as sessões existentes. `.env`, a chave local, banco e fotos não entram no ZIP nem no Git.

## Verificações

- `npm test` e `npm run build` em frontend.
- `mvnw.cmd test` em backend: integração HTTP autenticada com JPA/H2, estoque concorrente, repetição de pedido, acesso por proprietário, preços e arredondamento.
- Faça um teste de integração em PostgreSQL dedicado antes da implantação. Não aponte testes que recriam esquema para um banco com dados reais.
- Reinicie a API e confira que pedidos e fotos persistem. Confirme também a restauração de um backup.

## Operação do pedido manual

O brechó aceita o pedido em Perfil → Pedidos recebidos. A equipe combina pagamento e retirada/entrega fora do sistema e registra a conciliação de 10% para ISAC e 90% para o brechó no processo administrativo. “Aceito” não significa “pago”. Pedidos pendentes precisam de acompanhamento: não há expiração automática nem rotina bancária. Não solicite dados de cartão pelo aplicativo.
