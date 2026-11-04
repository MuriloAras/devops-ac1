package com.example.grupo34_atdd.modules.cursos.api.dto;

import java.util.List;

public record CursoConteudoDTO(String slug, String titulo, String descricao, String videoUrl,
                               List<QuestaoCursoDTO> questoes) {
}
