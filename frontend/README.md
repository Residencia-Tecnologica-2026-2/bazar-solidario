# ISAC Brechó - Front-end

Aplicativo mobile web em React, TypeScript e Vite, com marca ISAC, catálogo, filtros, favoritos, detalhes, sacola, encontros e cadastro.

## Executar

Requer Node.js 22.18+. Na pasta `frontend`:

```sh
npm ci
npm run dev
```

Em outro terminal, execute o backend dentro de `backend/`, com Java 21 e PostgreSQL configurados: `./mvnw spring-boot:run` ou `.\mvnw.cmd spring-boot:run` no Windows.

## Verificar

```sh
npm test
npm run build
npm run test:evidencias
```

São 36 testes de cadastro, catálogo e sacola. As evidências são geradas em `frontend/docs/evidencias/` e não são versionadas automaticamente.

## Integração atual

O Vite encaminha `/api` para `http://localhost:8080`, removendo o prefixo. Configure `API_PROXY_TARGET` e `VITE_API_BASE_URL` conforme `.env.example`. O cadastro de brechó usa `POST /usuarios`, envia nome, e-mail, senha e WhatsApp com DDI e DDD, aceita `AuthResponse.usuario` com role `LOJA` e descarta o token no retorno à interface. Nome: até 100 caracteres; senha: 6 a 72; WhatsApp: 10 a 13 dígitos, conforme o backend. A tela ainda não inicia sessão JWT.

Catálogo e checkout permanecem demonstrativos. Os endpoints novos do backend não estão automaticamente integrados às demais telas. Os testes HTTP usam respostas simuladas e não comprovam persistência no PostgreSQL.

## Identidade e publicação

Logo e variações em `public/brand/`, imagens do catálogo em `public/products/` e registros de marca em `docs/`. O build gera `dist/`. Na hospedagem, configure fallback das rotas para `index.html` e uma API acessível: o proxy local do Vite não acompanha os arquivos estáticos.
