package com.example.grupo34_atdd.modules.ranking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResultadoAvaliacaoRepository extends JpaRepository<ResultadoAvaliacaoEntity, Long> {

    Optional<ResultadoAvaliacaoEntity> findByAlunoIdAndCursoSlug(Long alunoId, String cursoSlug);

    @Query("""
            select r.alunoId as alunoId, r.nomeAluno as nome, sum(r.melhorPontuacao) as pontos
            from ResultadoAvaliacaoEntity r
            group by r.alunoId, r.nomeAluno
            order by sum(r.melhorPontuacao) desc, r.nomeAluno asc
            """)
    List<ProjecaoRanking> buscarRanking();

    @Query("select coalesce(sum(r.melhorPontuacao), 0) from ResultadoAvaliacaoEntity r where r.alunoId = :alunoId")
    Long buscarTotalPontosDoAluno(@Param("alunoId") Long alunoId);
}
