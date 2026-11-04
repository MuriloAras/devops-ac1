package com.example.grupo34_atdd.modules.ranking.application;

import com.example.grupo34_atdd.modules.ranking.infrastructure.persistence.ProjecaoRanking;
import com.example.grupo34_atdd.modules.ranking.infrastructure.persistence.ResultadoAvaliacaoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingServiceTest {

    @Test
    void conservaEmpatesEOrdemRecebidaDoRepositorio() {
        ResultadoAvaliacaoRepository repository = mock(ResultadoAvaliacaoRepository.class);
        when(repository.buscarRanking()).thenReturn(List.of(
                linha(1L, "Ana", 1_000L),
                linha(2L, "Bia", 1_000L),
                linha(3L, "Caio", 700L)
        ));

        var ranking = new RankingService(repository).consultar();

        assertEquals(1, ranking.get(0).posicao());
        assertEquals(1, ranking.get(1).posicao());
        assertEquals(3, ranking.get(2).posicao());
        assertEquals(700, ranking.get(2).pontos());
    }

    private ProjecaoRanking linha(Long id, String nome, Long pontos) {
        return new ProjecaoRanking() {
            @Override public Long getAlunoId() { return id; }
            @Override public String getNome() { return nome; }
            @Override public Long getPontos() { return pontos; }
        };
    }
}
