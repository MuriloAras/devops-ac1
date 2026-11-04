package com.example.grupo34_atdd.modules.eventos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record AprendizagemEventoDTO(
        @NotBlank @Size(max = 100) String eventoId,
        @NotNull Long alunoId,
        @NotBlank @Size(max = 50) String tipo,
        @NotNull Instant ocorridoEm,
        @NotBlank @Size(max = 100) String dispositivoId) { }
