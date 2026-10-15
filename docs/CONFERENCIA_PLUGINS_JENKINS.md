# Conferência dos plugins Jenkins

Conferência em 15/10/2026, por consulta somente leitura ao Jenkins local **2.580.1**.
Foi mantido o **JaCoCo 3.3.7**, conforme autorização do aluno para continuar com a
versão atual caso não houvesse uma versão docente explícita confirmada. Nenhum
plugin foi instalado, atualizado ou removido nesta conferência.

## Referência informada e cópia local

Referência enviada pelo aluno: [arquivo 5860904 no Canvas](https://facens.instructure.com/courses/18468/files/5860904?module_item_id=613764).
A página do arquivo foi aberta no Canvas e confirmou o título da apresentação;
a leitura do anexo foi feita pela cópia local
`C:\Users\pedro\Downloads\Assets\1_1_ExA1_Jenkins_Projeto_Integracao_CI_Junit_Jacoco_Plugins.pptx`.

A origem dessa cópia foi confirmada pelos metadados locais do download:
`ReferrerUrl` aponta ao arquivo Canvas `5860904`, e o caminho da CDN termina no
mesmo nome do PPTX. O PDF local de módulos também associa o item `613764` ao
título dessa apresentação. Isso confirma a procedência do download; não comprova
que o arquivo remoto atual permaneça idêntico à cópia local.

SHA-256 do PPTX local:
`2366c28713a53f2fbc7097c7096584bd6b10fd46544bef4d432d3fb9e6d3a03e`.

Nos slides 22–24 da cópia local, as capturas mostram a ação **Record JaCoCo
coverage report**, com caminhos para `.exec`, classes e fontes. O slide 6 mostra
Jenkins **2.516.3** em uma captura de tela. Não foi encontrada nesses trechos uma
versão numérica exigida para o plugin JaCoCo, nem uma exigência do plugin Coverage.
O template acadêmico local cita Git, Maven Integration, JUnit e JaCoCo na prática 1,
sem fixar suas versões. Não foi presumida uma versão docente a partir das imagens.

## Inventário relevante

Os plugins presentes abaixo estavam **instalados, ativos e habilitados**:

| Plugin / ID | Versão instalada |
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

Não estavam instalados: `coverage`, `code-coverage-api`, `warnings-ng`, `pmd`,
`htmlpublisher` e `maven-plugin` (Maven Integration).

O [inventário JSON](../evidencias/a2/p2/plugins-instalados.json) registra a data da
consulta, as versões e os estados dos plugins, sem credenciais ou tokens.

## Uso na esteira atual

O [Jenkinsfile](../Jenkinsfile) executa Maven pelo Wrapper, publica Java e Cypress
com `junit` e publica a cobertura com `jacoco`, consumindo `target/jacoco.exec`,
`target/classes` e `src/main/java`. O PMD é executado por `pmd:pmd`; seus relatórios
são arquivados por `archiveArtifacts`. Não são usados `recordCoverage`,
`recordIssues`, `publishHTML` ou passos do Maven Integration.

O plugin Jenkins `jacoco` e o `jacoco-maven-plugin` são componentes distintos:
o Maven gera os dados e relatórios; o Jenkins os publica. A ausência de um plugin
Coverage ou PMD não impede o mecanismo atualmente implementado. Esta conferência
registra a configuração existente, sem afirmar equivalência com uma versão
docente que não tenha sido explicitamente confirmada.
