#!/bin/sh
set -eu

PROJECT_NAME="${COMPOSE_PROJECT_NAME:-supercursos-homol}"
API_URL="${API_URL:-http://localhost:8082}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9092}"
compose() {
  docker compose -p "$PROJECT_NAME" -f docker-compose.homol.yml "$@"
}

restore_ai() {
  compose start ai-service >/dev/null 2>&1 || true
}
trap restore_ai EXIT INT TERM

compose stop ai-service
printf '%s\n' 'Resposta do Core com o serviço de IA parado:'
curl --fail --silent --show-error -X POST "$API_URL/api/ia/assistente" \
  -H 'Content-Type: application/json' \
  -d '{"pergunta":"Para que serve print no Python?"}'
printf '\n%s\n' 'Contador Prometheus de fallback (aguarda um scrape):'
sleep 16
curl --fail --silent --show-error --get "$PROMETHEUS_URL/api/v1/query" \
  --data-urlencode 'query=supercursos_ai_fallback_total{operacao="assistente"}'
printf '\n'
