# ISAC Brechó — versão integrada local

Projeto Integrador 3 · Residência Tecnológica.

Base: PR 7 (feat/cadastro-login-pagamento, commit 4b84704), correções locais e ícones aprovados no Figma. Esta pasta não altera a main nem o PR remoto.

Abra ISAC-Brecho.code-workspace no VS Code. Consulte INTEGRACAO-LOCAL.md para iniciar front e backend.

- frontend/: React, TypeScript, Vite, cadastro/login com API e ícones ISAC.
- backend/: Java 21, Spring Boot, JWT e persistência. Perfil local H2 em arquivo; configuração PostgreSQL separada.
- validacao-integracao.json: 12 verificações HTTP reais pelo proxy.
- INICIAR-BACKEND.cmd e INICIAR-FRONTEND.cmd: iniciar em duas janelas.

Aplicativo: http://127.0.0.1:4186/ · API: http://127.0.0.1:8080/home

Verificado: cadastro de cliente e loja, login, e-mail duplicado, senha incorreta, token, logout, sessão e persistência após reiniciar a API. 38 testes frontend e 56 backend aprovados; build concluído.

Catálogo e pagamento continuam demonstrativos. O perfil local não comprova o funcionamento com PostgreSQL nem é configuração para publicação.
