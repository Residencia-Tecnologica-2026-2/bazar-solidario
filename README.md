# ISAC Brechó - Bazar Solidário

Projeto Integrador 3 da Residência Tecnológica.

## Organização

- `backend/`: API Spring Boot e infraestrutura.
- `frontend/`: aplicativo web mobile React + TypeScript + Vite, com a identidade ISAC atualizada.
- `Tarefas da equipe/`: planejamento compartilhado.

## Front-end local

Requer Node.js 22.18+.

```sh
cd frontend
npm ci
npm run dev
```

Verificação: `npm test` e `npm run build`. `npm run test:evidencias` gera registros em `frontend/docs/evidencias/`.

## Backend local

Com Java 21 e PostgreSQL configurados, em outro terminal:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

O front-end encaminha `/api` para `http://localhost:8080`. O cadastro usa `POST /usuarios` e aceita `AuthResponse.usuario`. A interface ainda não mantém sessão JWT. Catálogo e checkout utilizam dados demonstrativos; a existência de endpoints no backend não significa que todas as telas já estejam conectadas. Veja `frontend/README.md`.

## API da tela principal e imagens

- `GET /home` retorna até cinco encontros próximos e doze lojas para a tela inicial.
- `POST /uploads` recebe imagens JPEG, PNG ou WebP de até 5 MB com autenticação Bearer. O volume `bazar-uploads` do Docker preserva os arquivos ao recriar a API.
- Para executar a suíte do backend sem PostgreSQL, em `backend/`: `./mvnw clean test` (Java 21).
- Contratos: `backend/docs/HOME-API.md` e `backend/docs/UPLOADS-API.md`.
