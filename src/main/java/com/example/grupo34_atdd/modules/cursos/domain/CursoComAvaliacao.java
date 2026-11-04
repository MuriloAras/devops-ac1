package com.example.grupo34_atdd.modules.cursos.domain;

import java.util.List;

public record CursoComAvaliacao(String slug, String titulo, String descricao, String videoUrl,
                                List<QuestaoQuiz> questoes) {

    public CursoComAvaliacao {
        if (slug == null || slug.isBlank() || titulo == null || titulo.isBlank()
                || videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("Curso, título e vídeo são obrigatórios.");
        }
        if (questoes == null || questoes.size() != 10) {
            throw new IllegalArgumentException("Cada curso precisa ter exatamente 10 questões.");
        }
        questoes = List.copyOf(questoes);
    }

    public int contarAcertos(List<Integer> respostas) {
        if (respostas == null || respostas.size() != questoes.size()) {
            throw new IllegalArgumentException("A avaliação deve conter exatamente 10 respostas.");
        }
        for (int i = 0; i < questoes.size(); i++) {
            Integer resposta = respostas.get(i);
            if (resposta == null || resposta < 0 || resposta >= questoes.get(i).alternativas().size()) {
                throw new IllegalArgumentException("Uma ou mais respostas são inválidas.");
            }
        }
        int acertos = 0;
        for (int i = 0; i < questoes.size(); i++) {
            if (questoes.get(i).foiRespondidaCorretamente(respostas.get(i))) {
                acertos++;
            }
        }
        return acertos;
    }

    public int calcularPontos(List<Integer> respostas) {
        return contarAcertos(respostas) * 100;
    }
}
