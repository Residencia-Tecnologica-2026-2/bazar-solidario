# Autenticação (JWT)

Fluxo: **cadastrar → recebe token → manda o token nas rotas protegidas**.

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| POST | `/usuarios` | Público | Cadastra e já devolve o token |
| POST | `/auth/login` | Público | E-mail + senha → token |
| GET | `/auth/me` | Logado | Mostra o usuário do token |

## Cadastro
```
POST /usuarios
{ "nome": "Ana", "email": "ana@email.com", "senha": "123456" }
```
Resposta `201`:
```
{ "token": "eyJ...", "tipo": "Bearer", "expiraEmMs": 86400000,
  "usuario": { "id": 1, "nome": "Ana", "email": "ana@email.com", "role": "CLIENTE" } }
```
Erros: `400` (campo inválido, com mensagem por campo), `409` (e-mail já cadastrado).

## Login
```
POST /auth/login
{ "email": "ana@email.com", "senha": "123456" }
```
Mesma resposta do cadastro. Erro: `401` "E-mail ou senha inválidos".

## Usando o token
Em toda rota protegida: `Authorization: Bearer <token>`
```
curl http://localhost:8080/auth/me -H "Authorization: Bearer SEU_TOKEN"
curl -X POST http://localhost:8080/anuncios -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Camiseta","preco":25.00,"estoque":3,"categoriaId":1}'
```
Sem token ou com token inválido/expirado: `401`.

## Configuração (`application.properties`)
- `jwt.secret` – chave de assinatura (mín. 32 caracteres). Em produção use a variável de ambiente `JWT_SECRET`.
- `jwt.expiration-ms` – validade do token (padrão 24h).

## Notas
- Senhas são salvas com BCrypt. Usuários antigos salvos em texto puro não conseguem logar: apague-os da tabela `usuarios` e cadastre de novo.
- O token guarda o **e-mail** (subject) e a role. O `UsuarioAutenticadoService` já usa o e-mail.
- Todo cadastro novo vira `CLIENTE`. Para criar um ADMIN, altere a coluna `role` direto no banco.
