# Super Cursos

## Contexto

Durante a Ac1, realizamos a automação de testes de uma plataforma de cursos.
Agora, o desafio é escolher entre alterar a arquitetura do projeto para modular ou continuar em camadas.
Devemos considerar que a plataforma atingiu 100000 usuários. 
Optamos por seguir com a arquitetura modular e a seguir iremos desenvolver o motivo dessa escolha.

## Decisão Arquitetural

**Arquitetura escolhida: Modular**

O sistema é organizado em módulos, cada um responsável por uma funcionalidade ou conjunto coeso de regras de negócio. Cada módulo possui fronteiras bem definidas e depende o mínimo possível dos detalhes internos dos demais.

## Motivos da Escolha

### Organização de código

Conforme o sistema cresce e novas funcionalidades são implementadas, as regras de negócio permanecem organizadas, pois cada uma tem um lugar claro na estrutura do projeto.

### Equipe

Com três membros, a divisão de tarefas na arquitetura modular é mais clara e objetiva. As fronteiras entre as funcionalidades são bem definidas, o que reduz sobreposição de trabalho e conflitos.

### Impacto de mudanças

A modularização limita o impacto entre as funcionalidades. Por exemplo, ao implementar uma nova funcionalidade, o risco de afetar partes já existentes do sistema é menor.

### Manutenção

Com o código organizado por funcionalidade, reduz-se a necessidade de compreender detalhes internos de partes não relacionadas à tarefa atual, o que favorece a manutenção.

### Testes

A estrutura modular ajuda a delimitar o escopo dos testes de cada funcionalidade e facilita a identificação de regressões.

### Clareza

A organização por módulos facilita a resposta a perguntas como:

- Qual módulo é responsável por determinada regra?
- Qual funcionalidade precisa de refatoração?

## Estrutura modular implementada

A aplicação foi reorganizada por capacidade de negócio: o módulo `aluno` cuida
de cadastro, autenticação, moedas e aquisição; `cursos` oferece o catálogo, o
vídeo e as avaliações; e `ranking` calcula as posições pelas melhores notas.
A configuração transversal de segurança e o redirecionamento da página inicial
ficam em `shared`.

O registro do Desafio 1, incluindo RF/RNF, ASRs, RPCs, alternativas, ADR, C4 e
evidências, está em [docs/arquitetura/README.md](docs/arquitetura/README.md).

```text
src/main/java/com/example/grupo34_atdd/
├── Grupo34AtddApplication.java
├── modules/aluno/
│   ├── api/                         # Controller e contratos HTTP (DTOs)
│   ├── application/                # Casos de uso e transações
│   ├── domain/                     # Regras de negócio
│   └── infrastructure/persistence/ # Entidade JPA e repositório
├── modules/cursos/                 # Catálogo, vídeo e avaliações
├── modules/ranking/                # Pontuação e classificação
└── shared/
    ├── config/                     # Configuração transversal de segurança
    └── web/                        # Rota inicial e redirecionamento
```

O controller encaminha requisições ao serviço de aplicação; o serviço aplica as
regras de domínio e usa o repositório para persistir os dados. DTOs representam
os contratos HTTP e não dependem de entidades JPA. `shared` não contém regras
específicas de aluno. A aplicação continua sendo um monólito modular: os módulos
compartilham o processo e o banco atuais, com fronteiras organizadas no código.

## Compatibilidade preservada

A refatoração preserva as rotas, payloads, status HTTP, autenticação, regras de
troca e mapeamento do banco existentes. Permanecem os endpoints de cadastro,
listagem, consulta, login e troca de moedas sob `/api/alunos`. Foram adicionados
os endpoints de catálogo e avaliação em `/api/cursos` e
`/api/alunos/{id}/cursos/{slug}`, além de `GET /api/ranking`. As tabelas
`alunos` e `aluno_cursos`, o custo de 3 moedas por curso e as respostas de erro
existentes foram mantidos; `resultados_avaliacao` guarda o recorde por aluno e
curso.

Os testes foram movidos junto às responsabilidades que verificam. Os testes de
integração continuam validando PostgreSQL, API, saúde, métricas e OpenAPI. O
README-v1.1.0 mantém as instruções e evidências de CI/CD da AC2.



--------------------------------------------------------------------------------

# Super Cursos

O Super Cursos é uma plataforma de educação continuada gamificada, na qual alunos
fazem login, usam moedas para adquirir cursos, assistem a uma aula em vídeo e
respondem a uma avaliação. Cada acerto rende 100 pontos; o ranking soma a melhor
nota de cada aluno por curso. A aplicação reúne uma API em Spring Boot, uma
interface em Vue.js e um banco de dados PostgreSQL.

Para conhecer as diferenças entre as versões e acessar suas práticas e evidências,
clique nos READMEs abaixo:

- [README v1.0.0 (AC1)](README-v1.0.0.md): implementação inicial da aplicação, com
  autenticação, troca de moedas por cursos, cenários BDD, práticas de TDD, testes
  de unidade e ambiente Docker.
- [README v1.1.0 (AC2)](README-v1.1.0.md): evolução com esteira Jenkins, testes de
  integração e Cypress, deploy em homologação com Docker, relatórios de qualidade
  e monitoramento com Prometheus e Grafana.

**[Acessar a pasta de evidências](evidencias/)**

Outras práticas: [CI/CD com Jenkins](https://github.com/PedroADev/CI-e-CD-com-Jenkins).
