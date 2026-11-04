import json
import os
import uuid
from datetime import datetime, timezone
import paho.mqtt.client as mqtt


def main():
    event = {
        "eventId": os.environ.get("EVENT_ID", str(uuid.uuid4())),
        "studentId": int(os.environ.get("STUDENT_ID", "1")),
        "eventType": os.environ.get("EVENT_TYPE", "presenca"),
        "occurredAt": os.environ.get("OCCURRED_AT", datetime.now(timezone.utc).isoformat()),
        "deviceId": os.environ.get("DEVICE_ID", "simulador-lab-01"),
    }
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.connect(os.environ.get("MQTT_HOST", "localhost"), int(os.environ.get("MQTT_PORT", "1883")), 30)
    client.loop_start()
    info = client.publish("supercursos/labs/lab-01/eventos", json.dumps(event), qos=1)
    info.wait_for_publish(timeout=5)
    if not info.is_published():
        raise TimeoutError("MQTT broker did not acknowledge the event")
    client.disconnect()
    client.loop_stop()
    print(json.dumps(event, indent=2))


if __name__ == "__main__":
    main()
