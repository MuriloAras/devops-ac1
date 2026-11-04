package com.example.grupo34_atdd.modules.ia.application;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiServiceClientTest {
    private final MeterRegistry metrics = new SimpleMeterRegistry();
    private final AiServiceClient client = new AiServiceClient(RestClient.builder(), "http://127.0.0.1:1", metrics);

    @Test
    void recomendacaoUsaRespostaAlternativaQuandoPythonEstaIndisponivel() {
        var result = client.recomendar();
        assertFalse(result.servicoDisponivel());
        assertTrue(result.cursos().isEmpty());
        assertTrue(metrics.counter("supercursos_ai_fallback_total", "operacao", "recomendacoes").count() > 0);
    }

    @Test
    void assistenteNaoBloqueiaFluxoQuandoPythonEstaIndisponivel() {
        var result = client.perguntar("O que é print?");
        assertFalse(result.servicoDisponivel());
        assertTrue(result.resposta().contains("temporariamente indisponível"));
        assertTrue(metrics.counter("supercursos_ai_fallback_total", "operacao", "assistente").count() > 0);
    }
}
