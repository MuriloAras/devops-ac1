# Desafio 5 — Ingresso de eventos IoT por MQTT

O simulador publica QoS 1 no tópico `supercursos/labs/lab-01/eventos`. O gateway Python valida o tópico e converte o formato do dispositivo para o contrato HTTP do Core. O Core valida o tipo e o aluno e persiste o evento em `eventos_aprendizagem`, cuja chave é o `eventoId`; reentregas são idempotentes.

Com a homologação ativa, crie um aluno e use o ID devolvido no primeiro comando:

```bash
curl -sS -X POST http://localhost:8082/api/alunos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Aluno IoT","email":"iot@example.test","senha":"senha123","saldoMoedas":0}'

STUDENT_ID=1 sh evidencias/B1/desafio-5/simular-evento.sh
```

O simulador imprime o ID do evento e o script confirma contadores do Core e do gateway. Rode de novo com o mesmo ID, por exemplo `EVENT_ID=evento-repetido STUDENT_ID=1 sh evidencias/B1/desafio-5/simular-evento.sh`, para verificar a deduplicação; a métrica `supercursos_iot_eventos_duplicados_total` aumenta. Para manter a mensagem MQTT até o Core confirmar, o gateway usa QoS 1 e confirmação manual após resposta HTTP bem-sucedida. O broker local é anônimo e deve permanecer restrito ao laboratório.
