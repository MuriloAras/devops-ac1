# Evidências A2 - práticas 2 e 3

Execução **Jenkins #3: SUCCESS**, em 15/10/2026 às 01:40 (America/Sao_Paulo),
com duração de 2 min 11 s. Commit executado: `1d37dcfeb7767ae2b06420eaebad2ea7fced5938`.
Branch de trabalho: `devops/praticas-2-3`.

- 26 testes de unidade, 8 de integração PostgreSQL e 6 Cypress: **40 aprovados**, sem falhas ou testes ignorados.
- JaCoCo: 165/167 linhas (98,80%), 553/558 instruções (99,10%) e 10/10 branches (100%). PMD: zero ocorrências.
- Seis serviços saudáveis, API `UP`, target Prometheus `UP` e consulta `up` com valor 1 pelo Grafana.
- Capturas do navegador: **cerca de 1280 × 720**. Capturas Cypress: **1600 × 900**. Todas em desktop wide, próximo de 16:9; as dimensões exatas constam no [inventário das capturas](capturas-desktop.json).

## Relatórios e coleta

O [manifesto verificado](p2/build-3-20261015T044637939Z/manifesto-coleta.json) registra build,
commit, resultados, verificações de saúde e SHA-256 de 113 relatórios e mídias.
Os prints do navegador complementam a coleta e mostram a interface real após a execução.

| Conteúdo | Local |
| --- | --- |
| Console, configuração do job e dados do build | [Jenkins](p2/build-3-20261015T044637939Z/jenkins/) |
| JUnit de unidade e integração | [JUnit](p2/build-3-20261015T044637939Z/junit/) |
| Cobertura HTML/XML | [JaCoCo](p2/build-3-20261015T044637939Z/jacoco/) |
| Análise estática | [PMD](p2/build-3-20261015T044637939Z/pmd/) |
| Saúde, containers e Prometheus | [Homologação](p3/build-3-20261015T044637939Z/) |
| Seis prints, vídeo e JUnit E2E | [Cypress](p3/cypress/build-3-20261015T044637939Z/) |
| Datasource, consulta e dashboard exportado | [Grafana](grafana/build-3-20261015T044637939Z/) |
| Plugins instalados | [Inventário](p2/plugins-instalados.json) e [conferência do material](../../docs/CONFERENCIA_PLUGINS_JENKINS.md) |

## Capturas do navegador

| Evidência | Captura desktop |
| --- | --- |
| SCM Git local | [Configuração](p2/01_configuracao_scm_build3_wide.jpg) |
| Branch atual | [Branch](p2/01b_configuracao_branch_build3_wide.jpg) |
| Estágios CI/CD | [Início do fluxo](p2/02_pipeline_build3_wide.jpg) e [final do fluxo](p2/02b_pipeline_final_build3_wide.jpg) |
| Resumo e commit | [Build #3](p2/02_resumo_build3_wide.jpg) |
| 40 testes aprovados | [JUnit](p2/03_junit_build3_wide.jpg) |
| Cobertura publicada | [JaCoCo](p2/04_jacoco_build3_wide.jpg) |
| Fluxo e observabilidade | [Pipeline Overview](p2/05_visao_pipeline_build3_wide.jpg) |
| Plugin JaCoCo 3.3.7 | [Versão instalada](p2/06_plugin_jacoco_build3_wide.jpg) |
| API UP | [Health](p3/01_health_build3_wide.jpg) |
| Target ativo | [Prometheus](p3/02_prometheus_build3_wide.jpg) |
| Quatro painéis | [Dashboard Grafana](grafana/01_dashboard_build3_wide.jpg) |
| Consulta up | [Editor e gráfico](grafana/02_explore_build3_wide.jpg) e [resultado igual a 1](grafana/02b_explore_valor_build3_wide.jpg) |

O [relatório PDF](../../RELATORIO_A2.pdf) e sua [versão Markdown](../../RELATORIO_A2.md)
apresentam os resultados e a análise das práticas. As evidências originais da AC1 permanecem em `evidencias/`.
As capturas anteriores da A2 foram preservadas no histórico local, fora deste conjunto atual.
A branch e as imagens Docker desta execução ainda não foram publicadas em serviços remotos.
