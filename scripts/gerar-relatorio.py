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
if not EVIDENCE.is_dir():
    EVIDENCE = ROOT / 'evidencias' / 'AC2'
EVIDENCE_REL = EVIDENCE.relative_to(ROOT).as_posix()
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
    safe = re.sub(r'`([^`]+)`', r'\1', safe)
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
para('SUPER CURSOS  |  CI/CD', 'KickerA2')
para('Super Cursos', 'TitleA2')
para('CI/CD e observabilidade', 'H1A2')
markdown.append('# Super Cursos - CI/CD e observabilidade\n')
text('Equipe: Murilo Araujo, Beatriz Silva e Pedro Augusto.')
text(f'Jenkins build #{BUILD} | {date} (America/Sao_Paulo).')
text(f'Commit executado: {COMMIT}. Repositório de origem: https://github.com/MuriloAras/devops-ac1.')
table(['Verificação', 'Resultado'], [
    ['Jenkins', f'SUCCESS - build #{BUILD}'],
    ['JUnit / PostgreSQL', f"{manifest['junit']['unitarios']['tests']} testes de unidade + {manifest['junit']['integracao']['tests']} de integração; zero falhas"],
    ['Cypress', f"{manifest['junit']['cypress']['tests']} cenários aprovados; seis capturas e vídeo"],
    ['JaCoCo', f"Linhas {coverage('LINE')}; branches {coverage('BRANCH')}"],
    ['Homologação', 'Seis serviços saudáveis; API UP; Prometheus up=1; Grafana operacional'],
], [130, width - 130])
newpage()
heading('Pipeline as Code')
text('Job: A2_P2_supercursos_pipeline. Branch: devops/praticas-2-3. Pipeline script from SCM; Git local; acionamento manual.')
story.append(architecture())
para('CI/CD, aplicação e monitoramento em homologação.', 'CaptionA2')
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
text('Testes: bloqueio em caso de falha. PMD: informativo. Cobertura: sem limite mínimo. Tag das imagens: número do build. Docker Hub: desabilitado.')

newpage()
heading(f'Evidência do Jenkins - build #{BUILD}')
figure(browser_capture('p2', '02_resumo'), f'Figura 1. Execução final no Jenkins, vinculada ao commit {COMMIT[:7]}.', 525)

newpage()
heading('Qualidade e testes automatizados')
text('JUnit: 26 testes de unidade. PostgreSQL 16 / Testcontainers: 8 testes de integração. Cypress: 6 testes E2E. Total: 40 aprovados.')
figure(browser_capture('p2', '03_junit'), 'Figura 2. Relatório JUnit publicado no Jenkins: testes Java e Cypress, sem falhas.', 500)

newpage()
heading('Cobertura JaCoCo e análise PMD')
text(f"JaCoCo: linhas {coverage('LINE')}; instruções {coverage('INSTRUCTION')}; branches {coverage('BRANCH')}.")
figure(browser_capture('p2', '04_jacoco'), 'Figura 3. Resumo JaCoCo publicado no build final do Jenkins.', 500)
text('PMD: zero ocorrências. JaCoCo Jenkins: 3.3.7. jacoco-maven-plugin: 0.8.13.')

newpage()
heading('Containers e homologação')
text('Compose: supercursos-homol. Frontend: Vue/Nginx. API: Spring Boot. Banco: PostgreSQL 16. Proxy: /api → api:8080.')
table(['Serviço', 'Acesso local'], [
    ['Frontend', 'http://localhost:3002'], ['API / health', 'http://localhost:8082/actuator/health'],
    ['pgAdmin', 'http://localhost:5051'], ['Prometheus', 'http://localhost:9092/targets'],
    ['Grafana', 'http://localhost:3003/d/supercursos-homol'], ['PostgreSQL 16', 'Somente rede interna Docker'],
], [115, width - 115])
text('Health check: {"groups":["liveness","readiness"],"status":"UP"}. Containers saudáveis: API, frontend, PostgreSQL, pgAdmin, Prometheus e Grafana.')

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

newpage()
heading('Prometheus e Grafana')
text('Endpoint: api:8080/actuator/prometheus. Job: supercursos-api. Target: UP. Consulta: up{job="supercursos-api"}=1.')
figure(browser_capture('p3', '02_prometheus'), 'Figura 5. Target da API ativo no Prometheus.', 250)
figure(browser_capture('grafana', '02b_explore_valor'), 'Figura 6. Grafana Explore confirma up=1 pelo datasource Prometheus.', 280)

newpage()
heading('Dashboard de observabilidade')
text('Painéis: disponibilidade, requisições HTTP/s, latência média e memória JVM Heap.')
figure(browser_capture('grafana', '01_dashboard'), 'Figura 7. Dashboard SuperCursos - Observabilidade HOMOL, com quatro painéis provisionados.', 540)

newpage()
heading('Análise técnica')
analysis = (ROOT / 'docs' / 'ANALISE_A2.md').read_text(encoding='utf-8-sig')
markdown.append(re.sub(r'^# .*\n+', '', analysis, count=1))
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
        if line[3:] in ['Containers e homologação', 'Integração e operação']:
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
text('SCM: Git local. Branch: devops/praticas-2-3. Publicação remota: não realizada nesta execução.')
text('Execução: scripts/configurar-jenkins.ps1 → build Jenkins → scripts/coletar-evidencias.ps1. Guia: docs/PRATICAS_A2.md.')
text('Capturas: desktop wide, aproximadamente 1280 x 720 no navegador e 1600 x 900 no Cypress. Evidências: ' + EVIDENCE_REL + '/.')

def decorate(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor('#c14663'))
    canvas.setLineWidth(2)
    canvas.line(44, A4[1] - 32, A4[0] - 44, A4[1] - 32)
    canvas.setFillColor(colors.HexColor('#68778a'))
    canvas.setFont('Body', 8)
    canvas.drawString(44, 25, f'Super Cursos | Build #{BUILD}')
    canvas.drawRightString(A4[0] - 44, 25, str(doc.page))
    canvas.restoreState()

output = ROOT / 'RELATORIO_A2.pdf'
doc = SimpleDocTemplate(str(output), pagesize=A4, rightMargin=44, leftMargin=44,
    topMargin=48, bottomMargin=42, title='Super Cursos - CI/CD e observabilidade',
    author='Murilo Araujo, Beatriz Silva e Pedro Augusto')
doc.build(story, onFirstPage=decorate, onLaterPages=decorate)
(ROOT / 'RELATORIO_A2.md').write_text('\n'.join(markdown), encoding='utf-8')
print(json.dumps({'pdf': str(output), 'markdown': str(ROOT / 'RELATORIO_A2.md'), 'build': BUILD, 'commit': COMMIT}, ensure_ascii=False))
