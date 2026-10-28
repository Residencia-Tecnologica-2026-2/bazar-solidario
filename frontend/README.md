# Front-end ISAC Brechó

React + TypeScript + Vite. Execute npm ci para instalar dependências. Use INICIAR-FRONTEND.cmd na raiz para iniciar na porta 4186.

Cadastro e login usam /api, encaminhado pelo Vite ao backend em localhost:8080. A autenticação recebe JWT e usuário, mantém a sessão e permite logout. O cadastro aceita CLIENTE ou LOJA; loja exige WhatsApp com 10 a 13 dígitos.

UsuarioService chama globalThis.fetch por uma função intermediária com contexto correto no navegador. Ícones em public/icons/isac/ são os arquivos exportados do Figma.

Verificação: npm test (38 testes) e npm run build. A evidência HTTP real está em ../validacao-integracao.json.

Consulte ../INTEGRACAO-LOCAL.md para executar API e banco persistente local. Catálogo, sacola e pagamento continuam demonstrativos.
