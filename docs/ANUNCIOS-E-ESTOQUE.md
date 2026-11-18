# Identificação de peças e proteção contra anúncios duplicados

## Regras do aplicativo

1. Cada nova peça recebe um código de estoque. A loja pode informar o código da etiqueta; se deixar vazio, o servidor gera um identificador com prefixo `PEC-`.
2. O código é normalizado para maiúsculas e não pode se repetir dentro da mesma loja, nem em anúncios ocultos ou vendidos. Outras lojas podem usar o mesmo código.
3. O formulário mantém uma chave de publicação até a operação ser concluída. Repetir a requisição com a mesma chave e os mesmos dados devolve o anúncio existente. Usar a chave com outros dados retorna conflito.
4. Publicações concorrentes são serializadas por vendedor; restrições únicas no banco reforçam a proteção.
5. Antes de criar um novo anúncio, o servidor procura peças semelhantes apenas na loja autenticada. Título normalizado igual na mesma categoria ou a mesma foto geram um aviso. Anúncios ocultos e vendidos também participam da comparação.
6. A foto de upload é comparada pelo SHA-256 do arquivo: reenviar os mesmos bytes com outro nome não evita o aviso. Para imagens externas compara-se a URL, sem baixar conteúdo remoto. Fotos recortadas, recomprimidas ou visualmente parecidas não são reconhecidas por este mecanismo.
7. O aviso não cria o anúncio. A pessoa pode consultar os resultados ou confirmar que é outra peça; a confirmação não permite reutilizar um código já ocupado.

Não se apagam nem se mesclam anúncios automaticamente. Duas peças parecidas podem ser produtos físicos diferentes. Para unidades realmente iguais, a loja deve gerenciar a quantidade no anúncio existente.

## Interface

O painel mostra “Código da peça no estoque” no cadastro e na lista. Ao encontrar semelhanças, exibe os anúncios, o código, a situação e o motivo da sugestão. A opção “É outra peça” permite continuar. Alterar o formulário invalida a confirmação anterior.

A chave, o resumo dos dados e a URL da foto enviada são mantidos na sessão do navegador para recuperar uma tentativa; senha e token não são gravados nesse registro. A foto não é reenviada desnecessariamente ao repetir os mesmos dados.

## Contrato da API

- `POST /anuncios` exige `Idempotency-Key` com 16 a 64 caracteres alfanuméricos ou hífen. Clientes antigos precisam passar esse cabeçalho.
- `codigoPeca` é opcional, com até 40 caracteres: letras, números, ponto, hífen e sublinhado. `LEG-` é reservado.
- `confirmarSemelhante: true` declara que o usuário revisou e está cadastrando outra peça.
- Resposta `409` com `codigo: ANUNCIO_SEMELHANTE` contém até cinco sugestões da própria loja.
- Resposta `409` para código ocupado ou chave reutilizada com outro conteúdo não pode ser ignorada pela confirmação.
- `PUT /anuncios/{id}` conserva o controle de versão. O código informado também deve ser único; vazio ou ausente mantém o código anterior.

## Peças já existentes e implantação

Os anúncios antigos permanecem no banco. Sem código persistido, a resposta mostra `LEG-{id}` como identificação estável. Não é necessário recadastrar nem excluir produtos.

No perfil local, Hibernate cria as novas colunas e restrições no banco H2 persistente. Para PostgreSQL com `DDL_AUTO=validate`, aplicar previamente a migração `backend/migrations/20261028_anuncios_duplicados.sql`, com backup e revisão da equipe. Esta alteração não publica nem migra produção automaticamente.

## Verificação

Os testes cobrem publicação repetida e concorrente, colisão de código dentro da loja, isolamento entre lojas, aviso de título, confirmação de peça distinta, comparação de arquivo reenviado e exigência de chave. O fluxo de pedidos mantém sua própria proteção contra duplicação e reserva de estoque.
