# Demonstração de compra — 18/11/2026

Aplicação: https://isac-brecho.vercel.app/

## Acessos de homologação

- Cliente: `homologacao.cliente@isac.example`.
- Vendedor: `homologacao.loja@isac.example` — Loja de Homologacao ISAC, vinculada às carteiras de teste Asaas.
- Senhas entregues diretamente ao responsável e em arquivo privado local, fora do GitHub. Não são chaves do Asaas nem contas de produção.

## Catálogo publicado

Anúncios criados pela interface do vendedor, sem alterar o código para inserir produtos em produção:

| Anúncio | Código | Preço de teste | Estoque inicial |
|---|---|---:|---:|
| Regata branca — DEMONSTRAÇÃO (id 3) | DEMO-REGATA-001 | R$ 40,00 | 5 |
| Bolsa caramelo — DEMONSTRAÇÃO (id 4) | DEMO-BOLSA-001 | R$ 60,00 | 5 |
| Tênis casual — DEMONSTRAÇÃO (id 5) | DEMO-TENIS-001 | R$ 50,00 | 5 |
| Vestido verde com bordados — DEMONSTRAÇÃO (id 6) | DEMO-VESTIDO-VERDE-001 | R$ 80,00 | 1 |
| Vestido vinho com bordados — DEMONSTRAÇÃO (id 7) | DEMO-VESTIDO-VINHO-001 | R$ 70,00 | 1 |
| Óculos escuros retangulares — DEMONSTRAÇÃO | DEMO-OCULOS-SOLAR-001 | R$ 35,00 | 1 |
| Armação oval marrom — DEMONSTRAÇÃO | DEMO-ARMACAO-MARROM-001 | R$ 25,00 | 1 |
| Armação oval tartaruga — DEMONSTRAÇÃO | DEMO-ARMACAO-TARTARUGA-001 | R$ 30,00 | 1 |
| Calça jeans preta de perna ampla — DEMONSTRAÇÃO | DEMO-CALCA-JEANS-001 | R$ 65,00 | 1 |
| Camisa xadrez de manga curta — DEMONSTRAÇÃO | DEMO-CAMISA-XADREZ-001 | R$ 45,00 | 1 |
| Camisa marrom estampada de manga longa — DEMONSTRAÇÃO | DEMO-CAMISA-ESTAMPADA-001 | R$ 55,00 | 1 |

São itens **fictícios**, com fotografia ilustrativa e aviso explícito na descrição. Não há venda, produto ou entrega real. O estoque muda conforme as reservas; o pedido nº 3 reservou uma regata, restando quatro disponíveis após esse teste. O upload das três fotografias e a exibição pública foram verificados.

Os oito últimos anúncios usam as fotografias enviadas pelo responsável, publicadas por sua instrução. São **cadastros de demonstração**, sem afirmar que as peças físicas são fictícias. Tamanhos, medidas, materiais e conservação não foram inventados; permanecem a confirmar. Valores dos vestidos aprovados pelo responsável; demais valores são provisórios de demonstração. Não se afirma graduação, autenticidade de marca ou proteção UV dos óculos. Fotografias pessoais enviadas foram armazenadas como uploads no banco, sem incluí-las no repositório Git. O catálogo público foi conferido com **11 anúncios ativos**.

## Cliente: experimentar a compra

1. Entre como cliente, explore o catálogo e adicione uma peça à sacola.
2. Continue, confira o total e registre o pedido. O servidor verifica preço e estoque; o pedido reserva a quantidade.
3. Em **Pagamento · ambiente de testes**, use o CPF fictício de homologação `11144477735` e escolha **Gerar pagamento Pix de teste**.
4. **Abrir checkout de teste** abre o Asaas Sandbox. Não escaneie o QR com banco real e não use Open Finance real. A conclusão é simulada pelo ambiente de homologação do provedor.
5. Volte a Pedidos e use **Consultar confirmação no Asaas**. O webhook também pode atualizar o estado; criar/aceitar um pedido não significa que ele foi pago.

O pedido **nº 3** está preparado na conta cliente: regata de R$ 40, cobrança `Aguardando pagamento de teste`, comissão solicitada ISAC R$ 4, tarifa simulada R$ 0,99, líquido previsto do brechó R$ 35,01. O checkout Sandbox foi aberto e conferido. **Essa cobrança não foi paga nem marcada como recebida** durante este teste.

O pedido **nº 1** preserva o exemplo já recebido: compra de R$ 20, estado PAGO, confirmação RECEIVED no provedor e split DONE anteriormente homologado. Permite comparar um pagamento pendente com um recebido.

## Vendedor: acompanhar

1. Saia da conta cliente e entre como vendedor.
2. No Perfil, use **Gerenciar pedidos recebidos** para conferir o pedido e seu pagamento separado.
3. **Cadastrar e gerenciar minhas peças** permite editar o catálogo e consultar estoque.
4. O aceite comercial não altera o pagamento para PAGO. Pedidos sem cobrança podem ser cancelados com devolução de estoque; cobrança vinculada exige conciliação para evitar liberar estoque com pagamento pendente.

## Android

O APK atualizado é `0.1.1-sandbox` (versionCode 2). Inclui as fotografias da apresentação e consulta o mesmo catálogo/API públicos; publicar produtos não exige recompilar o APK. A assinatura de depuração foi verificada e `assembleDebug` + `lintDebug` passaram. Não é uma versão comercial com pagamentos reais.
