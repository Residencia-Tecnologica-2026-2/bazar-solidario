# ISAC Brechó — Bazar Solidário

Projeto Integrador 3 · Residência Tecnológica · parceria com a ISAC.

Aplicativo React + TypeScript + Vite e API Java 21 + Spring Boot. Cadastro de cliente e brechó, JWT, catálogo e lojas persistidos, agenda de encontros, fotos autenticadas, painel de anúncios e pedidos com confirmação manual.

## Documentação para a equipe e a ONG

- [Histórico de atualizações em PDF](docs/apresentacao/ISAC-Brecho-Historico-de-Atualizacoes.pdf): registro detalhado de hoje desde as 14h, com todos os ícones, frontend, backend, banco, APIs, arquivos e evidências.
- [Histórico em texto](docs/HISTORICO-ATUALIZACOES.md): versão consultável no GitHub.
- [Guia VS Code em PDF](docs/apresentacao/ISAC-Brecho-Guia-VS-Code.pdf) e [guia passo a passo](GUIA-VSCODE.md).
- [Implantação](IMPLANTACAO.md): ambiente, PostgreSQL, volumes, proxy e limites de validação.

## Abrir no computador

Abra `ISAC-Brecho.code-workspace` no VS Code. Requisitos: Java 21, Node.js 22.18+ e PowerShell 7. Instale as dependências com `npm ci` em `frontend/`.

Na primeira execução, use a tarefa **ISAC: preparar ambiente (primeira vez)**.

Execute `INICIAR-BACKEND.cmd` e `INICIAR-FRONTEND.cmd` em duas janelas, ou a tarefa “ISAC: iniciar aplicativo completo” do VS Code.

- Aplicativo: http://127.0.0.1:4186/
- Painel do brechó: http://127.0.0.1:4186/loja/produtos
- Pedidos: http://127.0.0.1:4186/pedidos
- API: http://127.0.0.1:8080/home

O modo local usa H2 em arquivo (`backend/dados/`) e gera uma chave JWT aleatória em arquivo ignorado pelo Git. Não apague esse arquivo para preservar as sessões. Não execute duas APIs no mesmo banco local.

## Compra do MVP

Um pedido contém peças de um único brechó. O servidor calcula preços, reserva estoque e registra **10% para a ISAC / 90% para o brechó**, com arredondamento em centavos e soma exata. O pedido começa em `AGUARDANDO_CONFIRMACAO_MANUAL`. A loja pode aceitar ou cancelar; o cliente pode cancelar enquanto aguarda aceite. Cancelamento devolve estoque uma única vez.

**Nenhum cartão, PIX, cobrança ou transferência é processado.** Aceite não é quitação. Pagamento, entrega/retirada e conciliação do repasse são manuais. Os valores registrados são a divisão prevista do pedido; pedidos cancelados não representam receita.

## Organização

- `frontend/src/components/`: formulários, painel da loja, checkout, pedidos, lojas e agenda.
- `frontend/src/services/`: HTTP, sessão, anúncios e pedidos.
- `frontend/src/App.tsx`: rotas, catálogo integrado, favoritos e sacola (uma unidade por peça na interface).
- `frontend/public/icons/isac/`: ícones originais do projeto.
- `backend/src/main/java/.../controller/`: endpoints HTTP.
- `backend/src/main/java/.../service/`: autorização por proprietário e regras transacionais.
- `backend/src/main/java/.../model/` e `repository/`: entidades e persistência.
- `backend/src/test/` e `frontend/tests/`: testes automatizados.
- `Tarefas da equipe/`: planejamento da equipe existente.

Consulte `INTEGRACAO-LOCAL.md` para os contratos e `IMPLANTACAO.md` para configuração e publicação. Testes: `npm test` / `npm run build` em frontend; `mvnw.cmd test` em backend. A validação desta máquina usa H2; PostgreSQL e Docker precisam ser verificados no ambiente de implantação.
