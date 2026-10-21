# API de autenticação

## Cadastro de loja

**POST /usuarios** (público) recebe:

```json
{"nome":"Brechó da Ana","email":"ana@email.com","senha":"123456","whatsapp":"5581999999999"}
```

O WhatsApp deve conter de 10 a 13 dígitos, incluindo DDI e DDD. O cadastro cria o usuário e a loja na mesma transação. O nome inicial da loja é o nome informado no cadastro; o token retorna com role `LOJA`. A senha não é devolvida. E-mail duplicado retorna 409 e dados inválidos retornam 400.

## Login

**POST /auth/login** recebe e-mail e senha e devolve um token Bearer.

## Sessão atual

**GET /auth/me** exige `Authorization: Bearer <token>` e devolve os dados públicos do usuário autenticado.
