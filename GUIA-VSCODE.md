# Abrir e executar o ISAC Brechó no VS Code

## 1 Preparar o computador

Instale Git, JDK 21 ou superior, Node.js 22.18 ou superior, PowerShell 7 e VS Code. As extensões recomendadas são Extension Pack for Java e PowerShell, ambas dos fornecedores oficiais. A primeira execução precisa de internet para baixar as dependências Maven e npm.

Obtenha o projeto atualizado:

```powershell
git clone https://github.com/Residencia-Tecnologica-2026-2/bazar-solidario.git
cd bazar-solidario
code ISAC-Brecho.code-workspace
```

Se usar o ZIP, extraia primeiro e abra o arquivo `ISAC-Brecho.code-workspace` em Arquivo > Abrir Workspace do Arquivo. A raiz aberta deve conter `frontend`, `backend` e este guia. Não abra apenas frontend se quiser trabalhar com o projeto inteiro.

## 2 Instalar as dependências

No VS Code, escolha Terminal > Executar Tarefa > **ISAC: preparar ambiente (primeira vez)**. Alternativa pelo terminal PowerShell 7 na raiz:

```powershell
./PREPARAR-AMBIENTE.ps1
```

O script verifica ferramentas e instala as dependências do frontend se ainda não existirem. Dependências existentes são preservadas. Após uma atualização do package-lock, use `./PREPARAR-AMBIENTE.ps1 -Reinstalar` para sincronizá-las.

## 3 Iniciar os dois serviços

Terminal > Executar Tarefa > **ISAC: iniciar aplicativo completo**. Essa tarefa abre backend e frontend em paralelo. Mantenha os dois terminais abertos. Também é possível clicar nos iniciadores `.cmd`, um para cada serviço.

Aguarde a mensagem `Started BazarSolidarioApplication` no backend e a URL do Vite no frontend. Abra http://127.0.0.1:4186/. API: http://127.0.0.1:8080/home.

O modo local usa H2 persistente em `backend/dados/` e fotos em `backend/uploads/`. Não exige Docker ou PostgreSQL. Uma chave aleatória é criada em `backend/.local-secrets.properties`, ignorada no Git. Não compartilhe a chave nem o banco local.

## 4 Demonstrar o fluxo para a ONG

1. Crie uma conta LOJA em Cadastro e informe WhatsApp. Em Perfil, entre em Cadastrar e gerenciar minhas peças.
2. Publique uma peça com foto, descrição, categoria, preço e estoque. Confira sua presença no catálogo.
3. Saia da loja e crie/acesse uma conta CLIENTE. Salve uma peça nos favoritos e observe os dois estados da etiqueta.
4. Adicione a peça à sacola e registre o pedido. A tela informa confirmação manual e a divisão ISAC 10% / brechó 90%.
5. Volte à conta LOJA e abra Pedidos recebidos. Aceite ou cancele. Cancelamento devolve estoque uma única vez.
6. Explique que a confirmação não cobra cartão/PIX e não transfere dinheiro. Não use dados reais de cartão na apresentação.

Use contas e peças fictícias ao demonstrar. O ZIP e o Git não levam banco nem fotos de teste já cadastrados: uma extração nova começa vazia.

## 5 Encerrar e retomar

Use Ctrl+C em cada terminal; não feche o VS Code com os processos em segundo plano sem conferir. Ao retomar, execute os mesmos iniciadores. Os dados persistem. Não apague `dados` ou `uploads` para resolver erro de inicialização. Não rode duas APIs sobre o mesmo arquivo H2.

## Problemas comuns

| Sintoma | Conferir | Ação |
|---|---|---|
| Não foi possível conectar ao servidor | Backend iniciou e permanece aberto? | Abra a API /home; confira o terminal Java e inicie o backend |
| Porta 4186 ou 8080 ocupada | Já existe uma execução? | Use a existente ou encerre conscientemente seu terminal; não mate processos indiscriminadamente |
| Sessão expirou | Chave mudou ou token venceu? | Saia e entre novamente; validade padrão de oito horas |
| JAVA_HOME ou compilação incompatível | JDK 21+ e terminal atualizado? | Ajuste a instalação do JDK e reabra o VS Code |
| Maven/npm não baixam dependências | Internet ou proxy? | Confira a conexão e repita a preparação; não desative verificação de certificados |
| Banco H2 em uso | Outra API usa backend/dados? | Encerre a execução duplicada e mantenha o arquivo |
| Peça mudou ao editar | Houve compra ou outra edição? | Recarregue peças e estoque e reabra a edição |
| Catálogo vazio | Uma loja já publicou uma peça com estoque? | Crie conta LOJA e publique; não há catálogo fictício como fallback |
| Script bloqueado no terminal | Está usando PowerShell 7? | Use os iniciadores .cmd que chamam pwsh apenas para esse processo |

## Verificar antes de compartilhar

Use as tarefas **testes frontend**, **build frontend** e **testes backend**. Também confira no navegador cadastro, login, foto, favorito, pedido e cancelamento. Para publicação com PostgreSQL e HTTPS, siga `IMPLANTACAO.md`: rodar localmente não é o mesmo que implantar em produção.
