from pathlib import Path
import importlib.util,json
import pypdfium2 as pdfium
root=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location("design_doc",root/"tools/gerar-documentos.py")
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
m.HEADER_LABEL="PROJETO INTEGRADOR 3  /  HISTÓRICO DO DIA"
m.FOOTER_LABEL="28/10/2026  ·  Recorte 14h às 16h34  ·  UTC-3"
pages=json.loads((root/"docs/historico-conteudo.json").read_text(encoding="utf-8"))
output=root/"docs/apresentacao/ISAC-Brecho-Historico-de-Atualizacoes.pdf"
m.build(output,pages)
pdf=pdfium.PdfDocument(str(output))
print(len(pdf),"páginas")
qa=root/"tmp/pdf-review-hoje";qa.mkdir(exist_ok=True,parents=True)
for i,page in enumerate(pdf):page.render(scale=1.1).to_pil().save(qa/f"pagina-{i+1:02}.png")
