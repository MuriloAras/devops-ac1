# Super Cursos - praticas 2 e 3

Este laboratorio aplica ao projeto da AC1 o pipeline Jenkins, a entrega em homologacao,
os testes E2E e o monitoramento. A validacao inicial da AC1 foi dispensada pelo aluno;
os testes abaixo verificam as novas configuracoes e continuam obrigatorios no CI.

## Requisitos

- JDK 21 no `JAVA_HOME` do agente Jenkins, Git e Docker Desktop/Engine com Compose v2.
- Jenkins com Pipeline, Git, JUnit, JaCoCo, Credentials Binding, Timestamper e Stage View.
- Acesso do mesmo usuario que executa Jenkins ao Docker.
- Para executar Maven no Windows: `mvnw.cmd`; Linux/macOS: `./mvnw`.

Node e Cypress sao executados em containers durante o pipeline. Para desenvolvimento
local do Vue, use Node compativel com o `package-lock.json` e `npm ci`.

## Repositorio e job Jenkins

Origem do codigo: https://github.com/MuriloAras/devops-ac1. A branch de trabalho local
e `codex/praticas-2-3`. O laboratorio pode usar SCM Git local para executar mudancas
comitadas antes da publicacao remota. Nesse modo, o Jenkins deve rodar nesta maquina
e ter a opcao `-Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true`, como no laboratorio anterior.

O script cria SOMENTE o job dedicado; nao altera jobs anteriores nem a autenticacao:

```powershell
.\scripts\iniciar-jenkins.ps1
.\scripts\configurar-jenkins.ps1
```

O primeiro script reaproveita a instalacao em `$env:USERPROFILE\Jenkins\jenkins.war`,
inicia em segundo plano e usa JDK 21. Parametros `-JenkinsWar` e `-JavaHome` permitem
apontar outras instalacoes. Se Jenkins ja estiver ativo, o script apenas informa isso.

Se o servidor exigir autenticacao, defina `JENKINS_USER` e `JENKINS_TOKEN` apenas no
ambiente do processo. Para usar um repositorio remoto depois de publicar a branch:

```powershell
.\scripts\configurar-jenkins.ps1 -AtualizarJob `
  -RepositoryUrl https://github.com/MuriloAras/devops-ac1.git
```

Abra http://localhost:9090/job/A2_P2_supercursos_pipeline/ e execute o build.
O disparo e manual; nao ha webhook nem polling configurado neste laboratorio.

## Execucao e estagios

0. Preflight: JDK, Git, Docker Engine e Compose.
1. Checkout da revisao e registro do commit/build.
2. JUnit, PostgreSQL real com Testcontainers, JaCoCo; publica JUnit e cobertura.
3. PMD: gera e arquiva o relatorio. Nao ha gate de violacoes nem cobertura minima arbitraria.
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
sao exclusivas do laboratorio local e os dados dos testes sao ficticios.

O `.env` do checkout nao e copiado pelo SCM ao workspace Jenkins. Personalizacoes de
CI precisam ser definidas no ambiente do agente ou no workspace efetivamente usado.
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
porta no host. Os nomes, portas e volumes nao reutilizam a stack Jogo Enigma nem a AC1 antiga.

```powershell
docker compose -f docker-compose.homol.yml build api frontend
docker compose -f docker-compose.homol.yml up -d --wait postgres pgadmin api frontend prometheus grafana
docker compose -f docker-compose.homol.yml --profile e2e run --rm --no-deps cypress
docker compose -f docker-compose.homol.yml ps
```

`docker compose ... stop` encerra os servicos preservando dados. Nao use `down -v`
para uma parada normal. Os testes Testcontainers usam PostgreSQL temporario separado.

## Evidencias e entrega

As evidencias novas ficam em `evidencias/a2/`; as da AC1 permanecem em `evidencias/`.
O Jenkins arquiva JUnit, JaCoCo, PMD, JAR, resultados Cypress e metadados da execucao.

- `p2/`: configuracao do job, console completo, commit/build, estagios e relatorios.
- `p3/`: health, containers, frontend, Cypress e target Prometheus.
- `grafana/`: Explore, dashboard e exportacoes JSON.

As capturas devem identificar a pagina e o resultado. Para o monitoramento, gere
requisicoes reais na aplicacao, aguarde o scrape e confirme `up{job="supercursos-api"}=1`.
O dashboard provisionado contem disponibilidade, HTTP/s, latencia media e heap JVM.
O sucesso do stage 10, sozinho, nao comprova a coleta dessas metricas.

O relatorio final deve registrar somente resultados efetivamente obtidos, com
legendas ligando cada captura ao criterio demonstrado e ao build utilizado.
