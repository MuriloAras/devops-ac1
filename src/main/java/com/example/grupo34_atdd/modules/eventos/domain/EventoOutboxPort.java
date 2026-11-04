package com.example.grupo34_atdd.modules.eventos.domain;

public interface EventoOutboxPort {
    void publicar(String tipo, String payload);
}
