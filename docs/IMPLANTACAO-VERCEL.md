# Implantação do ISAC — aplicação e API Java

## Situação desta entrega

O repositório contém a preparação para executar React e Spring Boot em um único contêiner. A publicação real depende de criar o projeto de hospedagem e configurar um PostgreSQL persistente. Não basta publicar apenas a pasta `frontend`: as chamadas `/api` precisam chegar ao Java.

O `Dockerfile` da raiz compila o React, inclui os arquivos no JAR e executa Java 21. O `vercel.json` da raiz usa Vercel Services com Container Runtime. Essa funcionalidade está em beta; confirmar disponibilidade e limites na conta antes da entrega. Se não estiver disponível, o mesmo Dockerfile pode ser usado em um provedor de contêineres, sem reescrever a aplicação.

Referências oficiais: [Container Runtime](https://vercel.com/docs/functions/runtimes/container), [Services](https://vercel.com/docs/services), [preços](https://vercel.com/docs/services/pricing).

## Configuração

1. Importar `Residencia-Tecnologica-2026-2/bazar-solidario` na Vercel, usando a **raiz do repositório**, sem limitar o projeto à pasta frontend.
2. Criar ou conectar um PostgreSQL. Avaliar o plano gratuito e os limites do provedor; não habilitar cobrança automaticamente.
3. Cadastrar no servidor as variáveis abaixo. Não usar prefixo `VITE_` em segredos, não publicar credenciais no GitHub e não enviar senhas no chat.

| Variável | Valor esperado |
| --- | --- |
| `DB_URL` | URL JDBC PostgreSQL, incluindo SSL conforme o provedor |
| `DB_USER` / `DB_PASSWORD` | Credenciais de um usuário exclusivo do projeto |
| `JWT_SECRET` | Segredo aleatório de pelo menos 32 bytes, estável entre reinícios |
| `APP_UPLOADS_STORAGE` | `database`, já definido no Dockerfile |
| `PUBLIC_APP_URL` | Origem HTTPS publicada, sem caminho, para fotos do Mercado Livre |
| `DDL_AUTO` | `validate` depois de criar o esquema |

Para um **banco novo, vazio e exclusivo de demonstração**, é possível executar uma primeira implantação com `DDL_AUTO=update`, verificar a criação e em seguida mudar para `validate`. Nunca usar `create` ou `create-drop` em um banco com dados que precisam ser preservados. Para banco existente, revisar os scripts em `backend/migrations` e comparar o esquema antes de aplicar alterações. Os scripts são incrementais e não substituem um esquema inicial completo.

As fotos novas são armazenadas em `arquivos_foto`, evitando perda no disco temporário do contêiner. Fotos antigas em `backend/uploads` não são transferidas automaticamente: precisam ser reenviadas ou migradas antes de remover o ambiente antigo. O armazenamento no banco atende ao protótipo; acompanhar tamanho e limites do plano.

## Verificação após publicar

Execute na raiz:

```powershell
.\VALIDAR-IMPLANTACAO.ps1 -SiteUrl https://SEU-PROJETO.vercel.app
```

O script exige JSON válido e HTTP 200 da saúde do banco e das consultas públicas. Depois, verificar no navegador cadastro, login, catálogo, upload, criação de peça, reserva, pedido, cancelamento e retorno de estoque. Abrir diretamente `/perfil` deve carregar a interface; `/api/integracoes/mercadolivre/situacao` sem login deve retornar 401, sem virar HTML de sucesso.

## Integrações e limites

- Asaas: consultar [ASAAS-SANDBOX.md](ASAAS-SANDBOX.md). Esta entrega usa exclusivamente Sandbox e uma loja emissora configurada; não é um sistema financeiro multiempresa em produção.
- Mercado Livre: consultar [MERCADO-LIVRE-TESTES.md](MERCADO-LIVRE-TESTES.md). Ainda exige aplicação de desenvolvedor e usuário de teste autorizados.
- Nenhuma implantação deve ser apresentada como homologada antes de verificar o banco e as APIs no endereço público.
- O build atualizado remove os alertas antigos de roteador/Vite, mas a auditoria do npm ainda aponta um advisory transitivo de `picomatch` sem versão corrigida publicada na verificação desta entrega. Não executar `npm audit fix --force` sem revisar as regressões propostas.

Para separar hospedagens, `frontend/scripts/configurar-vercel.mjs` gera rewrites a partir de `API_PUBLIC_URL`; ele é uma alternativa ao contêiner unificado. Não combinar as duas estratégias sem revisar os caminhos `/api`.
