# ISAC Brechó — integração local

Esta pasta combina o PR 7 (`4b84704`) com os ícones aprovados e correções locais. Não foi publicado commit nem feito merge no GitHub.

## Abrir e executar

Abra `ISAC-Brecho.code-workspace` no VS Code. O front-end fica em `frontend/`, a API Java em `backend/`.

Requisitos: Java 21 e Node.js 22.18 ou superior. Ao extrair o ZIP em outro local, execute `npm ci` na pasta frontend.

Execute `INICIAR-BACKEND.cmd` e `INICIAR-FRONTEND.cmd` em duas janelas. Alternativamente use Terminal → Executar Tarefa → ISAC: iniciar aplicativo completo no VS Code. Se os servidores já estiverem rodando, use os links sem iniciar outra cópia.

- Aplicativo: http://127.0.0.1:4186/
- Cadastro: http://127.0.0.1:4186/cadastro
- Login: http://127.0.0.1:4186/login
- API: http://127.0.0.1:8080/home

O perfil `local` usa H2 persistente em `backend/dados/`. Isso permite cadastrar e autenticar sem Docker ou PostgreSQL instalados. Os dados permanecem após reiniciar a API. O perfil PostgreSQL original continua separado e não foi validado nesta máquina. Não execute duas APIs sobre o mesmo arquivo H2.

## Correções

1. O fetch padrão do UsuarioService agora é chamado por `globalThis.fetch(...)`, preservando o receptor exigido pelo navegador. A chamada anterior como método da classe podia falhar antes de enviar HTTP.
2. O front usa `/api`; o Vite encaminha para `localhost:8080`, removendo o prefixo. O navegador conversa com a própria origem; não foi liberado CORS indiscriminadamente.
3. Cadastro de CLIENTE e LOJA, login, sessão JWT e logout do PR foram mantidos. O servidor recusa cadastro público como ADMIN e exige WhatsApp válido para LOJA.
4. As regras privadas `/anuncios/meus` e `/lojas/me` passam antes das regras públicas com curingas.
5. O cliente trata resposta nula, identificador inválido e perfil inesperado. Os testes antigos de cadastro foram alinhados ao AuthResponse com token e usuario.
6. Ícones originais e categorias normalizadas foram preservados.

## Fluxo

Formulário → UsuarioService → POST /api/usuarios ou /api/auth/login → proxy Vite → controller Java → service → repository → banco → JSON com token e usuário → sessão local e perfil.

O catálogo e a sacola ainda usam dados demonstrativos. A tela de pagamento do PR também é simulada; não faz cobrança nem transferência. Esta entrega corrige e valida autenticação, não conecta automaticamente todas as telas à API.

## Verificar

- Frontend: `npm test` e `npm run build`.
- Backend: `mvnw.cmd test`.
- Cadastre uma conta fictícia, saia e entre novamente. Recarregue a página para conferir a sessão. Senha errada deve mostrar mensagem de credenciais inválidas, e e-mail repetido deve ser recusado.

O ZIP contém código e configuração, sem node_modules, banco local, tokens ou artefatos compilados. O H2 é exclusivo para desenvolvimento local; antes de publicar, configure PostgreSQL, chave JWT e estratégia de sessão apropriada ao ambiente.
