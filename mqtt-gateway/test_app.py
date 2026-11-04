import json
import unittest
from app import transform


class TransformTests(unittest.TestCase):
    def test_maps_device_payload_to_core_contract(self):
        event = {"eventId": "event-1", "studentId": 7, "eventType": "presenca",
                 "occurredAt": "2026-11-04T00:00:00Z", "deviceId": "lab-1"}
        output = transform("supercursos/labs/lab-1/eventos", json.dumps(event).encode())
        self.assertEqual("event-1", output["eventoId"])
        self.assertEqual(7, output["alunoId"])
        self.assertEqual("presenca", output["tipo"])

    def test_rejects_topic_outside_lab_namespace(self):
        event = {"eventId": "event-1", "studentId": 7, "eventType": "presenca",
                 "occurredAt": "2026-11-04T00:00:00Z", "deviceId": "lab-1"}
        with self.assertRaises(ValueError):
            transform("other/topic", json.dumps(event).encode())


if __name__ == "__main__":
    unittest.main()
