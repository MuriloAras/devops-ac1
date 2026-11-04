package com.example.grupo34_atdd.modules.cursos.application;

import com.example.grupo34_atdd.modules.aluno.api.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.modules.aluno.application.AlunoService;
import com.example.grupo34_atdd.modules.eventos.domain.EventoOutboxPort;
import com.example.grupo34_atdd.modules.ranking.domain.ResultadoAvaliacaoPort;
import com.example.grupo34_atdd.modules.ranking.domain.ResultadoCurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private AlunoService alunoService;

    @Mock
    private ResultadoAvaliacaoPort resultadoPort;

    @Mock
    private EventoOutboxPort eventoPort;

    private CursoService cursoService;

    @BeforeEach
    void configurar() {
        cursoService = new CursoService(new CatalogoCursos(), alunoService, resultadoPort, eventoPort);
    }

    @Test
    void cursoEntregaUmVideoEDezQuestoesSemChaveDeCorrecao() {
        when(alunoService.buscarPorId(7L)).thenReturn(aluno());
        var dto = cursoService.acessarCurso(7L, CatalogoCursos.SLUG_PYTHON);
        assertEquals("Introdução ao Python", dto.titulo());
        assertEquals("https://www.youtube-nocookie.com/embed/g_R_Asf6Co0", dto.videoUrl());
        assertEquals(10, dto.questoes().size());
    }

    @Test
    void pontuacaoUsaMelhorNotaPorCursoEAluno() {
        when(alunoService.buscarPorId(7L)).thenReturn(aluno());
        when(resultadoPort.buscar(7L, CatalogoCursos.SLUG_PYTHON))
                .thenReturn(Optional.empty());
        when(resultadoPort.totalizar(7L)).thenReturn(1_000L);
        when(resultadoPort.salvar(any(ResultadoCurso.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = cursoService.avaliar(7L, CatalogoCursos.SLUG_PYTHON,
                List.of(0, 1, 2, 0, 2, 0, 2, 1, 1, 3));

        assertEquals(1_000, resultado.pontosNestaTentativa());
        assertEquals(10, resultado.acertosNestaTentativa());
        assertEquals(1_000, resultado.melhorPontuacaoNoCurso());
    }

    @Test
    void tentativaMenorNaoReduzMelhorPontuacaoDoCurso() {
        when(alunoService.buscarPorId(7L)).thenReturn(aluno());
        ResultadoCurso recorde = new ResultadoCurso(7L, "Ana", CatalogoCursos.SLUG_PYTHON,
                1_000, 10, java.time.Instant.now());
        when(resultadoPort.buscar(7L, CatalogoCursos.SLUG_PYTHON)).thenReturn(Optional.of(recorde));
        when(resultadoPort.totalizar(7L)).thenReturn(1_000L);
        when(resultadoPort.salvar(any(ResultadoCurso.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = cursoService.avaliar(7L, CatalogoCursos.SLUG_PYTHON,
                List.of(1, 0, 0, 1, 0, 1, 0, 0, 0, 0));

        assertEquals(0, resultado.pontosNestaTentativa());
        assertEquals(1_000, resultado.melhorPontuacaoNoCurso());
        assertEquals(1_000, resultado.pontuacaoTotalNoRanking());
    }

    @Test
    void naoLiberaConteudoParaCursoNaoAdquirido() {
        when(alunoService.buscarPorId(7L)).thenReturn(new AlunoResponseDTO(7L, "Ana", "ana@test", 0, List.of()));
        assertThrows(ResponseStatusException.class,
                () -> cursoService.acessarCurso(7L, CatalogoCursos.SLUG_PYTHON));
    }

    private AlunoResponseDTO aluno() {
        return new AlunoResponseDTO(7L, "Ana", "ana@test", 0, List.of(CatalogoCursos.TITULO_PYTHON));
    }
}
