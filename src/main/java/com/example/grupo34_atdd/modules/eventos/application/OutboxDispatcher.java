package com.example.grupo34_atdd.modules.eventos.application;

import com.example.grupo34_atdd.modules.eventos.domain.ConsumidorNotificacao;
import com.example.grupo34_atdd.modules.eventos.infrastructure.persistence.EventoOutboxEntity;
import com.example.grupo34_atdd.modules.eventos.infrastructure.persistence.EventoOutboxRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    private final EventoOutboxRepository repository;
    private final ConsumidorNotificacao consumidor;
    private final MeterRegistry metrics;

    public OutboxDispatcher(EventoOutboxRepository repository, ConsumidorNotificacao consumidor,
                            MeterRegistry metrics) {
        this.repository = repository;
        this.consumidor = consumidor;
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${supercursos.outbox.intervalo-ms:2000}")
    @Transactional
    public void despacharPendentes() {
        for (EventoOutboxEntity evento : repository.findTop20ByProcessadoEmIsNullOrderByCriadoEmAsc()) {
            try {
                consumidor.consumir(evento.getTipo(), evento.getPayload());
                evento.setProcessadoEm(Instant.now());
                metrics.counter("supercursos_outbox_processados_total").increment();
            } catch (RuntimeException exception) {
                evento.registrarTentativa();
                metrics.counter("supercursos_outbox_falhas_total").increment();
                log.warn("Evento outbox {} permanece pendente; tentativa {}", evento.getId(), evento.getTentativas());
            }
        }
    }
}
