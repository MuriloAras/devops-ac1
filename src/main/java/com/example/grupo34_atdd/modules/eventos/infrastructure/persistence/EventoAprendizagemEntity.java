package com.example.grupo34_atdd.modules.eventos.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "eventos_aprendizagem")
public class EventoAprendizagemEntity {
    @Id
    @Column(name = "evento_id", length = 100)
    private String eventoId;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "dispositivo_id", nullable = false, length = 100)
    private String dispositivoId;

    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;

    protected EventoAprendizagemEntity() { }

    public EventoAprendizagemEntity(String eventoId, Long alunoId, String tipo, Instant ocorridoEm,
                                    String dispositivoId, Instant recebidoEm) {
        this.eventoId = eventoId;
        this.alunoId = alunoId;
        this.tipo = tipo;
        this.ocorridoEm = ocorridoEm;
        this.dispositivoId = dispositivoId;
        this.recebidoEm = recebidoEm;
    }

    public String getEventoId() { return eventoId; }
    public Long getAlunoId() { return alunoId; }
    public String getTipo() { return tipo; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public String getDispositivoId() { return dispositivoId; }
    public Instant getRecebidoEm() { return recebidoEm; }
}
