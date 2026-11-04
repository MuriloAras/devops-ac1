package com.example.grupo34_atdd.modules.cursos.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CursoComAvaliacaoTest {

    private final CursoComAvaliacao curso = new CursoComAvaliacao("teste", "Curso teste", "Descrição",
            "https://www.youtube.com/embed/video", List.of(
            new QuestaoQuiz("1", "Questão 1", List.of("A", "B"), 0),
            new QuestaoQuiz("2", "Questão 2", List.of("A", "B"), 1),
            new QuestaoQuiz("3", "Questão 3", List.of("A", "B"), 0),
            new QuestaoQuiz("4", "Questão 4", List.of("A", "B"), 1),
            new QuestaoQuiz("5", "Questão 5", List.of("A", "B"), 0),
            new QuestaoQuiz("6", "Questão 6", List.of("A", "B"), 1),
            new QuestaoQuiz("7", "Questão 7", List.of("A", "B"), 0),
            new QuestaoQuiz("8", "Questão 8", List.of("A", "B"), 1),
            new QuestaoQuiz("9", "Questão 9", List.of("A", "B"), 0),
            new QuestaoQuiz("10", "Questão 10", List.of("A", "B"), 1)
    ));

    @Test
    void cadaAcertoValeCemPontosEAvaliacaoPerfeitaValeMil() {
        assertEquals(1_000, curso.calcularPontos(List.of(0, 1, 0, 1, 0, 1, 0, 1, 0, 1)));
        assertEquals(500, curso.calcularPontos(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    void exigeDezRespostasEAlternativasValidas() {
        assertThrows(IllegalArgumentException.class, () -> curso.calcularPontos(List.of(0, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> curso.calcularPontos(List.of(0, 1, 0, 1, 0, 1, 0, 1, 0, 2)));
    }
}
