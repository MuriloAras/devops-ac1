# CI/CD e observabilidade

## Pipeline as Code

### Qual a principal diferença entre configurar a esteira no Freestyle e defini-la em Jenkinsfile?

No Freestyle, as etapas são configuradas nos formulários do job. No Super Cursos,
o `Jenkinsfile` descreve checkout, testes, análise, empacotamento e implantação no
Git, permitindo revisar mudanças e recuperar a definição de cada execução.

### Por que versionar o pipeline junto ao código?

O versionamento mantém a esteira alinhada à aplicação, aos testes e aos arquivos
Docker. O job registra o commit utilizado, permitindo identificar a configuração
executada. O disparo atual é manual, sem webhook ou polling.

### Se o estágio de testes falhar, os estágios de container/deploy devem continuar? Justifique.

Não. Uma falha nos testes JUnit ou de integração PostgreSQL interrompe o pipeline
antes da construção das imagens e do deploy. O código de saída do Maven impede
o avanço; os relatórios JUnit e JaCoCo permitem investigar a falha.

### Qual estágio representa a transição mais clara entre CI e CD?

O estágio 5, de construção dos containers. A CI reúne checkout, testes, cobertura,
PMD e empacotamento do JAR. As imagens da API e do frontend seguem para homologação,
health checks e Cypress. A publicação Docker Hub é opcional.

## Containers e homologação

### Por que um BUILD SUCCESS não garante que a aplicação implantada esteja operacional?

O sucesso do Maven confirma as verificações executadas, mas a implantação pode
apresentar problemas de configuração, rede ou conexão com o banco persistente.
Os health checks e o Cypress verificam o ambiente implantado:
Vue/Nginx → API Spring Boot → PostgreSQL.

### Qual é a função do Health Check no CD?

Verificar se os serviços e suas dependências estão prontos. O Compose aguarda
o PostgreSQL e o endpoint `/actuator/health` da API antes de iniciar serviços
dependentes. O Jenkins consulta a saúde da API novamente após o deploy.

### Qual é o papel do Docker Compose no ambiente de homologação?

Definir e conectar PostgreSQL, pgAdmin, API, frontend Nginx, Prometheus e Grafana,
além do Cypress. O Compose concentra rede, volumes, portas, variáveis e
dependências de inicialização. O Nginx serve o Vue e encaminha `/api` ao backend.

### Qual é a relação entre Prometheus e Grafana?

O Prometheus coleta `/actuator/prometheus` e armazena as métricas como séries
temporais. O Grafana consulta essas séries e apresenta disponibilidade da coleta,
HTTP/s, latência média e heap JVM. A métrica `up=1` confirma o sucesso da coleta.

## Integração e operação

### Em que ponto termina a Integração Contínua e começa a Entrega/Implantação Contínua?

A CI termina no empacotamento, após os testes e verificações de código. A entrega
avança com a construção das imagens e a publicação opcional no Docker Hub.
A implantação em homologação inclui inicialização dos serviços, health checks
e testes E2E. O Jenkins automatiza essa sequência após o disparo manual do job.

### Qual vantagem arquitetural o Jenkinsfile oferece em relação à configuração exclusivamente Freestyle?

O `Jenkinsfile` explicita a ordem das etapas e as condições de falha, relacionando
cada execução a um commit. Para reproduzir o job, o nó de execução precisa dos
plugins, ferramentas, acesso ao Docker e credenciais indicados na configuração.

### Por que JUnit deve ser executado antes da criação/deploy do container e Cypress depois do deploy?

JUnit verifica a lógica e os contratos antes da implantação; os testes de integração
validam a persistência em PostgreSQL descartável. Cypress usa os serviços
implantados para exercitar login, troca de moedas, saldo insuficiente e
persistência dos cursos pelo navegador e pela API.

### Como observabilidade se relaciona com a qualidade de uma aplicação após sua implantação?

Os quatro painéis permitem acompanhar indisponibilidade de coleta, variações de
tráfego, latência e consumo de memória durante a execução. As métricas orientam
a investigação e complementam os testes das regras de negócio.

### Como os componentes formam o fluxo de entrega do Super Cursos?

O Git fornece o código e o `Jenkinsfile`. O Jenkins executa testes, relatórios e
build das imagens; o Compose implanta o ambiente; o Cypress verifica os fluxos
funcionais; Prometheus e Grafana permitem acompanhar o sistema em homologação.
