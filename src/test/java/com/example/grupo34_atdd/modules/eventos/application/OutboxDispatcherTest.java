package com.example.grupo34_atdd.modules.eventos.application;

import com.example.grupo34_atdd.modules.eventos.domain.ConsumidorNotificacao;
import com.example.grupo34_atdd.modules.eventos.infrastructure.persistence.EventoOutboxEntity;
import com.example.grupo34_atdd.modules.eventos.infrastructure.persistence.EventoOutboxRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class OutboxDispatcherTest {
    @Test
    void notificacaoIndisponivelDeixaEventoPendenteParaNovaTentativa() {
        EventoOutboxRepository repository = mock(EventoOutboxRepository.class);
        ConsumidorNotificacao consumer = mock(ConsumidorNotificacao.class);
        EventoOutboxEntity event = new EventoOutboxEntity("avaliacao.concluida", "alunoId=7");
        when(repository.findTop20ByProcessadoEmIsNullOrderByCriadoEmAsc()).thenReturn(List.of(event));
        doThrow(new IllegalStateException("simulated outage")).when(consumer)
                .consumir("avaliacao.concluida", "alunoId=7");
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();

        new OutboxDispatcher(repository, consumer, metrics).despacharPendentes();

        assertNull(event.getProcessadoEm());
        assertEquals(1, event.getTentativas());
        assertEquals(1.0, metrics.counter("supercursos_outbox_falhas_total").count());
    }
}
