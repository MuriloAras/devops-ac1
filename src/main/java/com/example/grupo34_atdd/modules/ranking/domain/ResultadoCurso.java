package com.example.grupo34_atdd.modules.ranking.domain;

import java.time.Instant;

public record ResultadoCurso(Long alunoId, String nomeAluno, String cursoSlug,
                             int melhorPontuacao, int melhorNumeroAcertos,
                             Instant ultimaTentativaEm) {
}
