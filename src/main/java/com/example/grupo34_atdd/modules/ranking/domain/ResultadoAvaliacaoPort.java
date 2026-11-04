package com.example.grupo34_atdd.modules.ranking.domain;

import java.util.List;
import java.util.Optional;

public interface ResultadoAvaliacaoPort {
    Optional<ResultadoCurso> buscar(Long alunoId, String cursoSlug);

    ResultadoCurso salvar(ResultadoCurso resultado);

    long totalizar(Long alunoId);

    List<EntradaRanking> listarRanking();
}
