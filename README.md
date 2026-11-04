# Super Cursos

Este README apresenta a decisão do Desafio 1 do exercício B1. O projeto mantém uma única aplicação principal **modular** e organiza seu código por funcionalidades. O registro detalhado dos seis desafios, incluindo os diagramas completos e as demais decisões, está em [docs/arquitetura/README.md](docs/arquitetura/README.md).

**Repositório:** [github.com/MuriloAras/devops-ac1](https://github.com/MuriloAras/devops-ac1)

## Como rodar a aplicação

**Requisitos:** Docker Desktop instalado e aberto. O primeiro início precisa de internet para baixar e montar os serviços. Não é necessário instalar Java ou Node para rodar a aplicação pelo Docker.

O comando abaixo inicia a homologação, um ambiente para conferir a aplicação antes de uma entrega.

Na pasta principal do projeto, rode:

```bash
docker compose -f docker-compose.homol.yml up --build -d
docker compose -f docker-compose.homol.yml ps
```

Quando os serviços estiverem prontos, acesse:

| Serviço | Endereço | Acesso inicial |
| --- | --- | --- |
| Aplicação | [http://localhost:3002](http://localhost:3002) | — |
| API | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) | — |
| PgAdmin | [http://localhost:5051](http://localhost:5051) | `admin@admin.com` / `admin_local` |
| Prometheus | [http://localhost:9092](http://localhost:9092) | — |
| Grafana | [http://localhost:3003](http://localhost:3003) | `admin` / `admin_local` |

Para conectar o PgAdmin ao banco que roda no Docker, use o host `postgres`, porta `5432`, banco `supercursos_homol`, usuário `supercursos` e senha `supercursos_local`.

Para parar a aplicação, rode:

```bash
docker compose -f docker-compose.homol.yml down
```

Os dados do banco ficam salvos em um volume do Docker e continuam lá quando os serviços são parados dessa forma. Para apagar também os dados salvos, use `docker compose -f docker-compose.homol.yml down -v`. As portas e senhas podem ser alteradas em um arquivo `.env`, criado a partir de `.env.example`.

## 1. Leitura do cenário

O Super Cursos começou como um MVP e agora considera um cenário de até 100.000 usuários. O Ranking pode crescer mais que as outras funções, enquanto cursos, moedas, conquistas e recompensas também continuam evoluindo.

A pergunta principal foi: precisamos separar o sistema em vários serviços agora, ou organizar melhor o código dentro da aplicação que já existe? Como ainda não temos medições que mostrem um gargalo, escolhemos primeiro organizar o Core — a parte principal do sistema — em módulos.

## 2. RFs afetados

| ID | Requisito funcional | Impacto no desafio |
| --- | --- | --- |
| RF01 | Ranking | Registrar pontuações e mostrar a classificação dos alunos. |
| RF02 | Conquistas e gamificação | Manter a evolução de conquistas e recompensas junto às demais funções do Core. |

## 3. RNFs e seleção dos ASRs

RNFs são qualidades esperadas do sistema. Um RNF é um ASR quando pode influenciar diretamente a organização ou a operação da aplicação. Para este desafio, selecionamos três:

| RNF | ASR? | Justificativa |
| --- | --- | --- |
| Manutenibilidade | Sim | Separar o código por funcionalidade ajuda a localizar regras e reduz o impacto de mudanças. |
| Escalabilidade | Sim, como necessidade futura | A fronteira do Ranking permite acompanhar seu crescimento e considerar uma separação se os dados mostrarem que é preciso. |
| Complexidade operacional | Sim | Uma equipe de três pessoas precisa conseguir manter e executar a solução. |
| Desempenho | Não neste momento | É importante, mas ainda não temos medições que mostrem um problema de desempenho causado pela arquitetura. |
| Testabilidade | Não como decisão macro | É importante e os testes podem ser organizados por módulo, mas não exige separar a aplicação em serviços. |

## 4. RPCs — restrições e premissas

| RPC | Consequência para a decisão |
| --- | --- |
| O Core já usa Java e Spring Boot | Aproveitar a aplicação existente e evoluir aos poucos. |
| O projeto tem uma equipe de três pessoas | Manter uma solução que a equipe consiga operar. |
| O banco relacional atual deve continuar | Não trocar o banco sem uma necessidade demonstrada. |
| A aplicação da AC1 é o ponto de partida | Preservar seu comportamento e seus contratos. |
| A previsão é de até 100.000 usuários, sem teste de carga que comprove um gargalo | Não tratar a previsão, sozinha, como motivo para criar serviços separados. |

## 5. Alternativas e trade-offs

| Alternativa | Benefícios | Custos e riscos | Avaliação |
| --- | --- | --- | --- |
| A — Monólito em camadas | É simples de executar e mantém a organização atual. | Funcionalidades diferentes continuam espalhadas pelas mesmas camadas técnicas. | Poderia continuar sendo usado, mas organiza menos claramente as funções do produto. |
| B — Monólito modular | Organiza o código por funcionalidade e mantém uma aplicação e um deploy. | Os módulos ainda compartilham o processo e o banco; não escalam separadamente. | É a opção que melhor atende às necessidades atuais. |
| C — Serviços independentes | Cada serviço poderia ser publicado e escalado separadamente. | Aumenta o trabalho com comunicação, falhas, monitoramento e operação. | Ainda não há evidência de que esse custo seja necessário para o Core. |

## 6. Decisão do Tech Lead

**Decisão: organizar o Core como um monólito modular.**

O código fica dividido por funcionalidades, como Aluno, Cursos, Ranking, Eventos e IA. A aplicação principal continua sendo uma só. O Ranking ganha uma fronteira clara para que possamos acompanhar seu comportamento e pensar em separá-lo no futuro, caso as medições justifiquem essa mudança.

## 7. ADR-001 — Evolução do Core

| Campo | Registro |
| --- | --- |
| Contexto | O produto pode chegar a 100.000 usuários e o Ranking pode crescer mais que outras funções, mas ainda não há dados que indiquem a necessidade de separar serviços. |
| ASRs | Manutenibilidade, possibilidade de crescimento do Ranking e complexidade operacional. |
| RPCs | Java/Spring Boot existente, equipe de três pessoas, banco relacional e necessidade de preservar a aplicação da AC1. |
| Alternativas | Continuar em camadas; organizar em módulos; separar funções em serviços independentes. |
| Decisão | Organizar Aluno, Cursos, Ranking, Eventos e IA em módulos dentro da aplicação principal. |
| Consequências positivas | O código fica mais fácil de navegar e mudanças podem ser feitas dentro de uma funcionalidade com menos interferência nas outras. |
| Trade-offs | A aplicação continua com um único processo, publicação e banco. Os módulos não escalam separadamente. |
| Quando rever | Se medições mostrarem um gargalo contínuo no Ranking ou uma necessidade real de publicá-lo separadamente. |

## 8. C4 — visão de Container

No Desafio 1, o Core continua sendo uma única aplicação Java/Spring Boot. A mudança é na organização interna do código. O desenho abaixo resume a visão; o [registro arquitetural](docs/arquitetura/README.md) contém o C4 final com os serviços auxiliares dos outros desafios.

```text
Aluno
  │
  ▼
Interface Web (Vue.js)
  │ REST
  ▼
Core Super Cursos (Java / Spring Boot)
  ├── Aluno
  ├── Cursos
  └── Ranking
  │ JPA
  ▼
Banco relacional (PostgreSQL)
```

## 9. Mini-lab — evidência arquitetural

### 9.1 Estrutura antes

O código estava organizado principalmente por tipo técnico. Funcionalidades diferentes podiam aparecer lado a lado nas mesmas pastas:

```text
controller/
service/
repository/
model/
```

### 9.2 Estrutura depois

Agora, cada funcionalidade tem sua própria pasta. Dentro dela, as responsabilidades de API, regras e persistência ficam próximas:

```text
modules/
├── aluno/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/persistence/
├── cursos/
├── ranking/
├── eventos/
└── ia/
```

### 9.3 O que comparar

| Evidência | Antes | Depois |
| --- | --- | --- |
| Organização | Pastas separadas por tipo técnico. | Pastas separadas por funcionalidade. |
| Dependências | Era mais difícil ver quais partes atendiam cada função. | As responsabilidades de cada função ficam mais fáceis de localizar. |
| Testes | Era necessário procurar os testes e o código em áreas diferentes. | Os testes ficam próximos da função que verificam. |
| Evolução | Uma mudança podia exigir navegar por várias pastas comuns. | Uma mudança pode começar no módulo responsável por aquela função. |

## 10. Evidências a versionar no Git

O código reorganizado, os testes e os documentos dos seis desafios estão no repositório. Os links abaixo levam aos detalhes e às evidências de cada parte:

As configurações do Prometheus e Grafana estão em `infra/monitoring/`; a configuração do Mosquitto está em `infra/iot/mqtt/`. Os arquivos Compose continuam na raiz e já apontam para esses caminhos.

- [Registro arquitetural B1](docs/arquitetura/README.md): requisitos, decisões, ADRs, diagramas C4 e resumo dos seis desafios.
- [Evidências do Desafio 2](evidencias/B1/desafio-2/README.md): organização interna e acesso à persistência.
- [Evidências do Desafio 3](evidencias/B1/desafio-3/README.md): integração com o serviço Python de IA.
- [Evidências do Desafio 4](evidencias/B1/desafio-4/README.md): eventos e tratamento de falhas de notificação.
- [Evidências do Desafio 5](evidencias/B1/desafio-5/README.md): integração IoT por MQTT.
- [Evidências do Desafio 6](evidencias/B1/desafio-6/README.md): esteira e observabilidade.
- [Histórico de alterações](CHANGELOG.md) e documentação anterior: [AC1](docs/releases/README-v1.0.0.md) e [AC2](docs/releases/README-v1.1.0.md).

Os testes descritos nas evidências verificam o comportamento implementado. Não foi feito teste de carga para afirmar que o sistema atende a 100.000 usuários.

## 11. Exemplo de conclusão do aluno

Decidimos organizar o Core como um monólito modular. A comparação da estrutura antes e depois mostra que as funcionalidades agora têm lugares mais claros no código, sem exigir que a aplicação seja dividida em serviços. Assim, mantemos uma operação simples para a equipe atual. Se o Ranking apresentar um gargalo ou precisar de mudanças independentes, poderemos rever essa decisão com base em medições.

## 12. Rastreabilidade final

| Elemento | Resposta resumida |
| --- | --- |
| RF | Ranking, gamificação e evolução das funções existentes. |
| RNFs analisados | Manutenibilidade, escalabilidade, complexidade operacional, desempenho e testabilidade. |
| ASRs | Manutenibilidade, crescimento futuro do Ranking e complexidade operacional. |
| RPCs | Java/Spring Boot, equipe de três pessoas, banco relacional e legado da AC1. |
| Alternativas | Monólito em camadas, monólito modular e serviços independentes. |
| ADR | ADR-001: organizar o Core em módulos. |
| C4 | Core permanece uma aplicação; sua organização interna passa a ser por funcionalidade. |
| Evidência | Comparação da estrutura, código modularizado, testes e documentos ligados na seção 10. |

**Ordem da decisão:** RF/RNF → ASRs e RPCs → alternativas → ADR → C4 → evidência.
