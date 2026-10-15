# Super Cursos

Plataforma de educação continuada com autenticação de alunos e troca de moedas por cursos.
Cada curso custa **3 moedas**. A aplicação usa Vue/Nginx, API Spring Boot e PostgreSQL.

## Execução verificada

O Jenkins build **#3** terminou com **SUCCESS**, executando **26 testes de unidade,
8 testes de integração PostgreSQL e 6 cenários Cypress**. O JaCoCo registrou
**98,80% das linhas e 100% dos branches**, e os seis serviços de homologação ficaram
saudáveis. Commit executado: `1d37dcf`; branch: `devops/praticas-2-3`.

- [Relatório em PDF](RELATORIO_A2.pdf) e [relatório em Markdown](RELATORIO_A2.md).
- [Índice das evidências AC2](evidencias/AC2/README.md).
- [Guia de execução e pipeline](docs/PRATICAS_A2.md).
- [Análise de CI/CD e observabilidade](docs/ANALISE_A2.md).
- [Inventário dos plugins Jenkins](docs/CONFERENCIA_PLUGINS_JENKINS.md).

## Funcionalidades e regras

- Cadastro e autenticação de alunos.
- Consulta do saldo e dos cursos adquiridos.
- Troca de 3 moedas por um curso.
- Recusa da troca quando o saldo é insuficiente.
- Persistência dos cursos e do saldo após novo login.

Os cenários BDD estão na [planilha de requisitos](BDD_Devops_AC1.xlsx):

| Saldo inicial | Operação | Resultado esperado |
| --- | --- | --- |
| 3 moedas | Adquirir um curso | Curso adicionado; saldo 0 |
| 2 moedas | Adquirir um curso | Operação recusada; saldo e cursos inalterados |
| 5 moedas | Adquirir um curso | Curso adicionado; saldo 2 |

## Arquitetura e tecnologias

O Nginx serve o frontend Vue e encaminha `/api` para a API Spring Boot. A API
aplica as regras de negócio e persiste os dados no PostgreSQL. Prometheus coleta
`/actuator/prometheus`; Grafana consulta essas métricas.

- **Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Security e Hibernate.
- **Frontend:** Vue 3, Vite e Nginx.
- **Persistência:** H2 no perfil `dev`; PostgreSQL 16 em `homol`.
- **Testes e qualidade:** JUnit, Mockito, Testcontainers, JaCoCo, PMD e Cypress.
- **Entrega e operação:** Jenkins, Docker Compose, Prometheus e Grafana.
- **API:** Springdoc OpenAPI e Swagger UI.

As camadas do backend separam domínio (`domain.Aluno`), persistência
(`entity.AlunoEntity`), serviços, controladores e DTOs.

## Execução local

Requisitos: JDK 21 e Node compatível com o frontend.

No Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Em outro terminal:

```powershell
cd frontend
npm ci
npm run dev
```

No Linux/macOS, use `./mvnw`. O backend local usa H2 por padrão.

| Serviço local | Endereço |
| --- | --- |
| Frontend Vite | http://localhost:5173 |
| API | http://localhost:8080/api/alunos |
| Swagger | http://localhost:8080/swagger-ui.html |
| Console H2 | http://localhost:8080/h2-console |

No console H2: URL JDBC `jdbc:h2:mem:devopsdb`, usuário `sa` e senha vazia.
O relatório local de cobertura fica em `target/site/jacoco/index.html`.

## Homologação com Docker

```powershell
docker compose up -d --build --wait
docker compose ps
docker compose --profile e2e run --rm --no-deps cypress
```

| Serviço | Endereço |
| --- | --- |
| Frontend Vue/Nginx | http://localhost:3002 |
| API / health | http://localhost:8082/actuator/health |
| Swagger | http://localhost:8082/swagger-ui.html |
| pgAdmin | http://localhost:5051 |
| Prometheus | http://localhost:9092/targets |
| Grafana | http://localhost:3003/d/supercursos-homol |

O PostgreSQL fica acessível apenas na rede Docker. As portas e credenciais podem
ser configuradas a partir de `.env.example`. O [guia de execução](docs/PRATICAS_A2.md)
detalha os perfis, o Jenkins e a coleta de evidências.

Para encerrar os serviços preservando os dados:

```powershell
docker compose stop
```

## Pipeline

O Jenkins lê o `Jenkinsfile` do Git e executa checkout, testes Java/PostgreSQL,
cobertura, análise PMD, empacotamento, construção das imagens, deploy, health checks
e Cypress. Uma falha nos testes impede o avanço. A publicação Docker Hub é opcional.

Os seis cenários Cypress verificam login inválido, login válido, trocas com saldos
3 e 5, bloqueio com saldo 2 e persistência após novo login. Os quatro painéis
Grafana mostram disponibilidade da coleta, HTTP/s, latência média e heap JVM.

## Evidências

Os resultados de CI/CD e observabilidade estão em
[`evidencias/AC2/`](evidencias/AC2/README.md), organizados por build e commit.

Os registros de testes, refatoração e execução da aplicação estão em
`evidencias/AC1/`:

| Registro | Evidência |
| --- | --- |
| TDD: testes falhando | [JUnit RED](evidencias/AC1/evidencias_red_junit.png) |
| TDD: testes aprovados | [JUnit GREEN](evidencias/AC1/evidencias_green_junit.png) |
| TDD: build aprovado | [Maven GREEN](evidencias/AC1/evidencias_green_maven.png) |
| Cobertura do domínio | [JaCoCo GREEN](evidencias/AC1/evidencias_green_jacoco.png) |
| Testes após refatoração | [JUnit BLUE](evidencias/AC1/evidencias_blue_junit.png) |
| Build após refatoração | [Maven BLUE](evidencias/AC1/evidencias_blue_maven.png) |
| Cobertura após refatoração | [JaCoCo BLUE](evidencias/AC1/evidencias_blue_jacoco.png) |
| Cobertura por camada | [JaCoCo](evidencias/AC1/evidencias_jacoco_camadas.png) |
| Consulta no H2 | [Console H2](evidencias/AC1/evidencias_h2.png) |
| Consulta no PostgreSQL | [pgAdmin](evidencias/AC1/evidencias_pgAdmin.png) |
| Construção Docker | [Docker build](evidencias/AC1/evidencias_docker_1.png) |
| Containers ativos | [Docker Compose](evidencias/AC1/evidencias_docker_2.png) |
| Documentação da API | [Swagger](evidencias/AC1/evidencias_swagger.png) |
| Autenticação | [Login](evidencias/AC1/evidencias_frontend_1.png) |
| Saldo e cursos | [Dashboard](evidencias/AC1/evidencias_frontend_2.png) |

## Equipe

Murilo Araujo, Beatriz Silva e Pedro Augusto.
