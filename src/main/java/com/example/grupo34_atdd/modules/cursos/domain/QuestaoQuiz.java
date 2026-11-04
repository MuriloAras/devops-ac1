package com.example.grupo34_atdd.modules.cursos.domain;

import java.util.List;

public record QuestaoQuiz(String id, String enunciado, List<String> alternativas, int alternativaCorreta) {

    public QuestaoQuiz {
        if (id == null || id.isBlank() || enunciado == null || enunciado.isBlank()) {
            throw new IllegalArgumentException("A questão precisa de identificador e enunciado.");
        }
        if (alternativas == null || alternativas.size() < 2 || alternativaCorreta < 0
                || alternativaCorreta >= alternativas.size()) {
            throw new IllegalArgumentException("A questão precisa de alternativas e resposta correta válida.");
        }
        alternativas = List.copyOf(alternativas);
    }

    public boolean foiRespondidaCorretamente(Integer resposta) {
        return resposta != null && resposta == alternativaCorreta;
    }
}
