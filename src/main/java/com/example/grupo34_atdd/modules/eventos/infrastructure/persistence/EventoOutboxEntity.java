package com.example.grupo34_atdd.modules.eventos.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "eventos_outbox")
public class EventoOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(nullable = false, length = 1000)
    private String payload;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "processado_em")
    private Instant processadoEm;

    @Column(nullable = false)
    private int tentativas;

    protected EventoOutboxEntity() { }

    public EventoOutboxEntity(String tipo, String payload) {
        this.tipo = tipo;
        this.payload = payload;
        this.criadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getPayload() { return payload; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getProcessadoEm() { return processadoEm; }
    public void setProcessadoEm(Instant processadoEm) { this.processadoEm = processadoEm; }
    public int getTentativas() { return tentativas; }
    public void registrarTentativa() { tentativas++; }
}
