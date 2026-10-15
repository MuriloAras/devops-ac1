# Testes E2E

- Imagem: `cypress/included:14.5.4`.
- URL no container: `http://frontend:80`.
- URL local: `http://localhost:3002`.
- Dados: aluno com e-mail exclusivo por cenário, cadastrado pela API.

## Container

Na raiz do projeto, com homologação ativa:

```sh
docker compose -f docker-compose.homol.yml --profile e2e run --rm --no-deps cypress
```

## Execução local

No diretório `frontend`:

```sh
npm ci
npm run test:e2e
```

Modo interativo: `npm run test:e2e:open`. URL configurável por `CYPRESS_BASE_URL`.

## Cenários

1. Senha incorreta: HTTP 401 e acesso bloqueado.
2. Login válido: nome, saldo e cursos disponíveis.
3. Saldo 3: aquisição de curso e saldo final 0.
4. Saldo 5: aquisição de curso e saldo final 2.
5. Saldo 2: troca bloqueada e API HTTP 400, sem alteração de dados.
6. Novo login: curso adquirido e saldo persistidos.

## Artefatos

- `cypress/results/junit-*.xml`: resultados JUnit.
- `cypress/screenshots/`: capturas dos cenários.
- `cypress/videos/`: vídeo da especificação.
