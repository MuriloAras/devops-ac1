# Testes E2E e evidências

A suíte usa o frontend e a API reais e cadastra um aluno com e-mail exclusivo
para cada teste. Pode ser repetida sem apagar o banco. Não use um banco de
produção: os alunos de teste permanecem no volume de homologação.

## Executar

Com a aplicação de homologação ativa, execute na raiz do projeto:

```sh
docker compose --profile e2e run --rm --no-deps cypress
```

O serviço usa `cypress/included:14.5.4` e `CYPRESS_BASE_URL=http://frontend:80`.
O Cypress já está instalado nessa imagem; não é necessário `npm install` no
container. A configuração ESM também não depende de `node_modules` locais.

Para executar pelo Node local, dentro de `frontend`, use `npm ci` e
`npm run test:e2e`. A URL local padrão é `http://localhost:3002`; para outra
porta, defina `CYPRESS_BASE_URL` antes de executar. `npm run test:e2e:open` abre
o modo interativo.

## O que é verificado

1. Senha incorreta retorna HTTP 401 e mantém o aluno fora do dashboard.
2. Login correto mostra nome, saldo e cursos do aluno preparado pela API.
3. Uma troca com 3 moedas adquire um curso e deixa saldo 0.
4. Uma troca com 5 moedas adquire um curso e deixa saldo 2.
5. Saldo 2 desabilita a troca na interface; uma chamada direta à API retorna
   HTTP 400 sem alterar o saldo nem os cursos.
6. O curso e o saldo são preservados após sair e entrar novamente.

As trocas bem-sucedidas são conferidas na tela, na resposta da requisição e em
uma consulta posterior à API.

## Artefatos

- `cypress/results/junit-*.xml`: resultado JUnit para publicação no Jenkins.
- `cypress/screenshots/`: seis capturas dos cenários aprovados, além das
  capturas automáticas caso ocorra falha.
- `cypress/videos/`: gravação da execução completa da especificação.

O pipeline deve limpar esses diretórios antes da execução e arquivá-los ao
final, inclusive quando houver falha. Uma captura isolada não comprova que a
suíte inteira passou: consulte o JUnit e o código de saída do Cypress.

