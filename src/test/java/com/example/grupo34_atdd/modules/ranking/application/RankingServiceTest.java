package com.example.grupo34_atdd.modules.ranking.application;

import com.example.grupo34_atdd.modules.ranking.domain.EntradaRanking;
import com.example.grupo34_atdd.modules.ranking.domain.ResultadoAvaliacaoPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingServiceTest {

    @Test
    void conservaEmpatesEOrdemRecebidaDoRepositorio() {
        ResultadoAvaliacaoPort repository = mock(ResultadoAvaliacaoPort.class);
        when(repository.listarRanking()).thenReturn(List.of(
                new EntradaRanking(1L, "Ana", 1_000L),
                new EntradaRanking(2L, "Bia", 1_000L),
                new EntradaRanking(3L, "Caio", 700L)
        ));

        var ranking = new RankingService(repository).consultar();

        assertEquals(1, ranking.get(0).posicao());
        assertEquals(1, ranking.get(1).posicao());
        assertEquals(3, ranking.get(2).posicao());
        assertEquals(700, ranking.get(2).pontos());
    }
}
