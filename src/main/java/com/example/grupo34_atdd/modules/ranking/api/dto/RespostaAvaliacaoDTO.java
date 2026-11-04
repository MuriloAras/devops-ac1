package com.example.grupo34_atdd.modules.ranking.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RespostaAvaliacaoDTO {

    @NotNull(message = "As respostas são obrigatórias")
    @Size(min = 10, max = 10, message = "A avaliação deve conter exatamente 10 respostas")
    private List<@NotNull @Min(0) @Max(3) Integer> respostas;

    public List<Integer> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<Integer> respostas) {
        this.respostas = respostas;
    }
}
