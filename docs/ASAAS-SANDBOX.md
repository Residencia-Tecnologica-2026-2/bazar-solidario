# Pagamento ISAC Brechó — piloto Sandbox

## O que esta atualização implementa

- Cobrança Pix no Asaas Sandbox e abertura da fatura hospedada pelo provedor.
- Comissão fixa calculada no servidor: 10% do total das peças, arredondados para centavos. O saldo líquido restante permanece na conta emissora, representando o brechó.
- Tarifa e valor líquido previsto do brechó registrados conforme a resposta do Asaas.
- Pagamento separado do aceite comercial da loja. Somente uma consulta ao provedor pode informar o recebimento.
- Uma tentativa persistida por pedido antes de chamar a API. Timeout mantém a tentativa em reconciliação, sem repetir a criação automaticamente.
- Consulta autenticada pelo comprador ou vendedor responsável, recuperação por referência e webhook autenticado.
- Bloqueio do cancelamento comum após iniciar cobrança: não devolve estoque de uma venda que ainda possa ser paga.
- Nenhuma chave, CPF ou corpo da resposta externa é registrado em logs pela integração. O CPF é usado na requisição ao provedor e não é persistido pelo aplicativo.

## Estado real da implantação

Este é um piloto de **uma conta emissora vinculada a um vendedor local**, exclusivamente em Sandbox. Não é um cadastro completo de recebedores de um marketplace em produção. Lojas diferentes não podem usar a mesma conta emissora por engano: pedidos de outro vendedor são bloqueados.

Em 18/11/2026 o piloto foi configurado em `https://isac-brecho.vercel.app/` com duas contas Sandbox aprovadas e carteiras distintas. Foram executadas duas cobranças fictícias de R$ 20,00: uma diretamente no provedor e outra pelo aplicativo. Em ambas, o pagamento retornou `RECEIVED` e o split fixo de R$ 2,00 retornou `DONE`.

No teste pelo aplicativo, cadastro, login, foto persistida no PostgreSQL, peça, reserva de estoque e pedido foram exercitados. A mesma tentativa reutilizou o checkout existente, e a confirmação simulada do Asaas atualizou automaticamente o pagamento para `PAGO` via webhook autenticado. Essa atualização foi conferida pela listagem de pedidos, sem executar a consulta manual de confirmação. A peça fictícia foi ocultada ao finalizar o teste.

Os testes automatizados continuam usando respostas simuladas do provedor; a homologação acima é uma verificação adicional no Sandbox. Ela cobre esse fluxo de sucesso e recuperação, não representa aprovação para movimentar dinheiro real nem homologação de todos os cenários de estorno e falha. O piloto permanece restrito à loja de homologação vinculada no servidor.

## Preparação local

Antes da primeira cobrança com divisão, habilite **Integrações → Segurança → Habilitar Split** no Sandbox. A confirmação do ambiente de testes informa o código de simulação na própria tela. Não confundir essa opção com validação de saque por webhook ou autorização de encerramento de subcontas.

Cada recebedor precisa de uma carteira de outra conta. A API de criação de subcontas exige uma conta-pai pessoa jurídica; uma conta Sandbox pessoa física pode exigir cadastrar uma segunda conta de testes separadamente. Senhas e aceites do titular devem ser concluídos pelo responsável. Não utilizar o Wallet ID da própria conta emissora como destino do split.

1. Execute `CONFIGURAR-ASAAS.ps1` e informe a chave da conta de testes que representará o brechó. Nunca edite o script para colar a chave.
2. Crie uma segunda conta no **Sandbox** para representar o ISAC. Copie seu Wallet ID em Integrações; não precisa fornecer sua chave ao aplicativo.
3. Escolha o usuário LOJA do aplicativo que representa a conta emissora. Em `GET /lojas`, use o campo `vendedorId`, não o `id` da loja.
4. Execute `VINCULAR-ASAAS-SANDBOX.ps1` na pasta do projeto. O script pede o vendedor e a carteira do ISAC, confere que não é a carteira da conta emissora e gera um token privado de webhook.
5. Reinicie o backend com `INICIAR-BACKEND.cmd` e o frontend com `INICIAR-FRONTEND.cmd`.

O arquivo `backend/.asaas-local.properties` é ignorado pelo Git e importado pelo perfil local. A integração recusa qualquer URL diferente de `https://api-sandbox.asaas.com/v3`. O programa nunca deve ser alterado apenas para trocar essa URL por produção.

## Fluxo de teste

1. Entre no aplicativo como CLIENTE, escolha uma peça da loja vinculada e registre o pedido.
2. Informe um CPF fictício válido próprio para o ambiente de testes; o aplicativo informa antes de enviá-lo ao Asaas. Não use dados pessoais de terceiros.
3. Gere a cobrança e abra o checkout de teste. Não use aplicativo bancário real.
4. Simule o recebimento pelas ferramentas do Asaas Sandbox. Consulte a confirmação na área de pedidos.
5. Confira no Asaas a cobrança e o split de comissão na segunda conta. O estado PAGO no aplicativo informa recebimento, não comprova liquidação do split.
6. Reabra o pedido e tente gerar novamente: a integração deve reutilizar a mesma tentativa e não criar outra cobrança.

## Webhook para atualização automática

O Asaas não acessa `127.0.0.1`. Para callbacks automáticos, o backend precisa de uma URL pública HTTPS ou um túnel de desenvolvimento configurado para a equipe.

- Receptor: `POST /webhooks/asaas`.
- Configure no Asaas os eventos de cobrança necessários, incluindo recebimento, confirmação, vencimento e estorno.
- O token de autenticação deve ser o mesmo `app.asaas.webhook-token` do arquivo privado; ele é diferente da chave da API e não deve ser publicado.
- Na implantação unificada do Vercel, a URL pública é `https://isac-brecho.vercel.app/api/webhooks/asaas`. O webhook provisionado deve permanecer inativo enquanto faltarem o vendedor vinculado, a carteira recebedora e a validação da cobrança de teste.
- O receptor verifica `asaas-access-token` e consulta a cobrança no Asaas antes de atualizar o banco. Eventos repetidos não geram outro pagamento nem alteram o estoque.
- Sem URL pública, use **Consultar confirmação no Asaas** no aplicativo. Isso é uma alternativa local de teste; não equivale a um webhook já instalado.

## Limites antes de produção

- Implementar cadastro e autorização das contas recebedoras para cada brechó; revisar o modelo de contas com o Asaas.
- Implementar acompanhamento específico dos eventos de liquidação e falha do split e conciliação operacional.
- Implementar estorno solicitado pelo aplicativo, cancelamento de cobrança, devolução de produto e liberação segura de estoque. No piloto, a equipe concilia pelo painel do provedor e o estoque não é liberado automaticamente.
- Implementar expiração/reconciliação em segundo plano e tratamento de atendimento para tentativas inconclusivas. Não criar outra cobrança apenas porque uma consulta não encontrou a anterior.
- Configurar banco PostgreSQL com migrações, HTTPS, armazenamento e backups de produção. A tabela nova é criada automaticamente somente no perfil local/teste.
- Testar ponta a ponta com contas aprovadas, tarifas reais, falhas de rede e cenários de estorno.
- Frete não integrado. O total desta cobrança contém apenas produtos. Vendas presenciais não geram split por este fluxo.

## Referências

- [Split e regras de valor líquido](https://docs.asaas.com/docs/split-de-pagamentos)
- [Criar cobrança](https://docs.asaas.com/reference/criar-nova-cobranca)
- [Webhooks](https://docs.asaas.com/docs/webhooks-2)
- [Tentativas e idempotência](https://docs.asaas.com/docs/retries-and-idempotency)
