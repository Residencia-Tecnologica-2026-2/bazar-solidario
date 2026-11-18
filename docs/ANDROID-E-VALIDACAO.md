# Android e validação — 18/11/2026

## Versão de demonstração

O Android usa Capacitor 8 com o front-end React embarcado. A API e as fotos dos anúncios ficam em `https://isac-brecho.vercel.app/api`; o aplicativo precisa de internet. Nenhuma chave Asaas, credencial PostgreSQL ou segredo Mercado Livre é empacotado no front-end.

O APK é de **depuração/homologação**, versão `0.1.0-sandbox`, pacote `br.org.isac.brecho`, Android 7.0/API 24 ou superior. Não é uma publicação na Play Store nem habilita pagamentos reais. O back-end permite a origem específica `https://localhost` do WebView Android, com autenticação Bearer e sem cookies entre origens. Tráfego HTTP está desabilitado e backup de dados do aplicativo também.

### Compilar

Requisitos: Node 22, Java 21, SDK Android plataforma 36 e ferramentas de compilação. Defina o caminho do SDK em `frontend/android/local.properties` (arquivo privado/ignorado; escape `:` e `\` conforme formato Java properties) ou `ANDROID_HOME`.

```powershell
cd frontend
npm ci
npm run build:android
cd android
.\gradlew.bat assembleDebug lintDebug
```

Resultado: `frontend/android/app/build/outputs/apk/debug/app-debug.apk`. `build:android` fixa a URL pública da API; o build web comum continua usando `/api`. A assinatura de depuração permite instalação manual para teste; futuras distribuições oficiais exigem chave de lançamento mantida pela equipe.

## Apresentação interativa

Quatro passos: propósito; salvar uma peça de exemplo; adicionar à sacola e compreender os 10%; acompanhar um pedido de exemplo. Usa exclusivamente a iconografia existente em `public/icons/isac` pelo componente `IsacIcons`, incluindo as duas artes de favoritos.

Os exercícios não criam pedidos, reservas ou cobranças. Aparece no primeiro acesso à página inicial, pode ser pulada e é acessível em Perfil → Rever apresentação do app. A conclusão é registrada apenas no dispositivo. Navegação por passos e teclado; preferência de movimento reduzido respeitada.

## Evidências de validação

- Front-end: 54 testes aprovados e build TypeScript/Vite aprovado.
- Back-end: 85 testes aprovados, incluindo preflight Android autorizado e origem arbitrária recusada; regressões de autenticação, proprietário, anúncios, duplicidade, estoque, pedidos e integrações.
- Navegador publicado: páginas inicial, catálogo, busca sem resultados, filtros, produto, favoritos, sacola, checkout, perfil, pedidos, cadastro, login/logout, painel de loja, páginas da comunidade/institucionais, termos e privacidade inspecionados. Áreas de loja exigem sessão/perfil apropriado.
- Fluxo real com dados **fictícios identificados**: login de loja e cliente; upload PNG; publicação com confirmação de foto semelhante; edição de preço R$ 25 → R$ 24; favorito; sacola; pedido #2 reservado; cancelamento sem cobrança devolveu estoque de 0 para 1. Anúncio de teste ocultado ao terminar.
- Pagamento #1 reconsultado: PAGO na aplicação, RECEIVED no Asaas Sandbox e split DONE de R$ 2 em compra simulada de R$ 20. Tarifa simulada R$ 0,99, líquido do brechó R$ 17,01. A homologação anterior também comprovou webhook automático e prevenção de cobranças duplicadas (ver ASAAS-SANDBOX.md).
- Apresentação testada no navegador com 320 e 390 pixels: interações, avanço/retorno, conclusão e persistência sem repetição no próximo acesso. Não foi constatada rolagem horizontal.
- APK: compilação e Android Lint executados. **Não havia celular/emulador conectado via ADB**; instalação e execução em Android físico ainda precisam de validação. Testes do navegador não substituem esse teste nativo.

## O que permanece limitado

1. Mercado Livre: configuração suspensa pelo usuário até concluir pessoalmente a identidade exigida pelo provedor. Produtos/frete não foram homologados na API real. A implementação piloto não é sincronização completa de estoque/pedidos entre canais.
2. Pagamento exclusivamente Sandbox, com um brechó de homologação vinculado. Produção exige carteiras reais, credenciais, regras operacionais, estorno e conciliação; nenhum teste de dinheiro real foi realizado.
3. Formulários de contato/parceria são prévias sem envio; encontros sem registros exibem estado vazio. Não há inscrição ou envio de mensagens habilitados nesses formulários.
4. Auditoria npm apontou um aviso ReDoS na dependência de ferramentas `picomatch@4.0.7`. O registro consultado não oferecia versão corrigida mais recente. Não foi aplicado downgrade automático amplo de Vite. Esse código de ferramenta não é incluído como dependência de execução do APK.

Portanto, esta versão é adequada à **demonstração dos fluxos homologados**, sem afirmar que todos os recursos comerciais estão prontos para produção.
