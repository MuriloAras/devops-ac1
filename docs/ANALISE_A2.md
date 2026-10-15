## Prática 2

### Qual a principal diferença entre configurar a esteira no Freestyle e defini-la em Jenkinsfile?

No Freestyle, as etapas são configuradas principalmente nos formulários do job no Jenkins. Com o Jenkinsfile, a sequência de checkout, testes, análise, empacotamento e implantação fica descrita em código no repositório do Super Cursos. Isso permite revisar alterações e recuperar a definição correspondente a cada commit, desde que o nó de execução Jenkins tenha as ferramentas e permissões necessárias.

### Por que versionar o pipeline junto ao código?

O versionamento mantém a esteira alinhada às mudanças da aplicação, dos testes e dos arquivos Docker. Neste trabalho, o job lê o Jenkinsfile de um repositório Git local e registra o commit utilizado; o disparo é manual, sem webhook. Assim, o histórico permite identificar qual configuração foi executada, embora o início da execução dependa da ação do usuário.

### Se o estágio de testes falhar, os estágios de container/deploy devem continuar? Justifique.

Não. Uma falha nos testes JUnit ou de integração com PostgreSQL deve interromper o pipeline antes da criação das imagens e da implantação em homologação. O código de saída do Maven impede o avanço, enquanto os relatórios de testes e cobertura ajudam a investigar o problema. A existência de um relatório JaCoCo ou PMD, por si só, não comprova aprovação funcional.

### Qual estágio representa a transição mais clara entre CI e CD?

Nesta organização da esteira, a transição ocorre no estágio 5, de construção dos containers. A CI reúne checkout, JUnit, testes de integração, JaCoCo, relatório PMD e empacotamento; a partir daí, as imagens da API e do frontend são preparadas para implantação em homologação, seguida de health check e Cypress. A publicação no Docker Hub é uma etapa opcional e não é necessária para executar as imagens no Docker local.

## Prática 3

### Por que um BUILD SUCCESS não garante que a aplicação implantada esteja operacional?

Um BUILD SUCCESS do Maven confirma apenas as etapas executadas naquele build. Mesmo com testes de integração em um PostgreSQL descartável, a implantação em homologação ainda pode apresentar falhas de configuração, rede, portas ou conexão com o banco persistente. Por isso, a esteira também verifica a saúde da API e executa Cypress sobre o fluxo real Vue/Nginx → API Spring Boot → PostgreSQL.

### Qual é a função do Health Check no CD?

O health check verifica se a aplicação está pronta para atender requisições e se suas dependências essenciais respondem. No Super Cursos, o Compose verifica o PostgreSQL e o endpoint `/actuator/health` da API antes de iniciar os serviços dependentes; o Jenkins consulta novamente esse endpoint após o deploy. Essa verificação prepara o ambiente para o Cypress, que valida o comportamento funcional da aplicação.

### Qual é o papel do Docker Compose no ambiente de homologação?

O Docker Compose descreve e conecta PostgreSQL, pgAdmin, API, frontend Nginx, Prometheus e Grafana, além do serviço Cypress utilizado nos testes. Ele concentra configuração de rede, volumes, portas e dependências de inicialização. O Nginx serve a aplicação Vue e encaminha as chamadas `/api` ao backend, que aplica as regras de negócio e persiste os dados no PostgreSQL.

### Qual é a relação entre Prometheus e Grafana? Explique por que o Prometheus coleta/armazena métricas e o Grafana consulta essas métricas para visualização.

O Prometheus consulta periodicamente `/actuator/prometheus` e armazena as métricas como séries temporais. O Grafana usa esse Prometheus como fonte de dados e executa consultas PromQL para apresentar quatro painéis: disponibilidade da coleta da API, requisições HTTP por segundo, tempo médio de resposta e memória heap da JVM. O indicador `up` informa se a coleta funcionou; os demais gráficos precisam ser interpretados junto ao tráfego e ao período observado.

## Encerramento

### Em que ponto da A2 termina a Integração Contínua e começa a Entrega/Implantação Contínua?

Nesta esteira, a CI termina no empacotamento após as verificações de código e testes; a entrega avança com a construção das imagens e sua publicação opcional no Docker Hub. A implantação automatizada ocorre no ambiente de homologação, onde o pipeline inicia os serviços, verifica a saúde da API e executa os testes de ponta a ponta. O job é iniciado manualmente, e o Jenkins automatiza a sequência a partir desse disparo.

### Qual vantagem arquitetural o Jenkinsfile oferece em relação à configuração exclusivamente Freestyle?

O Jenkinsfile torna o processo de entrega uma definição versionada junto à aplicação. Ele explicita a ordem das etapas e as condições de falha, permitindo revisar mudanças e relacionar cada execução a um commit. Isso facilita reconstruir o job em outro Jenkins, desde que também sejam preparados os plugins, o nó de execução, o acesso ao Docker e eventuais credenciais exigidas pela configuração.

### Por que JUnit deve ser executado antes da criação/deploy do container e Cypress depois do deploy?

JUnit permite verificar cedo a lógica e os contratos da aplicação; os testes de integração acrescentam a persistência em um PostgreSQL descartável antes da implantação. Cypress precisa dos serviços implantados para exercitar a aplicação pelo navegador. No Super Cursos, os cenários incluem login válido e inválido, troca com 3 moedas terminando em 0, troca com 5 terminando em 2, bloqueio com saldo 2 e preservação do curso após novo login; o saldo insuficiente também é conferido diretamente na API.

### Como observabilidade se relaciona com a qualidade de uma aplicação após sua implantação?

A observabilidade permite acompanhar o comportamento da aplicação enquanto ela está em execução e complementar os resultados dos testes. Os quatro painéis do Super Cursos ajudam a identificar indisponibilidade de coleta, variações de tráfego, aumento de latência e crescimento do consumo de memória. Esses sinais orientam a investigação, mas não substituem os testes das regras de troca de moedas nem comprovam, isoladamente, a causa de uma falha.

### Qual conhecimento do Exercício A1 foi reutilizado diretamente na construção desta esteira?

Foram reutilizados os cenários BDD e a prática de testar as regras de negócio com JUnit, medir cobertura com JaCoCo e organizar domínio, serviços, controladores e persistência. A configuração Docker inicial e o frontend Vue também serviram de base. Nesta continuação, esses elementos passam a integrar uma esteira Jenkins com análise PMD, testes de integração em PostgreSQL e validação de ponta a ponta com Cypress.

### Conclusão: O que mudou arquiteturalmente do Exercício A1 para o Exercício A2?

A A2 acrescenta ao Super Cursos um processo de entrega descrito em Jenkinsfile, um ambiente Docker completo e verificações automatizadas antes e depois da implantação. O fluxo da aplicação permanece centrado no Vue servido pelo Nginx, na API Spring Boot e no PostgreSQL; a evolução está na integração desse conjunto com testes, relatórios, health checks e monitoramento Prometheus/Grafana. Dessa forma, a avaliação passa a considerar tanto o código quanto o funcionamento do sistema em homologação.
