# Desafio 6 — CI/CD e observabilidade

O `Jenkinsfile` está configurado para executar testes Java com PostgreSQL, JaCoCo e PMD; construir Core, interface, serviço Python e gateway MQTT; rodar os testes Python; implantar a homologação; verificar saúde; rodar Cypress; e arquivar resultados. Prometheus coleta métricas do Core, serviço Python e gateway MQTT; Grafana apresenta a operação.

Para demonstrar uma falha observável com o ambiente local ativo, rode `sh evidencias/B1/desafio-6/simular-indisponibilidade-ia.sh`. O serviço Python para, o Core responde com contingência sem falhar a requisição e `supercursos_ai_fallback_total` sobe no Prometheus. O script reinicia o serviço ao terminar. A mesma condição é coberta automaticamente por `AiServiceClientTest`.

As etapas de build, testes Java/Python, Compose, saúde, Cypress e métricas foram executadas no ambiente local. O servidor Jenkins não estava iniciado em `localhost:9090`, então não há um novo número de build Jenkins para registrar nesta execução. O pipeline único é proporcional ao MVP modular e mantém gates comuns; Docker Hub é opcional. Se Python ou MQTT ganharem autonomia de release ou volume, o pipeline poderá ser dividido por componente, mantendo contratos e verificações compartilhadas.
