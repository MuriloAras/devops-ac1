# Frontend Super Cursos

Vue 3, Vite e Nginx. Login, saldo de moedas, catálogo e aquisição de cursos.

## Desenvolvimento

```sh
npm ci
npm run dev
```

## Build

```sh
npm run build
```

## Homologação

```sh
docker compose -f docker-compose.homol.yml up -d --build frontend
```

Endereço: `http://localhost:3002`. Proxy: `/api` → `api:8080`.
Testes: [Cypress](cypress/README.md).
