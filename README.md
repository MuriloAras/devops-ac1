# Exercício A2 - Super Cursos

Branch: `Exercicio-A2`.

## Exercício proposto

Aplicar as práticas de CI/CD ao projeto Super Cursos da AC1, a partir da prática 2:
versionar a esteira em um `Jenkinsfile`, construir os containers, implantar em
homologação, executar testes antes e depois do deploy e registrar as evidências.
O ambiente também inclui monitoramento com Prometheus e Grafana.

A aplicação mantém a proposta original: autenticação de alunos e troca de moedas
por cursos. Cada curso custa 3 moedas; saldo insuficiente impede a aquisição.

## O que mudou em relação à versão anterior

| Versão anterior - AC1 | Versão atual - A2 |
| --- | --- |
| Testes e builds executados manualmente | Esteira Jenkins com etapas e condições de falha definidas em código |
| JUnit e evidências de TDD | JUnit, integração com PostgreSQL/Testcontainers e seis cenários Cypress |
| Containers da API, PostgreSQL e pgAdmin; frontend em desenvolvimento | Ambiente de homologação com frontend Vue/Nginx, API, banco e monitoramento |
| Validação manual após iniciar os serviços | Health checks e testes E2E após o deploy |
| Relatórios locais de testes e cobertura | JUnit, JaCoCo e PMD arquivados no Jenkins |
| Prints da aplicação e dos testes | Prints, vídeo, logs, relatórios e estados dos serviços organizados por execução |

## Como ocorreu a execução

O job `A2_P2_supercursos_pipeline` foi iniciado manualmente e leu o código de um
repositório Git local. A sequência foi:

`Checkout → JUnit/PostgreSQL → JaCoCo/PMD → Package → Build Docker → Deploy HOMOL → Health checks → Cypress → Observabilidade`

Uma falha nos testes interrompe o avanço. O Docker Compose inicia o frontend,
API, PostgreSQL, pgAdmin, Prometheus e Grafana. O Cypress verifica login válido
e inválido, trocas com saldos 3 e 5, bloqueio com saldo 2 e persistência após novo
login. A publicação no Docker Hub é opcional e ficou desabilitada na execução
registrada. A esteira faz checkout de um commit existente; os arquivos de
entrega foram salvos no Git após a execução.

## Resultado registrado

Jenkins **build #3: SUCCESS**, em **15/10/2026**, com duração de **2 min 11 s**,
sobre o commit `1d37dcf`.

- **40 testes aprovados:** 26 de unidade, 8 de integração PostgreSQL e 6 Cypress.
- **JaCoCo:** 98,80% das linhas e 100% dos branches. **PMD:** zero ocorrências.
- **Homologação:** seis serviços saudáveis e API `UP`.
- **Monitoramento:** target Prometheus `UP`, consulta `up=1` no Grafana e quatro painéis.

## Onde estão as evidências

- [`evidencias/AC1/`](evidencias/AC1/): TDD, cobertura, bancos, Docker, Swagger e telas da versão anterior.
- [`evidencias/AC2/p2/`](evidencias/AC2/p2/): configuração e resumo do Jenkins, estágios, console, testes, JaCoCo e PMD.
- [`evidencias/AC2/p3/`](evidencias/AC2/p3/): saúde da API, containers, Prometheus e resultados, capturas e vídeo do Cypress.
- [`evidencias/AC2/grafana/`](evidencias/AC2/grafana/): dashboard, consulta Explore e exportações do monitoramento.

Os diretórios `build-3-20261015T044637939Z` contêm os arquivos da execução registrada.
O arquivo `manifesto-coleta.json`, dentro de `evidencias/AC2/p2/`, registra o build,
o commit, os resultados e os hashes dos relatórios e mídias. Os prints estão em
formato desktop wide; as capturas do Cypress têm 1600 × 900 pixels.

## Como executar novamente

Requisitos: JDK 21, Git, Docker com Compose e Jenkins com acesso ao Docker.
No Windows, a partir da raiz do projeto:

```powershell
.\scripts\iniciar-jenkins.ps1
.\scripts\configurar-jenkins.ps1 -AtualizarJob
```

Acesse `http://localhost:9090/job/A2_P2_supercursos_pipeline/` e execute o build.
Após `SUCCESS`, a coleta pode ser feita com:

```powershell
.\scripts\coletar-evidencias.ps1 -OutputRoot .\evidencias\AC2
```

Acessos em homologação: frontend `http://localhost:3002`, API
`http://localhost:8082/actuator/health`, pgAdmin `http://localhost:5051`,
Prometheus `http://localhost:9092/targets` e Grafana
`http://localhost:3003/d/supercursos-homol`.

Equipe: Murilo Araujo, Beatriz Silva e Pedro Augusto.
