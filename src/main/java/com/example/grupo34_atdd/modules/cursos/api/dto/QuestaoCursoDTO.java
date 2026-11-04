package com.example.grupo34_atdd.modules.cursos.api.dto;

import java.util.List;

public record QuestaoCursoDTO(String id, String enunciado, List<String> alternativas) {
}
