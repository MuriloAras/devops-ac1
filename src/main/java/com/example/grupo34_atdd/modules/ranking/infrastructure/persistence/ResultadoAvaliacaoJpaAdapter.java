package com.example.grupo34_atdd.modules.ranking.infrastructure.persistence;

import com.example.grupo34_atdd.modules.ranking.domain.EntradaRanking;
import com.example.grupo34_atdd.modules.ranking.domain.ResultadoAvaliacaoPort;
import com.example.grupo34_atdd.modules.ranking.domain.ResultadoCurso;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ResultadoAvaliacaoJpaAdapter implements ResultadoAvaliacaoPort {
    private final ResultadoAvaliacaoRepository repository;

    public ResultadoAvaliacaoJpaAdapter(ResultadoAvaliacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ResultadoCurso> buscar(Long alunoId, String cursoSlug) {
        return repository.findByAlunoIdAndCursoSlug(alunoId, cursoSlug).map(this::toDomain);
    }

    @Override
    public ResultadoCurso salvar(ResultadoCurso resultado) {
        ResultadoAvaliacaoEntity entity = repository.findByAlunoIdAndCursoSlug(
                resultado.alunoId(), resultado.cursoSlug()).orElseGet(ResultadoAvaliacaoEntity::new);
        entity.setAlunoId(resultado.alunoId());
        entity.setNomeAluno(resultado.nomeAluno());
        entity.setCursoSlug(resultado.cursoSlug());
        entity.setMelhorPontuacao(resultado.melhorPontuacao());
        entity.setMelhorNumeroAcertos(resultado.melhorNumeroAcertos());
        entity.setUltimaTentativaEm(resultado.ultimaTentativaEm());
        return toDomain(repository.save(entity));
    }

    @Override
    public long totalizar(Long alunoId) {
        Long total = repository.buscarTotalPontosDoAluno(alunoId);
        return total == null ? 0 : total;
    }

    @Override
    public List<EntradaRanking> listarRanking() {
        return repository.buscarRanking().stream()
                .map(row -> new EntradaRanking(row.getAlunoId(), row.getNome(), row.getPontos() == null ? 0 : row.getPontos()))
                .toList();
    }

    private ResultadoCurso toDomain(ResultadoAvaliacaoEntity entity) {
        return new ResultadoCurso(entity.getAlunoId(), entity.getNomeAluno(), entity.getCursoSlug(),
                entity.getMelhorPontuacao(), entity.getMelhorNumeroAcertos(), entity.getUltimaTentativaEm());
    }
}
