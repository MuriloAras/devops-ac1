package com.example.grupo34_atdd.modules.eventos.infrastructure.persistence;

import com.example.grupo34_atdd.modules.eventos.domain.EventoOutboxPort;
import org.springframework.stereotype.Repository;

@Repository
public class EventoOutboxJpaAdapter implements EventoOutboxPort {
    private final EventoOutboxRepository repository;

    public EventoOutboxJpaAdapter(EventoOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void publicar(String tipo, String payload) {
        repository.save(new EventoOutboxEntity(tipo, payload));
    }
}
