# ISAC Brechó — Parte 2

Continuação técnica da Parte 1. Inclui UsuarioService, CatalogoService, SacolaService e 34 testes. Acrescenta duas classes e 20 testes em relação aos 14 testes do cadastro da etapa inicial.

Executar na pasta frontend: npm ci, npm test, npm run test:evidencias, npm run build e npm run dev. Requer Node.js 22.18 ou superior.

Esta versão foi organizada e executada em 18/10/2026; não representa publicação em semanas anteriores.

## Contrato atual de cadastro

Nome: até 100 caracteres. Senha: 6 a 72 caracteres, conforme RegistroRequest do backend. O cliente aceita o envelope AuthResponse e mantém compatibilidade com a resposta pública anterior.
