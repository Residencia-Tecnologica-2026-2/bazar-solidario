# API de lojas

## Cadastro

`POST /usuarios` (público) recebe `nome`, `email`, `senha` e `whatsapp`. O WhatsApp deve conter somente 10 a 13 dígitos, incluindo DDI e DDD. O cadastro cria usuário e perfil de loja na mesma transação; o nome inicial da loja vem do nome informado no cadastro. A senha nunca aparece nas respostas e a role criada é `LOJA`.

```json
{"nome":"Brechó da Ana","email":"ana@email.com","senha":"123456","whatsapp":"5581999999999"}
```

A resposta mantém o contrato `AuthResponse` com token Bearer e usuário autenticado.

## Rotas

| Método | Rota | Acesso | Resultado |
|---|---|---|---|
| GET | `/lojas?page=0&size=20` | Público | Lista paginada de perfis |
| GET | `/lojas/{id}` | Público | Perfil completo |
| GET | `/lojas/me` | Role LOJA | Perfil da loja identificada pelo token |
| PUT | `/lojas/me` | Role LOJA | Atualiza o próprio perfil |

A resposta completa contém `id`, `nome`, `logoUrl`, `descricao`, `endereco`, `latitude`, `longitude` e `whatsapp`. O WhatsApp é público para que clientes possam contatar a loja. Endereço e coordenadas podem ser nulos.

Exemplo de atualização:

```json
{"nome":"Brechó da Ana","logoUrl":"https://exemplo.com/logo.png","descricao":"Peças vintage","endereco":"Rua X, 10, Recife","latitude":-8.05,"longitude":-34.9,"whatsapp":"5581999999999"}
```

O proprietário vem do token, nunca do corpo da requisição. O WhatsApp segue a mesma validação do cadastro. Erros de validação retornam 400, perfil inexistente retorna 404 e rotas privadas sem token retornam 401.

## Integração com outros módulos

`LojaAutenticadaService.obter(Authentication)` resolve a loja pelo e-mail do usuário autenticado e devolve a entidade `Loja`. Encontros e anúncios devem usar esse serviço para resolver o proprietário.
