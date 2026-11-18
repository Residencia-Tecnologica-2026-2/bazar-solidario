# Atualizações e entrega — 18/11/2026

## Aplicação e implantação

- Front-end React/TypeScript, API Java/Spring Boot e fotos dos anúncios publicados no projeto próprio `isac-brecho` da Vercel, com PostgreSQL Neon e armazenamento de uploads no banco.
- Ajustes no início do contêiner, links de criação de anúncios atrás do proxy HTTPS, tratamento de identificadores Asaas e permissões CORS para WebView Android e origem pública.
- Branch de integração `feat/implantacao-integracoes`, mantendo a revisão da equipe no PR #11. A branch principal não foi mesclada automaticamente.

## Pagamento homologado

- Integração Asaas exclusivamente Sandbox com duas carteiras de teste distintas; confirmação e recebimento simulados, split DONE, webhook autenticado e prevenção de criação duplicada de cobrança.
- Compra fictícia de R$ 20: comissão ISAC R$ 2, tarifa simulada R$ 0,99, líquido do brechó R$ 17,01. Isso não é uma cotação de tarifas de produção.
- Aceite comercial do pedido e pagamento são estados separados. Cancelamento sem cobrança devolve estoque; cobrança vinculada exige conciliação antes de cancelar.
- Mercado Livre permanece sem homologação externa enquanto o titular não concluir a identidade exigida pelo provedor. Não há credenciais reais de pagamento no app.

## Android e apresentação

- Projeto Capacitor 8, app `br.org.isac.brecho`, internet HTTPS, marca ISAC no ícone e na abertura, navegação Voltar, sem backup dos dados do aplicativo.
- Apresentação em quatro passos com exercícios fictícios de favoritos, sacola e acompanhamento. Primeiro acesso, Pular, voltar por etapas e opção de rever no Perfil.
- Versão **0.1.1-sandbox**: fotografias reais de arara, look e peças dobradas substituem as ilustrações grandes. Imagens incluídas no APK, fontes/licença documentadas; controles mantêm os ícones Figma oficiais e os estados salvo/não salvo.
- APK é uma entrega de homologação assinada para instalação manual, não uma publicação na Play Store.

## Validação e contas

- 54 testes do front-end, 85 testes do back-end, teste de regressão CORS/proxy, build web e Android Lint aprovados nas verificações documentadas.
- Fluxo web: cadastro/login, loja, upload, anúncio semelhante, edição de preço, favoritos, sacola, reserva e cancelamento/retorno de estoque com dados fictícios. Peças fictícias foram ocultadas após a validação.
- Teste físico da versão 0.1.0 em Samsung SM-S731B/Android 17: apresentação, login, pedidos, consulta do pagamento Sandbox, Voltar e logout. Evidência guardada na entrega local.
- Contas fictícias existentes: cliente `homologacao.cliente@isac.example` e vendedor `homologacao.loja@isac.example`. **Senhas entregues em arquivo privado fora do repositório**; chaves Asaas, tokens, credenciais do banco e arquivos `.env` continuam ignorados.
- Formulários de contato/parceria ainda são prévias; frete/retirada é combinado com a loja. Produção exige concluir regras operacionais, recebedores e estornos antes de movimentar dinheiro real.
