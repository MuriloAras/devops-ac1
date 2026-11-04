package com.example.grupo34_atdd.modules.cursos.application;

import com.example.grupo34_atdd.modules.aluno.api.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.modules.aluno.application.AlunoService;
import com.example.grupo34_atdd.modules.cursos.api.dto.CursoConteudoDTO;
import com.example.grupo34_atdd.modules.cursos.api.dto.CursoResumoDTO;
import com.example.grupo34_atdd.modules.cursos.api.dto.QuestaoCursoDTO;
import com.example.grupo34_atdd.modules.cursos.domain.CursoComAvaliacao;
import com.example.grupo34_atdd.modules.ranking.api.dto.ResultadoAvaliacaoDTO;
import com.example.grupo34_atdd.modules.ranking.infrastructure.persistence.ResultadoAvaliacaoEntity;
import com.example.grupo34_atdd.modules.ranking.infrastructure.persistence.ResultadoAvaliacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class CursoService {

    private final CatalogoCursos catalogoCursos;
    private final AlunoService alunoService;
    private final ResultadoAvaliacaoRepository resultadoRepository;

    public CursoService(CatalogoCursos catalogoCursos, AlunoService alunoService,
                        ResultadoAvaliacaoRepository resultadoRepository) {
        this.catalogoCursos = catalogoCursos;
        this.alunoService = alunoService;
        this.resultadoRepository = resultadoRepository;
    }

    @Transactional(readOnly = true)
    public List<CursoResumoDTO> listarCursos() {
        return catalogoCursos.listar().stream()
                .map(curso -> new CursoResumoDTO(curso.slug(), curso.titulo(), curso.descricao()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoConteudoDTO acessarCurso(Long alunoId, String slug) {
        CursoComAvaliacao curso = buscarCurso(slug);
        validarAcesso(alunoId, curso.titulo());

        List<QuestaoCursoDTO> questoes = curso.questoes().stream()
                .map(questao -> new QuestaoCursoDTO(questao.id(), questao.enunciado(), questao.alternativas()))
                .toList();
        return new CursoConteudoDTO(curso.slug(), curso.titulo(), curso.descricao(), curso.videoUrl(), questoes);
    }

    @Transactional
    public ResultadoAvaliacaoDTO avaliar(Long alunoId, String slug, List<Integer> respostas) {
        CursoComAvaliacao curso = buscarCurso(slug);
        AlunoResponseDTO aluno = validarAcesso(alunoId, curso.titulo());

        int acertos;
        try {
            acertos = curso.contarAcertos(respostas);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        int pontos = acertos * 100;

        ResultadoAvaliacaoEntity resultado = resultadoRepository
                .findByAlunoIdAndCursoSlug(alunoId, slug)
                .orElseGet(ResultadoAvaliacaoEntity::new);
        resultado.setAlunoId(alunoId);
        resultado.setNomeAluno(aluno.getNome());
        resultado.setCursoSlug(slug);
        if (pontos > resultado.getMelhorPontuacao()) {
            resultado.setMelhorPontuacao(pontos);
            resultado.setMelhorNumeroAcertos(acertos);
        }
        resultado.setUltimaTentativaEm(Instant.now());
        resultadoRepository.save(resultado);

        Long pontuacaoTotal = resultadoRepository.buscarTotalPontosDoAluno(alunoId);
        return new ResultadoAvaliacaoDTO(slug, pontos, resultado.getMelhorPontuacao(), acertos,
                pontuacaoTotal == null ? 0 : pontuacaoTotal.intValue());
    }

    private CursoComAvaliacao buscarCurso(String slug) {
        CursoComAvaliacao curso = catalogoCursos.buscar(slug);
        if (curso == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado.");
        }
        return curso;
    }

    private AlunoResponseDTO validarAcesso(Long alunoId, String nomeCurso) {
        AlunoResponseDTO aluno = alunoService.buscarPorId(alunoId);
        boolean adquirido = aluno.getCursosAdquiridos() != null
                && aluno.getCursosAdquiridos().stream().anyMatch(nomeCurso::equalsIgnoreCase);
        if (!adquirido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Adquira este curso antes de acessá-lo.");
        }
        return aluno;
    }
}
