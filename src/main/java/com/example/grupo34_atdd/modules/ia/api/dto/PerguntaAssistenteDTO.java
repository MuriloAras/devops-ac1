package com.example.grupo34_atdd.modules.ia.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerguntaAssistenteDTO(@NotBlank @Size(max = 500) String pergunta) { }
