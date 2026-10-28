from pathlib import Path
import importlib.util,json
import pypdfium2 as pdfium
root=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location("design_doc",root/"tools/gerar-documentos.py")
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
m.HEADER_LABEL="PROJETO INTEGRADOR 3  /  RESUMO PARA APRESENTAÇÃO"
m.FOOTER_LABEL="ISAC Brechó  ·  Atualizações de 28/10/2026 desde as 14h"
page={"title":"O que mudou no ISAC Brechó","lead":"O trabalho de hoje conectou as telas aos dados do sistema e organizou o fluxo entre cliente e brechó. Agora é possível publicar uma peça, solicitar um pedido e acompanhar a confirmação da loja no ambiente local.","table":{"headers":["Mudança","Benefício para quem usa"],"widths":[140,371],"rows":[
["Interface e ícones","Aplicamos os ícones originais do projeto, corrigimos alinhamentos e espaçamento dos formulários. Favoritos agora mostra claramente quando uma peça está salva ou não."],
["Cadastro e acesso","Corrigimos a comunicação do cadastro e do login. Cliente e brechó têm acessos diferentes; cada loja administra apenas suas próprias peças e pedidos."],
["Painel do brechó","A loja consegue publicar fotos, informar preço e estoque, editar peças e ocultar anúncios. Isso dá autonomia para manter o catálogo atualizado."],
["Dados conectados","Catálogo, lojas e encontros consultam os dados do servidor. Cadastros e pedidos ficam armazenados e continuam disponíveis após reiniciar o sistema."],
["Pedidos e estoque","O pedido é registrado e aguarda o aceite do brechó. As peças são reservadas; cancelar devolve o estoque. Há proteção contra pedidos repetidos e disputa da última unidade."],
["Participação da ISAC","O servidor registra a divisão de 10% para a ISAC e 90% para o brechó. Em um pedido de R$ 85,00: R$ 8,50 para a ISAC e R$ 76,50 para o brechó."]]},"sections":[
["Como demonstrar em dois minutos","1. Publique uma peça pelo painel do brechó. 2. Entre como cliente, salve a peça e registre um pedido. 3. Volte à loja, mostre o aceite ou cancelamento e explique a divisão dos valores."],
["Verificação realizada","Passaram 111 testes automatizados: 63 no backend e 48 na interface. Também conferimos o fluxo no navegador, o retorno do estoque e a permanência dos dados após reiniciar."],
["O que ainda é manual","Aceitar o pedido não significa receber pagamento. Pagamento, entrega/retirada e repasse à ISAC são combinados e conciliados manualmente. O banco de produção e a publicação na internet ainda precisam de validação; os testes locais usaram H2."]
]}
page["after"]=page.pop("sections")
output=root/"docs/apresentacao/ISAC-Brecho-Resumo-para-Empresa.pdf"
m.build(output,[page])
pdf=pdfium.PdfDocument(str(output));print(len(pdf),"páginas")
qa=root/"tmp/pdf-review-resumo";qa.mkdir(exist_ok=True,parents=True)
for i,p in enumerate(pdf):p.render(scale=1.4).to_pil().save(qa/f"pagina-{i+1}.png")
md=["# O que mudou no ISAC Brechó","",page["lead"],"","| Mudança | Benefício |","|---|---|"]
md += ["|"+"|".join(r)+"|" for r in page["table"]["rows"]]
for h,b in page["after"]:md += ["","## "+h,"",b]
(root/"docs/RESUMO-PARA-EMPRESA.md").write_text("\n".join(md)+"\n",encoding="utf-8")
