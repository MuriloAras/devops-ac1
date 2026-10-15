# Super Cursos - execucao e CI/CD

O pipeline Jenkins valida e implanta a aplicacao em homologacao e executa os testes E2E.
Prometheus e Grafana monitoram o ambiente. Os testes de unidade e integracao verificam a
aplicacao e as configuracoes do ambiente em cada execucao do CI.

## Requisitos

- JDK 21 no `JAVA_HOME` do nó de execução Jenkins, Git e Docker Desktop/Engine com Compose v2.
- Jenkins com Pipeline, Git, JUnit, JaCoCo, Credentials Binding, Timestamper e Stage View.
- Acesso do mesmo usuario que executa Jenkins ao Docker.
- Para executar Maven no Windows: `mvnw.cmd`; Linux/macOS: `./mvnw`.

Node e Cypress sao executados em containers durante o pipeline. Para desenvolvimento
local do Vue, use Node compativel com o `package-lock.json` e `npm ci`.

## Repositorio e job Jenkins

Origem do codigo: https://github.com/MuriloAras/devops-ac1. A branch de trabalho local
e `devops/praticas-2-3`. O job pode usar SCM Git local para executar mudancas
comitadas antes da publicacao remota. Nesse modo, o Jenkins deve rodar nesta maquina
e ter a opcao `-Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true`.

Para iniciar o Jenkins e configurar o job:

```powershell
.\scripts\iniciar-jenkins.ps1
.\scripts\configurar-jenkins.ps1
```

O primeiro script usa a instalacao em `$env:USERPROFILE\Jenkins\jenkins.war`,
inicia em segundo plano e usa JDK 21. Parametros `-JenkinsWar` e `-JavaHome` permitem
apontar outras instalacoes. Se Jenkins ja estiver ativo, o script apenas informa isso.

Se o servidor exigir autenticacao, defina `JENKINS_USER` e `JENKINS_TOKEN` apenas no
ambiente do processo. Para usar um repositorio remoto depois de publicar a branch:

```powershell
.\scripts\configurar-jenkins.ps1 -AtualizarJob `
  -RepositoryUrl https://github.com/MuriloAras/devops-ac1.git
```

Abra http://localhost:9090/job/A2_P2_supercursos_pipeline/ e execute o build.
O disparo e manual; nao ha webhook nem polling configurado no job.

## Execucao e estagios

0. Preflight: JDK, Git, Docker Engine e Compose.
1. Checkout da revisao e registro do commit/build.
2. JUnit, PostgreSQL real com Testcontainers, JaCoCo; publica JUnit e cobertura.
3. PMD: gera e arquiva o relatorio, sem bloqueio por violacoes ou limite minimo de cobertura configurado.
4. Package: gera o JAR apos os testes aprovados.
5. Build das imagens da API e do Vue servido pelo Nginx.
6. Docker Hub opcional, desabilitado por padrao.
7. Deploy no projeto Compose `supercursos-homol`, aguardando os healthchecks.
8. Health check HTTP e listagem dos containers.
9. Cypress em container, com JUnit, capturas de sucesso/falha e video.
10. URLs para a verificacao efetiva do monitoramento.

Os testes falhos impedem os estagios seguintes. O parametro `PUSH_DOCKER_HUB` so deve
ser habilitado com namespace proprio e credencial Jenkins `dockerhub-supercursos`.
Nenhuma publicacao e necessaria para a execucao local.

## Ambientes

`dev` usa H2 por padrao. O Compose DEV oferece PostgreSQL separado como opcao.
`homol` usa PostgreSQL 16 e volume proprio. Copie `.env.example` para `.env` se quiser
personalizar as configuracoes locais; `.env` fica fora do Git. As credenciais padrao
sao destinadas a execucao local e os dados dos testes sao ficticios.

O `.env` do checkout nao e copiado pelo SCM ao workspace Jenkins. Personalizacoes de
CI precisam ser definidas no ambiente de execução Jenkins ou no workspace efetivamente usado.
Para optar pelo PostgreSQL DEV em vez do H2, defina no processo do backend
`SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/supercursos_dev`,
`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` e
`SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver`.

| Servico HOMOL | Endereco local |
| --- | --- |
| Frontend Vue/Nginx | http://localhost:3002 |
| API health | http://localhost:8082/actuator/health |
| Swagger | http://localhost:8082/swagger-ui.html |
| pgAdmin | http://localhost:5051 |
| Prometheus | http://localhost:9092/targets |
| Grafana | http://localhost:3003 |

O Nginx encaminha `/api` para `api:8080` na rede Docker. O banco HOMOL nao publica
porta no host. O projeto Compose `supercursos-homol` possui rede e volumes proprios.

```powershell
docker compose -f docker-compose.homol.yml build api frontend
docker compose -f docker-compose.homol.yml up -d --wait postgres pgadmin api frontend prometheus grafana
docker compose -f docker-compose.homol.yml --profile e2e run --rm --no-deps cypress
docker compose -f docker-compose.homol.yml ps
```

`docker compose ... stop` encerra os servicos preservando dados. Nao use `down -v`
para uma parada normal. Os testes Testcontainers usam PostgreSQL temporario separado.

## Evidencias

Os resultados estao organizados em `evidencias/AC1/` e `evidencias/AC2/`.
O [indice AC2](../evidencias/AC2/README.md) identifica as capturas e a execucao verificada.
O Jenkins arquiva JUnit, JaCoCo, PMD, JAR, resultados Cypress e metadados da execucao.

- `evidencias/AC2/p2/`: configuracao do job, console, commit/build, estagios e relatorios.
- `evidencias/AC2/p3/`: health, containers, frontend, Cypress e target Prometheus.
- `evidencias/AC2/grafana/`: Explore, dashboard e exportacoes JSON.

Para coletar os resultados do ultimo build aprovado nessa estrutura:

```powershell
.\scripts\coletar-evidencias.ps1 -OutputRoot .\evidencias\AC2
```

O parametro `-OutputRoot` define o destino da coleta. O padrao do script e
`evidencias/a2/`; os caminhos dos artefatos arquivados sao definidos pelo Jenkinsfile.

Para verificar o monitoramento, gere
requisicoes reais na aplicacao, aguarde o scrape e confirme `up{job="supercursos-api"}=1`.
O dashboard provisionado contem disponibilidade, HTTP/s, latencia media e heap JVM.
O sucesso do stage 10, sozinho, nao comprova a coleta dessas metricas.

As coletas registram build, commit, resultados e hashes dos arquivos.
