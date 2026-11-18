# Mercado Livre — produtos e consulta de frete

## O que está implementado

A tela **Perfil do brechó → Integrações** permite preparar OAuth com PKCE e autorização da conta de teste, listar e consultar seus anúncios, carregar características de uma categoria, validar/publicar uma peça de teste e consultar opções de frete por CEP. A API valida a propriedade do anúncio; tokens permanecem cifrados no servidor com AES-256-GCM.

O código aceita apenas usuários de teste brasileiros (`test_user`, site `MLB`). O Mercado Livre usa usuários de teste em seu ambiente normal, em vez de uma URL Sandbox separada. Não utilizar compradores/vendedores reais durante a homologação.

## Cadastro necessário antes de usar

1. O responsável deve criar uma aplicação no [portal de desenvolvedores do Mercado Livre](https://developers.mercadolivre.com.br/). A equipe ainda não possuía essa aplicação ao preparar a integração.
2. Configurar a URL de retorno exata: `https://SEU-PROJETO/api/integracoes/mercadolivre/callback` e as permissões exigidas para usuários, anúncios e envios. Aceites e autorizações da conta devem ser realizados pelo responsável.
3. Criar usuários de teste segundo a documentação oficial e autorizar um vendedor brasileiro de teste pela tela do ISAC.
4. Cadastrar `ML_CLIENT_ID`, `ML_CLIENT_SECRET`, `ML_REDIRECT_URI`, `ML_ENCRYPTION_KEY` e `PUBLIC_APP_URL` nas variáveis privadas do servidor. A chave de cifragem deve conter 32 bytes aleatórios codificados em Base64 e ser preservada; trocá-la invalida o acesso aos tokens já guardados.

Não compartilhar segredos por mensagem nem incluí-los em variáveis do React. O callback verifica um state de uso único com validade de dez minutos; em caso de falha, iniciar nova conexão.

## Roteiro de homologação

1. Entrar no ISAC como brechó e conectar o vendedor de teste. A conta real deve ser recusada.
2. Cadastrar uma peça com foto pública e estoque, depois ocultá-la no ISAC. A peça não pode ter pedidos pendentes ou já efetivados no canal local.
3. Informar a categoria MLB correta, carregar seus atributos e preencher os obrigatórios. O servidor usa o preço e estoque cadastrados no banco; marca o anúncio como teste e adapta `title`/`family_name` ao tipo de vendedor retornado pelo provedor.
4. Validar e publicar. Conferir a resposta e o anúncio na conta de teste. Repetir a ação não deve criar outro anúncio: a tentativa fica registrada antes da chamada externa.
5. Selecionar o anúncio, consultar um CEP válido e conferir as opções de frete retornadas.
6. Simular uma resposta inconclusiva: o estado `RECONCILIAR` impede repetição automática. Procurar o anúncio na conta de teste e associar o código encontrado, sem reenviar a publicação às cegas.

## Limites explícitos

- Uma peça vinculada ou com publicação iniciada fica bloqueada para reativação/edição local. Isso evita ofertar o mesmo estoque em dois canais enquanto não há sincronização automática.
- Não estão implementados recebimento de notificações de vendas, sincronização contínua de estoque, importação de pedidos, desligamento/reconciliação administrativa completa nem emissão de etiquetas.
- A cotação de `/items/{id}/shipping_options` pertence ao anúncio do Mercado Livre. Ela não contrata frete para uma compra paga pelo Asaas no ISAC. Essa combinação exige outra integração logística.
- Os testes automatizados utilizam um gateway simulado: validam o código local, mas não substituem a autorização e a homologação real da aplicação.
- A aplicação não deve ser apresentada como integração comercial pronta antes dessa homologação.

Referências: [autorização](https://developers.mercadolivre.com.br/pt_br/autenticacao-e-autorizacao), [testes](https://developers.mercadolivre.com.br/pt_br/atributos/realizacao-de-testes), [User Products](https://developers.mercadolivre.com.br/pt_br/user-products), [validação de anúncios](https://developers.mercadolivre.com.br/en_us/listing-validator).
