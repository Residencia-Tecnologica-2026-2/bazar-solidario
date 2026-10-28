# Upload de fotos

## Enviar imagem

POST /uploads exige autenticação Bearer e recebe multipart/form-data com o campo arquivo.

Formatos aceitos: JPEG, PNG e WebP. O arquivo deve ter até 5 MB e no máximo 10 megapixels. O servidor valida o conteúdo da imagem, gera um nome UUID e não usa o nome enviado pelo cliente.

Resposta 200:

```json
{ "url": "/uploads/5b5e...-....png" }
```

Tipos ou conteúdos inválidos retornam 415, arquivo vazio retorna 400 e arquivo maior que o limite retorna 413.

## Consultar imagem

GET /uploads/{arquivo} é público. Os arquivos ficam no volume Docker bazar-uploads, persistente entre recriações do serviço de API.
