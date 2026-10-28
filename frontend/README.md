# Frontend ISAC Brechó

React + TypeScript + Vite, integrado à API Spring Boot. Catálogo sem fallback fictício, lojas, encontros, publicação com foto, JWT e pedidos com confirmação manual.

```sh
npm ci
npm run dev -- --host 127.0.0.1 --port 4186
npm test
npm run build
```

Use `VITE_API_BASE_URL=/api`; o proxy Vite usa `API_PROXY_TARGET=http://localhost:8080`. Não coloque segredos em variáveis VITE_. Veja README, INTEGRACAO-LOCAL e IMPLANTACAO na raiz.

Cliente registra pedido e acompanha o aceite. Loja publica anúncios e administra seus pedidos. Divisão de 10% para ISAC e 90% para o brechó calculada no servidor; pagamento e repasse são manuais.
