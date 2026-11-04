package com.example.grupo34_atdd.modules.ia.application;

import com.example.grupo34_atdd.modules.ia.api.dto.PerguntaAssistenteDTO;
import com.example.grupo34_atdd.modules.ia.api.dto.RecomendacoesDTO;
import com.example.grupo34_atdd.modules.ia.api.dto.RespostaAssistenteDTO;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class AiServiceClient {
    private final RestClient client;
    private final MeterRegistry metrics;

    public AiServiceClient(RestClient.Builder builder,
                           @Value("${supercursos.ai.base-url:http://localhost:8000}") String baseUrl,
                           MeterRegistry metrics) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(1_000);
        this.client = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.metrics = metrics;
    }

    public RecomendacoesDTO recomendar() {
        metrics.counter("supercursos_ai_solicitacoes_total", "operacao", "recomendacoes").increment();
        try {
            Map<?, ?> response = client.get().uri("/recommendations").retrieve().body(Map.class);
            Object courses = response == null ? null : response.get("courses");
            @SuppressWarnings("unchecked") List<String> slugs = courses instanceof List<?> list
                    ? (List<String>) list : List.of();
            return new RecomendacoesDTO(slugs, true);
        } catch (RestClientException exception) {
            metrics.counter("supercursos_ai_fallback_total", "operacao", "recomendacoes").increment();
            return new RecomendacoesDTO(List.of(), false);
        }
    }

    public RespostaAssistenteDTO perguntar(String pergunta) {
        metrics.counter("supercursos_ai_solicitacoes_total", "operacao", "assistente").increment();
        try {
            Map<?, ?> response = client.post().uri("/assist").body(Map.of("question", pergunta))
                    .retrieve().body(Map.class);
            String answer = response == null ? "O assistente está temporariamente indisponível." :
                    String.valueOf(response.get("answer"));
            String source = response == null ? "" : String.valueOf(response.get("source"));
            return new RespostaAssistenteDTO(answer, source, true);
        } catch (RestClientException exception) {
            metrics.counter("supercursos_ai_fallback_total", "operacao", "assistente").increment();
            return new RespostaAssistenteDTO(
                    "O assistente está temporariamente indisponível. Tente novamente mais tarde.", "", false);
        }
    }
}
