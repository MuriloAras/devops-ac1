package com.example.grupo34_atdd.modules.eventos.application;

import com.example.grupo34_atdd.modules.aluno.infrastructure.persistence.AlunoRepository;
import com.example.grupo34_atdd.modules.eventos.api.dto.AprendizagemEventoDTO;
import com.example.grupo34_atdd.modules.eventos.api.dto.IngressoEventoDTO;
import com.example.grupo34_atdd.modules.eventos.infrastructure.persistence.EventoAprendizagemEntity;
import com.example.grupo34_atdd.modules.eventos.infrastructure.persistence.EventoAprendizagemRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Set;

@Service
public class EventoAprendizagemService {
    private static final Set<String> TIPOS_ACEITOS = Set.of("presenca", "atividade_iniciada", "curso_concluido");
    private final EventoAprendizagemRepository repository;
    private final AlunoRepository alunoRepository;
    private final MeterRegistry metrics;

    public EventoAprendizagemService(EventoAprendizagemRepository repository, AlunoRepository alunoRepository,
                                     MeterRegistry metrics) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.metrics = metrics;
    }

    @Transactional
    public IngressoEventoDTO registrar(AprendizagemEventoDTO evento) {
        if (!TIPOS_ACEITOS.contains(evento.tipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de evento não suportado.");
        }
        if (!alunoRepository.existsById(evento.alunoId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
        }
        if (repository.existsById(evento.eventoId())) {
            metrics.counter("supercursos_iot_eventos_duplicados_total").increment();
            return new IngressoEventoDTO(evento.eventoId(), true);
        }
        repository.save(new EventoAprendizagemEntity(evento.eventoId(), evento.alunoId(), evento.tipo(),
                evento.ocorridoEm(), evento.dispositivoId(), Instant.now()));
        metrics.counter("supercursos_iot_eventos_recebidos_total", "tipo", evento.tipo()).increment();
        return new IngressoEventoDTO(evento.eventoId(), false);
    }
}
