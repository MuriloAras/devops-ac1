# Ambiente Jenkins

Jenkins **2.580.1**. Job: `A2_P2_supercursos_pipeline`.

## Plugins instalados

| Plugin / ID | Versão |
| --- | --- |
| JaCoCo / `jacoco` | `3.3.7` |
| JUnit / `junit` | `1438.v6a_ccb_4b_11004` |
| Pipeline / `workflow-aggregator` | `608.v67378e9d3db_1` |
| Pipeline Declarative / `pipeline-model-definition` | `2.2293.v6e7193cec599` |
| Stage View / `pipeline-stage-view` | `2.41` |
| Pipeline Graph View / `pipeline-graph-view` | `1053.v9df19e6ceed2` |
| Git / `git` | `5.11.0` |
| Credentials Binding / `credentials-binding` | `728.v902a_273b_8947` |
| Timestamper / `timestamper` | `1.30` |

[Inventário dos plugins](../evidencias/AC2/p2/plugins-instalados.json).

## Build e relatórios

| Componente | Configuração |
| --- | --- |
| Maven | Wrapper do projeto |
| JaCoCo Maven | `0.8.13` |
| PMD Maven | `3.28.0` |
| Testes | JUnit e Cypress publicados por `junit` |
| Cobertura | `jacoco`, com `target/jacoco.exec`, `target/classes` e `src/main/java` |
| Análise estática | `pmd:pmd`, com relatórios arquivados por `archiveArtifacts` |

Pipeline: [Jenkinsfile](../Jenkinsfile).
