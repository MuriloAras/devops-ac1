package com.example.grupo34_atdd.modules.ranking.api.dto;

public record ResultadoAvaliacaoDTO(String cursoSlug, int pontosNestaTentativa, int melhorPontuacaoNoCurso,
                                    int acertosNestaTentativa, int pontuacaoTotalNoRanking) {
}
