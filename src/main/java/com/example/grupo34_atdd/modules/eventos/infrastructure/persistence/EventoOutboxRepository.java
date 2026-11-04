package com.example.grupo34_atdd.modules.eventos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoOutboxRepository extends JpaRepository<EventoOutboxEntity, Long> {
    List<EventoOutboxEntity> findTop20ByProcessadoEmIsNullOrderByCriadoEmAsc();
}
