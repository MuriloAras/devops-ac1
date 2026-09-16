package com.example.grupo34_atdd.dto;

import jakarta.validation.constraints.NotBlank;

public class TrocaMoedasRequestDTO {

    @NotBlank(message = "O nome do curso é obrigatório")
    private String nomeCurso;

    public TrocaMoedasRequestDTO() {
    }

    public TrocaMoedasRequestDTO(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }

    public String getNomeCurso() {
        return nomeCurso;
    }

    public void setNomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }
}
