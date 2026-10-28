# Integração do MVP

## Identidade e sessão

`POST /usuarios` cria CLIENTE ou LOJA (LOJA exige WhatsApp). Cadastro público como ADMIN é recusado. `POST /auth/login` devolve JWT e usuário. `GET /auth/me` valida a sessão no servidor. O frontend verifica expiração antes das chamadas privadas e limpa a sessão quando recebe 401. Senhas são armazenadas com BCrypt no backend.

O JWT fica no armazenamento local do navegador; não é uma sessão por cookie HttpOnly e logout remove a cópia local, sem revogar tokens já emitidos. A validade padrão é de oito horas. Mudança da chave invalida tokens anteriores. Para uma implantação pública, a estratégia de sessão e proteção contra XSS deve ser revisada pela equipe.

O navegador usa `/api`. O proxy Vite encaminha para a API local e remove o prefixo. Não é necessário liberar CORS para qualquer origem.

## Dados e telas

| Tela | API | Acesso |
|---|---|---|
| Catálogo e detalhe | GET /anuncios, /anuncios/{id} | Público; sem fallback de produtos fictícios |
| Brechós | GET /lojas | Público; link para peças do proprietário |
| Encontros | GET /encontros | Público; datas e locais vindos da API |
| Painel de peças | GET /anuncios/meus; POST/PUT/PATCH /anuncios | LOJA, apenas anúncios próprios |
| Foto | POST /uploads (multipart: arquivo) | Token obrigatório; interface de loja |
| Perfil da loja | GET/PUT /lojas/me | LOJA proprietária |
| Registrar pedido | POST /pedidos | CLIENTE |
| Histórico | GET /pedidos?page=0&size=12 | Cliente vê seus pedidos; loja vê os recebidos |
| Aceite/cancelamento | PATCH /pedidos/{id}/confirmar ou cancelar | Propriedade conferida no servidor |

O painel mínimo publica, edita e oculta anúncios e exibe pedidos recebidos. Uploads aceitam JPEG, PNG e WebP de até 5 MB, com nome gerado no servidor. Fotos públicas não exigem token para leitura. Tamanho, cor e conservação devem estar na descrição: a interface não inventa esses atributos.

## Contrato de pedido

`POST /pedidos` exige `Authorization: Bearer ...`, `Content-Type: application/json` e `Idempotency-Key` (UUID por intenção de compra).

```json
{"itens":[{"anuncioId":42,"quantidade":1}]}
```

Não são aceitos como fonte de verdade preços, percentuais, total ou proprietário enviados pelo navegador. O servidor consulta anúncios ativos, verifica estoque e um único vendedor e grava itens com título e preço como estavam no pedido. A mesma chave e os mesmos itens devolvem o pedido já criado; reutilizar a chave com outro conteúdo produz conflito.

Reserva e criação acontecem na mesma transação. Bloqueios de linha ordenados impedem disputa da última unidade. Edição de anúncio exige `versao`, obtida na consulta: formulário antigo não pode repor acidentalmente estoque reservado. Cancelar devolve unidades sob os mesmos bloqueios e não pode ocorrer duas vezes.

Estados: `AGUARDANDO_CONFIRMACAO_MANUAL` → `CONFIRMADO` ou `CANCELADO`. Cliente só cancela pendente; a loja proprietária também pode cancelar após aceite. Não há estado “pago”, estorno financeiro ou transferência automática.

## Divisão ISAC

Base: soma de preço unitário × quantidade dos produtos, sem frete ou taxa extra. ISAC = total × 0,10, arredondado HALF_UP para duas casas. Brechó = total − ISAC. Exemplo: R$ 89,99 → ISAC R$ 9,00; brechó R$ 80,99. Cancelados devem ser excluídos da conciliação financeira. A divisão não comprova recebimento de dinheiro.

## Limites explícitos

Reservas pendentes não expiram automaticamente: cliente ou loja devem cancelar quando não houver continuidade. A interface adiciona uma unidade de cada anúncio; a API valida quantidades de 1 a 99. Não há inscrição em eventos, frete integrado, comprovante fiscal, gateway ou repasse automático. Formulários institucionais de contato/parceria continuam identificados como prévias sem envio.

H2 persistente foi usado nesta máquina porque PostgreSQL/Docker não estão disponíveis. Isso não substitui testar a implantação com PostgreSQL. Testes e evidências HTTP usam exclusivamente contas fictícias.

## Favoritos sincronizados com o Figma

A etiqueta com estrela vem do desenho original `242:216`. As variantes `249:557` (não salvo) e `249:565` (salvo) usam a mesma geometria, com estrela creme ou laranja. A aplicação usa os SVGs locais, com área óptica de 24 px, nos botões de produto, categoria e navegação. Botões mantêm aria-pressed e a navegação ativa mantém aria-current. Fonte e arquivos estão em frontend/public/icons/isac/manifest.json.
