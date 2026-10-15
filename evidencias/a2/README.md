# Evidências A2 - Super Cursos

Execução final: **Jenkins build #2**, de **15/10/2026 às 01:03:41 (America/Sao_Paulo)**.
Resultado **SUCCESS**, duração **2 min 17 s**, commit **f7ab240a731e1e5c67b313cc44732d607efeea62**.
Job: [A2_P2_supercursos_pipeline](http://localhost:9090/job/A2_P2_supercursos_pipeline/2/).

| Verificação | Resultado comprovado |
| --- | --- |
| Testes Java | 26 unidade + 8 integração, sem falhas/erros/pulos |
| Cypress | 6 cenários, sem falhas; seis capturas e um vídeo |
| JaCoCo | Linhas 165/167 = 98,80%; instruções 553/558 = 99,10%; branches 10/10 = 100% |
| PMD | Sem ocorrências nas regras padrão; relatório sem gate de violações |
| Docker HOMOL | Seis serviços running/healthy, imagens API/frontend com tag 2 |
| Monitoramento | API UP; target supercursos-api UP; consulta up=1; quatro painéis Grafana |

## Prática 2 - Jenkins e qualidade

- [Resumo do build aprovado e revisão Git](p2/02_resumo_build2.jpg).
- [Configuração do job com SCM e branch](p2/01_configuracao_job.jpg).
- [Relatório JUnit publicado: 40 testes aprovados](p2/03_junit_build2.jpg).
- [Resumo JaCoCo publicado](p2/04_jacoco_build2.jpg).
- [Stage View - trecho inicial](p2/02_pipeline_build2.jpg) e [visão gráfica parcial](p2/05_visao_pipeline_build2.jpg).
- [Estágios completos em JSON](p2/build-2-20261015T040708726Z/jenkins/stages.json): todos os ativos aprovados; Docker Hub opcional não executado.
- [Console completo](p2/build-2-20261015T040708726Z/jenkins/console.txt), [configuração XML](p2/build-2-20261015T040708726Z/jenkins/job-config.xml) e [metadados do build](p2/build-2-20261015T040708726Z/jenkins/build.json).
- [Relatório HTML JaCoCo](p2/build-2-20261015T040708726Z/jacoco/index.html) e [XML original](p2/build-2-20261015T040708726Z/jacoco/jacoco.xml).
- [PMD HTML](p2/build-2-20261015T040708726Z/pmd/pmd.html) e [PMD XML](p2/build-2-20261015T040708726Z/pmd/pmd.xml).
- [Manifesto verificado e hashes SHA-256 dos relatórios/mídias](p2/build-2-20261015T040708726Z/manifesto-coleta.json).

## Prática 3 - aplicação e observabilidade

- [Health no navegador](p3/01_health_build2.jpg) e [resposta JSON](p3/build-2-20261015T040708726Z/health.json).
- [Containers - texto](p3/build-2-20261015T040708726Z/containers.txt) e [JSON](p3/build-2-20261015T040708726Z/containers.json).
- [Cypress: JUnit, screenshots e vídeo](p3/cypress/build-2-20261015T040708726Z/).
- [Prometheus target UP](p3/02_prometheus_build2.jpg).
- [Grafana Explore: consulta up=1](grafana/02_explore_build2.jpg).
- [Dashboard com quatro painéis](grafana/01_dashboard_build2.jpg).
- [Exportações e respostas das APIs Grafana](grafana/build-2-20261015T040708726Z/).

As capturas do Stage View/Overview são parciais devido à rolagem da interface; o JSON e o console preservam a execução inteira.
Os arquivos pertencem ao Super Cursos. As imagens antigas da AC1 não foram reutilizadas como prova desta execução.
O coletor consulta somente os serviços e copia os artefatos do build, verificando o commit e removendo senhas dos textos.

O arquivo `.env` fica fora do Git. As credenciais de exemplo servem apenas ao laboratório local com dados fictícios.
Para repetir a coleta após outro build aprovado: `scripts/coletar-evidencias.ps1`.
