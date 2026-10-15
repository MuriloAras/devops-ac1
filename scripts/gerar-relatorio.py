"""Gera o relatorio A2 em PDF e Markdown a partir de uma coleta verificada.

Dependencias: reportlab e Pillow. Execute a partir de qualquer diretorio.
As capturas do navegador devem estar em evidencias/a2 antes da geracao.
"""
import json
import re
import xml.etree.ElementTree as ET
from datetime import datetime, timezone, timedelta
from pathlib import Path
from xml.sax.saxutils import escape

from PIL import Image as PILImage
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image,
    PageBreak, KeepTogether,
)
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'evidencias' / 'a2'
collections = []
for path in EVIDENCE.glob('p2/build-*/manifesto-coleta.json'):
    collected = json.loads(path.read_text(encoding='utf-8-sig'))
    if collected.get('status') == 'VERIFICADO' and collected.get('checks') and all(collected['checks'].values()):
        collections.append((path, collected))
if not collections:
    raise SystemExit('Execute primeiro scripts/coletar-evidencias.ps1 com build SUCCESS.')
manifest_path, manifest = max(collections, key=lambda item: (
    int(item[1]['buildTimestamp']), int(item[1]['build']), item[1].get('collectedAtUtc', ''),
))
BUILD = manifest['build']
COMMIT = manifest['commit']
# Os snapshots acompanham a entrega; caminhos absolutos do computador de origem
# presentes no manifesto nao precisam existir em outra maquina.
capture_id = manifest_path.parent.name
P2 = manifest_path.parent
P3 = EVIDENCE / 'p3' / capture_id
CYPRESS = EVIDENCE / 'p3' / 'cypress' / capture_id
GRAFANA = EVIDENCE / 'grafana' / capture_id
for directory in (P2, P3, CYPRESS, GRAFANA):
    if not directory.is_dir():
        raise SystemExit(f'Snapshot da coleta mais recente incompleto: {directory}')
JACOCO = ET.parse(P2 / 'jacoco' / 'jacoco.xml').getroot()
counters = {c.attrib['type']: c.attrib for c in JACOCO.findall('counter')}

def coverage(kind):
    c = counters[kind]
    covered, missed = int(c['covered']), int(c['missed'])
    return f'{100 * covered / (covered + missed):.2f}%'.replace('.', ',')

FONTS = Path('C:/Windows/Fonts')
if (FONTS / 'arial.ttf').exists():
    for name, file in [('Body', 'arial.ttf'), ('Body-Bold', 'arialbd.ttf'), ('Body-Italic', 'ariali.ttf')]:
        pdfmetrics.registerFont(TTFont(name, str(FONTS / file)))
    pdfmetrics.registerFontFamily('Body', normal='Body', bold='Body-Bold', italic='Body-Italic', boldItalic='Body-Bold')
else:
    for name, face in [('Body', 'Helvetica'), ('Body-Bold', 'Helvetica-Bold'), ('Body-Italic', 'Helvetica-Oblique')]:
        pdfmetrics.registerFont(pdfmetrics.Font(name, face, 'WinAnsiEncoding'))
    pdfmetrics.registerFontFamily('Body', normal='Helvetica', bold='Helvetica-Bold', italic='Helvetica-Oblique', boldItalic='Helvetica-BoldOblique')

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='BodyA2', fontName='Body', fontSize=10, leading=14, spaceAfter=8, textColor=colors.HexColor('#233044')))
styles.add(ParagraphStyle(name='TitleA2', fontName='Body-Bold', fontSize=26, leading=31, textColor=colors.HexColor('#142238'), spaceAfter=14))
styles.add(ParagraphStyle(name='H1A2', fontName='Body-Bold', fontSize=18, leading=23, textColor=colors.HexColor('#142238'), spaceBefore=4, spaceAfter=12, keepWithNext=True))
styles.add(ParagraphStyle(name='H2A2', fontName='Body-Bold', fontSize=11, leading=15, textColor=colors.HexColor('#1d4b66'), spaceBefore=9, spaceAfter=6, keepWithNext=True))
styles.add(ParagraphStyle(name='CaptionA2', fontName='Body', fontSize=8, leading=11, spaceAfter=12, textColor=colors.HexColor('#526174')))
styles.add(ParagraphStyle(name='SmallA2', fontName='Body', fontSize=8, leading=11, spaceAfter=5))
styles.add(ParagraphStyle(name='KickerA2', fontName='Body-Bold', fontSize=10, leading=14, textColor=colors.HexColor('#c14663'), spaceAfter=10))
story = []
markdown = []
width = A4[0] - 88

def para(text, style='BodyA2'):
    safe = escape(text)
    safe = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', safe)
    story.append(Paragraph(safe, styles[style]))

def heading(text):
    story.append(Paragraph(escape(text), styles['H1A2']))
    markdown.append(f'\n## {text}\n')

def text(text):
    para(text)
    markdown.append(text + '\n')

def table(headers, rows, colwidths):
    data = [[Paragraph(escape(str(x)), styles['SmallA2']) for x in headers]]
    data += [[Paragraph(escape(str(x)), styles['SmallA2']) for x in row] for row in rows]
    t = Table(data, colWidths=colwidths, repeatRows=1, hAlign='LEFT')
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#e7edf3')),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('LINEBELOW', (0, 0), (-1, 0), .6, colors.HexColor('#b6c4d4')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f6f8fb')]),
        ('LEFTPADDING', (0, 0), (-1, -1), 8), ('RIGHTPADDING', (0, 0), (-1, -1), 8),
        ('TOPPADDING', (0, 0), (-1, -1), 7), ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.extend([t, Spacer(1, 12)])
    markdown.extend(['| ' + ' | '.join(headers) + ' |', '| ' + ' | '.join('---' for _ in headers) + ' |'])
    markdown.extend('| ' + ' | '.join(map(str, row)) + ' |' for row in rows)
    markdown.append('')

def figure(path, caption, max_height=380):
    path = Path(path)
    if not path.is_file():
        raise FileNotFoundError(f'Captura obrigatoria ausente: {path}')
    with PILImage.open(path) as im:
        iw, ih = im.size
    scale = min(width / iw, max_height / ih)
    image = Image(str(path), width=iw * scale, height=ih * scale)
    image.hAlign = 'CENTER'
    story.append(KeepTogether([image, Spacer(1, 6), Paragraph(escape(caption), styles['CaptionA2'])]))
    markdown.append(f'![{caption}]({path.relative_to(ROOT).as_posix()})\n')

def browser_capture(section, name):
    for suffix in ('_wide', ''):
        for extension in ('.jpg', '.png', '.jpeg'):
            path = EVIDENCE / section / f'{name}_build{BUILD}{suffix}{extension}'
            if path.is_file():
                return path
    raise FileNotFoundError(
        f'Captura obrigatoria ausente: {section}/{name}_build{BUILD}_wide.jpg '
        f'(ou o nome anterior sem _wide).'
    )

def newpage():
    story.append(PageBreak())

def architecture():
    d = Drawing(width, 180)
    palette = ['#e8eff9', '#fff0e9', '#e6f4ec']
    rows = [
        (142, ['Git / Jenkinsfile', 'Jenkins / testes', 'Imagens Docker', 'Deploy HOMOL']),
        (84, ['Cypress E2E', 'Vue + Nginx', 'API Spring Boot', 'PostgreSQL 16']),
        (26, ['Saúde + métricas', 'API /actuator', 'Prometheus', 'Grafana']),
    ]
    boxw, gap = (width - 24) / 4, 8
    for index, (y, labels) in enumerate(rows):
        for j, label in enumerate(labels):
            x = j * (boxw + gap)
            d.add(Rect(x, y, boxw, 30, rx=5, fillColor=colors.HexColor(palette[index]), strokeColor=colors.HexColor('#afbdcc'), strokeWidth=.6))
            d.add(String(x + boxw / 2, y + 12, label, fontName='Body', fontSize=8.1, textAnchor='middle', fillColor=colors.HexColor('#203148')))
            if j < 3:
                start, end = x + boxw, x + boxw + gap
                d.add(Line(start, y + 15, end, y + 15, strokeColor=colors.HexColor('#526174')))
                d.add(Polygon([end, y + 15, end - 3, y + 17, end - 3, y + 13], fillColor=colors.HexColor('#526174')))
    return d

date = datetime.fromtimestamp(manifest['buildTimestamp'] / 1000, timezone(timedelta(hours=-3))).strftime('%d/%m/%Y às %H:%M')
para('DEVOPS & QA  |  EXERCÍCIO A2', 'KickerA2')
para('Super Cursos', 'TitleA2')
para('CI/CD e observabilidade', 'H1A2')
markdown.append('# Super Cursos - CI/CD e observabilidade\n')
text('Equipe: Murilo Araujo, Beatriz Silva e Pedro Augusto.')
text(f'Práticas 2 e 3 | Jenkins build #{BUILD} | {date} (America/Sao_Paulo).')
text(f'Commit executado: {COMMIT}. Repositório de origem: https://github.com/MuriloAras/devops-ac1.')
text('O sistema da AC1 foi ampliado com um pipeline versionado, testes de integração PostgreSQL, containers do frontend e backend, Cypress e monitoramento. A validação inicial da AC1 foi dispensada pelo aluno; esta entrega registra as novas práticas e as execuções efetivamente realizadas.')
table(['Verificação', 'Resultado'], [
    ['Jenkins', f'SUCCESS - build #{BUILD}'],
    ['JUnit / PostgreSQL', f"{manifest['junit']['unitarios']['tests']} testes de unidade + {manifest['junit']['integracao']['tests']} de integração; zero falhas"],
    ['Cypress', f"{manifest['junit']['cypress']['tests']} cenários aprovados; seis capturas e vídeo"],
    ['JaCoCo', f"Linhas {coverage('LINE')}; branches {coverage('BRANCH')}"],
    ['Homologação', 'Seis serviços saudáveis; API UP; Prometheus up=1; Grafana operacional'],
], [130, width - 130])
newpage()
heading('Prática 2 - Pipeline as Code')
text('Job: A2_P2_supercursos_pipeline. O Jenkins usa Pipeline script from SCM e lê o Jenkinsfile da branch devops/praticas-2-3. Nesta máquina, o SCM aponta à cópia Git local, vinculada ao repositório de origem; o disparo do build é manual. Não há webhook nem polling configurado.')
story.append(architecture())
para('Fluxo da entrega e topologia em homologação. O Cypress usa o frontend real; o Nginx encaminha /api ao backend. As métricas seguem da API ao Prometheus e ao Grafana.', 'CaptionA2')
table(['Estágio', 'Responsabilidade'], [
    ['0 - Preflight', 'Verificar Java, Git, Docker Engine e Compose.'],
    ['1 - Checkout', 'Obter a revisão e registrar job, build, commit e tag.'],
    ['2 - CI', 'Executar JUnit e PostgreSQL/Testcontainers; publicar JUnit e JaCoCo.'],
    ['3 - PMD', 'Gerar e arquivar análise estática.'],
    ['4 - Package', 'Empacotar o JAR após aprovação dos testes.'],
    ['5 - Containers', 'Construir API Java 21 e frontend Vue/Nginx.'],
    ['6 - Registry', 'Publicação Docker Hub opcional; desabilitada nesta execução.'],
    ['7 - Deploy', 'Subir HOMOL e aguardar saúde dos seis serviços.'],
    ['8 - Health', 'Confirmar /actuator/health e registrar containers.'],
    ['9 - Cypress', 'Validar seis cenários na aplicação implantada.'],
    ['10 - Observabilidade', 'Apresentar URLs para a conferência das métricas e painéis.'],
], [112, width - 112])
text('Uma falha de teste interrompe o avanço para os estágios seguintes. O PMD gera relatório sem gate de violações, e não foi criado um limiar arbitrário de cobertura. As imagens usam a tag do número do build. A publicação remota de imagens permaneceu desabilitada.')

newpage()
heading(f'Evidência do Jenkins - build #{BUILD}')
figure(browser_capture('p2', '02_resumo'), f'Figura 1. Execução final no Jenkins, vinculada ao commit {COMMIT[:7]}.', 525)
text('O console integral, a configuração XML do job, o JSON dos estágios e os relatórios estão preservados na pasta de evidências P2 desta execução. As capturas antigas da AC1 permanecem separadas.')

newpage()
heading('Qualidade e testes automatizados')
text('Os testes de integração usam PostgreSQL 16 em container temporário e porta aleatória, sem acessar o volume de homologação. Verificam persistência, cursos, regras de saldo, e-mail único e respostas HTTP de saúde, métricas e OpenAPI.')
figure(browser_capture('p2', '03_junit'), 'Figura 2. Relatório JUnit publicado no Jenkins: testes Java e Cypress, sem falhas.', 500)

newpage()
heading('Cobertura JaCoCo e análise PMD')
text(f"O XML JaCoCo registra linhas {coverage('LINE')}, instruções {coverage('INSTRUCTION')} e branches {coverage('BRANCH')}. O plugin Jenkins arredonda os percentuais na apresentação. Duas linhas do método main existente não foram cobertas.")
figure(browser_capture('p2', '04_jacoco'), 'Figura 3. Resumo JaCoCo publicado no build final do Jenkins.', 500)
text('O relatório PMD desta execução não registrou ocorrências nas regras padrão. Os XML e HTML acompanham as evidências para conferência dos resultados, além das imagens.')
text('O Jenkins publica a cobertura pelo plugin JaCoCo 3.3.7; o Maven gera os dados com jacoco-maven-plugin 0.8.13. A cópia local da apresentação indicada no Canvas mostra o uso de Record JaCoCo coverage report, sem uma versão numérica obrigatória identificada. A conferência e o inventário estão em docs/CONFERENCIA_PLUGINS_JENKINS.md.')

newpage()
heading('Prática 3 - Containers e homologação')
text('O projeto Compose supercursos-homol separa containers, rede e volumes do laboratório anterior. O frontend é Vue estático servido por Nginx, com proxy /api para api:8080. O backend usa o perfil homol com PostgreSQL; o perfil dev mantém H2 por padrão.')
table(['Serviço', 'Acesso local'], [
    ['Frontend', 'http://localhost:3002'], ['API / health', 'http://localhost:8082/actuator/health'],
    ['pgAdmin', 'http://localhost:5051'], ['Prometheus', 'http://localhost:9092/targets'],
    ['Grafana', 'http://localhost:3003/d/supercursos-homol'], ['PostgreSQL 16', 'Somente rede interna Docker'],
], [115, width - 115])
text('Health check registrado em /actuator/health: {"groups":["liveness","readiness"],"status":"UP"}. A resposta JSON e a captura original do navegador estão preservadas em evidencias/a2/p3/.')
text('A coleta registra os seis containers em execução e saudáveis. Os healthchecks abrangem API, frontend, PostgreSQL, pgAdmin, Prometheus e Grafana. Os arquivos containers.txt e containers.json preservam os estados e as portas reais.')

newpage()
heading('Cypress - regras do Super Cursos')
table(['Cenário', 'Comprovação'], [
    ['Login inválido', 'HTTP 401 e permanência na tela de login.'],
    ['Login válido', 'Saldo inicial e lista de cursos apresentados.'],
    ['3 moedas', 'Curso adquirido e saldo final zero.'],
    ['5 moedas', 'Curso adquirido e saldo final duas moedas.'],
    ['2 moedas', 'Botão bloqueado, API 400 e dados inalterados.'],
    ['Persistência', 'Curso e saldo mantidos após sair e entrar.'],
], [100, width - 100])
shots = sorted(CYPRESS.glob('screenshots/**/*04-troca-5-moedas-saldo-dois*.png'))
if not shots:
    shots = sorted(CYPRESS.glob('screenshots/**/*.png'))
figure(shots[0], 'Figura 4. Captura produzida pelo Cypress: operação real de troca de moedas pela interface.', 330)
text('Cada cenário cria um aluno fictício com e-mail único pela API. Os testes não substituem respostas por mocks; observam as requisições reais e verificam os dados persistidos. O JUnit, as seis capturas de sucesso e o vídeo estão na pasta Cypress da execução.')

newpage()
heading('Prometheus e Grafana')
text('O Prometheus coleta /actuator/prometheus do serviço api:8080 sob o job supercursos-api. O Grafana consulta esse datasource. A verificação registrou o target UP e a consulta up{job="supercursos-api"} com valor 1.')
figure(browser_capture('p3', '02_prometheus'), 'Figura 5. Target da API ativo no Prometheus.', 250)
figure(browser_capture('grafana', '02b_explore_valor'), 'Figura 6. Grafana Explore confirma up=1 pelo datasource Prometheus.', 280)

newpage()
heading('Dashboard de observabilidade')
text('Os quatro painéis mostram disponibilidade da coleta, requisições HTTP por segundo, tempo médio de resposta e memória JVM Heap. Foi gerado tráfego real na aplicação e aguardada a coleta; as consultas e o dashboard exportado acompanham as evidências.')
figure(browser_capture('grafana', '01_dashboard'), 'Figura 7. Dashboard SuperCursos - Observabilidade HOMOL, com quatro painéis provisionados.', 540)

newpage()
heading('Análise das práticas e encerramento')
analysis = (ROOT / 'docs' / 'ANALISE_A2.md').read_text(encoding='utf-8-sig')
markdown.append(analysis)
buffer = []
def flush():
    if buffer:
        para(' '.join(buffer))
        buffer.clear()
for line in analysis.splitlines():
    if line.startswith('# '):
        continue
    if line.startswith('## '):
        flush()
        if line[3:] in ['Prática 3', 'Encerramento']:
            newpage()
        para(line[3:], 'H1A2')
    elif line.startswith('### '):
        flush(); para(line[4:], 'H2A2')
    elif line.strip():
        buffer.append(line.strip())
    else:
        flush()
flush()

newpage()
heading('Rastreabilidade e reprodução')
text(f"Manifesto da coleta: {manifest_path.relative_to(ROOT).as_posix()}. Status: VERIFICADO. Build #{BUILD}, commit {COMMIT}.")
text('A configuração do job aponta ao Git local desta máquina. A branch contém as alterações; a origem remota é MuriloAras/devops-ac1. A publicação desta branch no GitHub e o Docker Hub não fazem parte da execução local registrada aqui.')
text('Para reproduzir: siga docs/PRATICAS_A2.md, configure o job com scripts/configurar-jenkins.ps1, execute o build e, após SUCCESS, rode scripts/coletar-evidencias.ps1. O arquivo .env não é versionado e não acompanha o SCM ao workspace Jenkins.')
text('O coletor verifica o build e o commit, os resultados JUnit e Cypress, a saúde da API e o monitoramento, e registra hashes SHA-256 dos relatórios e mídias. As capturas do navegador complementam esses arquivos. As credenciais exibidas no exemplo são exclusivas do laboratório local.')
text('Todas as novas capturas usam formato desktop wide, próximo de 16:9: cerca de 1280 x 720 no navegador e 1600 x 900 no Cypress. O índice evidencias/a2/README.md lista os arquivos e suas dimensões. Os relatórios de execuções anteriores foram preservados no histórico local, e esta entrega usa a coleta do build atual.')
text('Referências técnicas: documentação Jenkins de Pipeline e API remota (jenkins.io/doc); matriz de compatibilidade Springdoc (springdoc.org); documentação Docker Compose (docs.docker.com/compose); documentação Cypress (docs.cypress.io); Prometheus e Grafana (prometheus.io/docs e grafana.com/docs).')

def decorate(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor('#c14663'))
    canvas.setLineWidth(2)
    canvas.line(44, A4[1] - 32, A4[0] - 44, A4[1] - 32)
    canvas.setFillColor(colors.HexColor('#68778a'))
    canvas.setFont('Body', 8)
    canvas.drawString(44, 25, f'Super Cursos | DevOps & QA | Build #{BUILD}')
    canvas.drawRightString(A4[0] - 44, 25, str(doc.page))
    canvas.restoreState()

output = ROOT / 'RELATORIO_A2.pdf'
doc = SimpleDocTemplate(str(output), pagesize=A4, rightMargin=44, leftMargin=44,
    topMargin=48, bottomMargin=42, title='Super Cursos - Práticas 2 e 3 de CI/CD',
    author='Murilo Araujo, Beatriz Silva e Pedro Augusto')
doc.build(story, onFirstPage=decorate, onLaterPages=decorate)
(ROOT / 'RELATORIO_A2.md').write_text('\n'.join(markdown), encoding='utf-8')
print(json.dumps({'pdf': str(output), 'markdown': str(ROOT / 'RELATORIO_A2.md'), 'build': BUILD, 'commit': COMMIT}, ensure_ascii=False))
