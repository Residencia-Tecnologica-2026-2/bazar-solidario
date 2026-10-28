from pathlib import Path
import json,html
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,Table,TableStyle,PageBreak,Image,KeepTogether
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.colors import HexColor,white
from reportlab.lib.enums import TA_LEFT
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import pypdfium2 as pdfium
from PIL import ImageChops,Image as PILImage

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs'/'apresentacao';OUT.mkdir(exist_ok=True)
QA=ROOT/'tmp'/'pdf-review';QA.mkdir(parents=True,exist_ok=True)
for name,file in [('Body','calibri.ttf'),('Bold','calibrib.ttf'),('Italic','calibrii.ttf')]:pdfmetrics.registerFont(TTFont(name,'C:/Windows/Fonts/'+file))
pdfmetrics.registerFontFamily('Body',normal='Body',bold='Bold',italic='Italic',boldItalic='Bold')
TEAL=HexColor('#0B6676');ORANGE=HexColor('#F57F3A');CREAM=HexColor('#F1EBCB');BROWN=HexColor('#40291E');BG=HexColor('#FAF9F5');TINT=HexColor('#E6EEEB')
W,H=595.276,841.89
styles={
 'title':ParagraphStyle('title',fontName='Bold',fontSize=25,leading=29,textColor=TEAL,spaceAfter=13),
 'lead':ParagraphStyle('lead',fontName='Body',fontSize=11.3,leading=15.5,textColor=BROWN,spaceAfter=14),
 'h':ParagraphStyle('h',fontName='Bold',fontSize=12.5,leading=16,textColor=TEAL,spaceBefore=9,spaceAfter=5),
 'p':ParagraphStyle('p',fontName='Body',fontSize=10.6,leading=14.4,textColor=BROWN,spaceAfter=7),
 'cell':ParagraphStyle('cell',fontName='Body',fontSize=9.6,leading=12.6,textColor=BROWN),
 'head':ParagraphStyle('head',fontName='Bold',fontSize=9.7,leading=12,textColor=white),
 'small':ParagraphStyle('small',fontName='Body',fontSize=9,leading=12,textColor=BROWN,spaceAfter=8),
 'code':ParagraphStyle('code',fontName='Body',fontSize=10,leading=15,textColor=TEAL,backColor=TINT,borderPadding=9,spaceBefore=5,spaceAfter=14)
}
def para(text,style='p'):return Paragraph(html.escape(text).replace('\n','<br/>'),styles[style])
logo=None
source=Path('C:/Users/victo/Downloads/MarcaIsac.pdf')
if source.exists():
 doc=pdfium.PdfDocument(str(source));im=doc[0].render(scale=1.5).to_pil().convert('RGB');mask=ImageChops.difference(im,PILImage.new('RGB',im.size,'white')).convert('L').point(lambda x:255 if x>25 else 0);box=mask.getbbox()
 if box:im=im.crop(box)
 logo=QA/'logo-isac.png';im.save(logo)

def header(c,doc):
 c.saveState();c.setFillColor(BG);c.rect(0,0,W,H,fill=1,stroke=0)
 c.setFillColor(TEAL);c.rect(0,H-8,W,8,fill=1,stroke=0)
 c.setFillColor(ORANGE);c.rect(42,H-52,5,21,fill=1,stroke=0)
 c.setFont('Bold',10);c.setFillColor(TEAL);c.drawString(56,H-39,'ISAC BRECHÓ')
 c.setFont('Body',8);c.setFillColor(BROWN);c.drawString(56,H-51,'PROJETO INTEGRADOR 3  /  VERSÃO CONSOLIDADA')
 if logo:
  c.drawImage(str(logo),W-118,H-68,width=72,height=44,preserveAspectRatio=True,anchor='c',mask='auto')
 c.setStrokeColor(HexColor('#D7DCD6'));c.line(42,45,W-42,45)
 c.setFont('Body',8);c.setFillColor(BROWN);c.drawString(42,31,'Equipe Bazar Solidário  ·  28/10/2026')
 c.drawRightString(W-42,31,str(doc.page))
 c.restoreState()

def table(t):
 n=len(t['headers']);widths=([118,393] if n==2 else [126,178,207])
 data=[[para(x,'head') for x in t['headers']]]+[[para(x,'cell') for x in row] for row in t['rows']]
 obj=Table(data,colWidths=widths,repeatRows=1,hAlign='LEFT')
 obj.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),TEAL),('ROWBACKGROUNDS',(0,1),(-1,-1),[white,TINT]),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),10),('RIGHTPADDING',(0,0),(-1,-1),10),('TOPPADDING',(0,0),(-1,-1),8),('BOTTOMPADDING',(0,0),(-1,-1),8),('LINEBELOW',(0,0),(-1,0),2,ORANGE)]))
 return obj

def sections(story,items):
 for h,b in items:story.extend([para(h,'h'),para(b)])

def build(path,pages):
 story=[]
 for i,page in enumerate(pages):
  if i:story.append(PageBreak())
  story.extend([para(page['title'],'title'),para(page['lead'],'lead')])
  if i==0 and 'sections' in page:sections(story,page['sections'])
  if 'table' in page:story.extend([table(page['table']),Spacer(1,8)])
  if i!=0 and 'sections' in page:sections(story,page['sections'])
  for text in page.get('codes',[]):story.append(para(text,'code'))
  sections(story,page.get('after',[]))
  if page.get('figure'):story.extend([Spacer(1,8),Image(str(OUT/page['figure']),width=336,height=131.2,hAlign='LEFT')])
  if i==len(pages)-1:story.append(Paragraph('<link href="https://github.com/Residencia-Tecnologica-2026-2/bazar-solidario" color="#0B6676">Repositório oficial no GitHub</link>  ·  <link href="https://www.figma.com/design/UNlqj03qJ6BsaAX9sUwxiL?node-id=249-574" color="#0B6676">Favoritos no Figma</link>',styles['small']))
 doc=SimpleDocTemplate(str(path),pagesize=(W,H),rightMargin=42,leftMargin=42,topMargin=92,bottomMargin=59,title=path.stem.replace('-',' '),author='Equipe Bazar Solidário')
 doc.build(story,onFirstPage=header,onLaterPages=header)

pages=json.loads((ROOT/'docs/historico-conteudo.json').read_text(encoding='utf-8'))
build(OUT/'ISAC-Brecho-Historico-de-Atualizacoes.pdf',pages)
guide=[
{'title':'Abrir o projeto no VS Code','lead':'Este guia executa o aplicativo e a API no computador, com banco local persistente. Para publicar na internet, use as instruções de implantação do repositório.','sections':[['Antes de começar','Instale Git, JDK 21 ou superior, Node.js 22.18 ou superior, PowerShell 7 e VS Code. A primeira execução usa internet para baixar dependências. Extensões recomendadas: Extension Pack for Java e PowerShell.'],['1 Obter e abrir','Clone o repositório com os comandos abaixo. Se usar ZIP, extraia antes e abra ISAC-Brecho.code-workspace em Arquivo > Abrir Workspace do Arquivo. A raiz deve conter frontend e backend.'],['2 Preparar uma única vez','Terminal > Executar Tarefa > ISAC: preparar ambiente (primeira vez). O script verifica ferramentas e instala dependências ausentes. Após mudanças no package-lock, use a opção -Reinstalar.'],['3 Iniciar o aplicativo completo','Terminal > Executar Tarefa > ISAC: iniciar aplicativo completo. Mantenha os dois terminais abertos. Aguarde Started BazarSolidarioApplication e a URL do Vite. Também é possível abrir INICIAR-BACKEND.cmd e INICIAR-FRONTEND.cmd.']],'codes':['git clone https://github.com/Residencia-Tecnologica-2026-2/bazar-solidario.git\ncd bazar-solidario\ncode ISAC-Brecho.code-workspace','./PREPARAR-AMBIENTE.ps1'],'after':[['Endereços','Aplicativo: http://127.0.0.1:4186/  ·  API: http://127.0.0.1:8080/home. Se essas portas já estiverem ocupadas, confira a execução existente antes de iniciar outra.']]},
{'title':'Demonstrar e testar o fluxo','lead':'Use dados fictícios. Uma extração nova do ZIP não contém o banco nem as fotos de testes anteriores: o catálogo começa vazio até uma loja publicar uma peça.','table':{'headers':['Etapa','O que fazer e conferir'],'rows':[['1 Conta LOJA','Cadastre o brechó com WhatsApp. Abra Perfil > Cadastrar e gerenciar minhas peças.'],['2 Publicação','Informe nome, descrição, categoria, preço, estoque e foto. Confira a peça no catálogo.'],['3 Conta CLIENTE','Saia da loja e entre como cliente. Salve e remova a peça dos favoritos para conferir os estados da etiqueta.'],['4 Pedido','Adicione à sacola e registre sem pagamento online. Confira o total e a divisão 10% ISAC / 90% brechó.'],['5 Operação da loja','Volte à LOJA e abra Pedidos recebidos. Aceite ou cancele. Aceite não significa pagamento recebido.'],['6 Estoque','Ao cancelar, confira a unidade devolvida ao catálogo. Repetir o cancelamento não deve devolver duas vezes.'],['7 Persistência','Encerre a API com Ctrl+C e reinicie. Entre novamente se necessário e confira pedido e dados preservados.']]},'sections':[['Onde ficam os dados locais','Banco H2: backend/dados. Fotos: backend/uploads. Chave da sessão: backend/.local-secrets.properties. Esses arquivos são ignorados pelo Git e não devem ser compartilhados. Não rode duas APIs sobre o mesmo banco H2.'],['Verificações automatizadas','As tarefas do workspace incluem testes frontend, build frontend e testes backend. Os comandos equivalentes são npm test, npm run build e mvnw.cmd test, cada um na pasta correspondente.']]},
{'title':'Resolver problemas e publicar','lead':'Confira o terminal que falhou antes de alterar configurações. Não apague o banco nem desative verificações de segurança para tentar corrigir uma inicialização.','table':{'headers':['Sintoma','Ação indicada'],'rows':[['Servidor indisponível','Confira se a API iniciou e se /home responde na porta 8080. Depois confira o proxy /api do frontend.'],['Porta ocupada','Existe outra execução em 4186 ou 8080? Use-a ou encerre conscientemente o terminal correspondente.'],['Sessão expirada','Saia e entre novamente. A chave pode ter mudado ou a validade de oito horas pode ter terminado.'],['Erro no Java','Confira JDK 21+ e JAVA_HOME. Reabra o terminal após instalar ou ajustar o Java.'],['Falha ao baixar dependências','Confira internet e proxy e repita a preparação. Não desative verificação de certificados.'],['H2 em uso','Encerre a execução duplicada. Preserve backend/dados e retome com uma única API.'],['Peça mudou ao editar','Recarregue peças e estoque no painel e reabra a edição. Houve compra ou alteração após abrir o formulário.'],['Catálogo vazio','Publique uma peça com estoque por uma conta LOJA. O catálogo não usa produtos fictícios como fallback.'],['Script bloqueado','Use PowerShell 7 ou os iniciadores .cmd, que chamam pwsh apenas para esse processo.']]},'sections':[['Configuração de implantação','DB_URL, DB_USER, DB_PASSWORD e JWT_SECRET devem ser configurados no servidor. Preserve volumes de banco e uploads, use HTTPS e um proxy /api. Nunca coloque segredos em variáveis VITE_. Consulte IMPLANTACAO.md para Compose e infraestrutura.'],['Limite da validação','A execução nesta máquina usa H2. PostgreSQL, Docker e implantação pública ainda precisam ser validados. Pagamento, entrega e conciliação dos repasses continuam manuais no MVP.']]}
]
build(OUT/'ISAC-Brecho-Guia-VS-Code.pdf',guide)
for filename in ['ISAC-Brecho-Historico-de-Atualizacoes.pdf','ISAC-Brecho-Guia-VS-Code.pdf']:
 d=pdfium.PdfDocument(str(OUT/filename));print(filename,len(d),'páginas')
 for i,page in enumerate(d):page.render(scale=1.1).to_pil().save(QA/(filename.replace('.pdf','')+f'-{i+1}.png'))
