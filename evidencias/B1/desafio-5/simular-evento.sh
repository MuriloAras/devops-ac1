#!/bin/sh
set -eu

PROJECT_NAME="${COMPOSE_PROJECT_NAME:-supercursos-homol}"
STUDENT_ID="${STUDENT_ID:-1}"
API_URL="${API_URL:-http://localhost:8082}"
GATEWAY_METRICS_URL="${GATEWAY_METRICS_URL:-http://localhost:9101/metrics}"
compose() {
  docker compose -p "$PROJECT_NAME" -f docker-compose.homol.yml "$@"
}

EVENT_ID="${EVENT_ID:-iot-demo-$(date +%s)-$$}"
printf 'Publicando evento %s para aluno %s\n' "$EVENT_ID" "$STUDENT_ID"
compose exec -T mqtt-gateway env STUDENT_ID="$STUDENT_ID" EVENT_ID="$EVENT_ID" python simulate.py
sleep 2
printf '\n%s\n' 'Métrica de eventos IoT persistidos no Core:'
curl --fail --silent --show-error "$API_URL/actuator/prometheus" | grep -E 'supercursos_iot_eventos_(recebidos|duplicados)_total'
printf '\n%s\n' 'Métrica do gateway MQTT:'
curl --fail --silent --show-error "$GATEWAY_METRICS_URL" | grep -E 'mqtt_gateway_(forwarded|forward_failures)_total'
