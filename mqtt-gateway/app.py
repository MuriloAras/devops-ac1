import json
import os
import time
import requests
import paho.mqtt.client as mqtt
from prometheus_client import Counter, start_http_server

received = Counter("mqtt_gateway_received_total", "MQTT events received")
forwarded = Counter("mqtt_gateway_forwarded_total", "MQTT events accepted by Core")
failed = Counter("mqtt_gateway_forward_failures_total", "MQTT forwarding failures")
invalid = Counter("mqtt_gateway_invalid_messages_total", "Invalid MQTT messages discarded")


def transform(topic: str, raw: bytes) -> dict:
    data = json.loads(raw.decode("utf-8"))
    required = ("eventId", "studentId", "eventType", "occurredAt", "deviceId")
    if any(key not in data for key in required):
        raise ValueError("Payload missing required fields")
    if not topic.startswith("supercursos/labs/") or not topic.endswith("/eventos"):
        raise ValueError("Unsupported MQTT topic")
    return {
        "eventoId": str(data["eventId"]),
        "alunoId": int(data["studentId"]),
        "tipo": str(data["eventType"]),
        "ocorridoEm": str(data["occurredAt"]),
        "dispositivoId": str(data["deviceId"]),
    }


def on_message(client, _userdata, message):
    received.inc()
    try:
        event = transform(message.topic, message.payload)
    except (ValueError, json.JSONDecodeError) as error:
        invalid.inc()
        print(f"Invalid MQTT event discarded: {error}", flush=True)
        if message.qos > 0:
            client.ack(message.mid, message.qos)
        return
    try:
        response = requests.post(os.environ.get("CORE_EVENTS_URL", "http://api:8080/api/eventos-aprendizagem"),
                                 json=event, timeout=3)
        response.raise_for_status()
        forwarded.inc()
        if message.qos > 0:
            client.ack(message.mid, message.qos)
    except requests.RequestException as error:
        failed.inc()
        print(f"Event forwarding failed; MQTT broker may redeliver: {error}", flush=True)


def main():
    start_http_server(9101)
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="supercursos-mqtt-gateway")
    client.manual_ack_set(True)
    client.on_message = on_message
    host = os.environ.get("MQTT_HOST", "mosquitto")
    port = int(os.environ.get("MQTT_PORT", "1883"))
    while True:
        try:
            client.connect(host, port, 30)
            client.subscribe("supercursos/labs/+/eventos", qos=1)
            print(f"Subscribed to Super Cursos lab topic at {host}:{port}", flush=True)
            client.loop_forever()
        except Exception as error:
            failed.inc()
            print(f"MQTT unavailable, retrying: {error}", flush=True)
            time.sleep(3)


if __name__ == "__main__":
    main()
