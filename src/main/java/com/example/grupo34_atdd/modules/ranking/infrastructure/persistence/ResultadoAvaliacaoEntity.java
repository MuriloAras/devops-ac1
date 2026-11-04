package com.example.grupo34_atdd.modules.ranking.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "resultados_avaliacao", uniqueConstraints =
        @UniqueConstraint(name = "uk_resultado_aluno_curso", columnNames = {"aluno_id", "curso_slug"}))
public class ResultadoAvaliacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "nome_aluno", nullable = false, length = 120)
    private String nomeAluno;

    @Column(name = "curso_slug", nullable = false, length = 100)
    private String cursoSlug;

    @Column(name = "melhor_pontuacao", nullable = false)
    private int melhorPontuacao;

    @Column(name = "melhor_numero_acertos", nullable = false)
    private int melhorNumeroAcertos;

    @Column(name = "ultima_tentativa_em", nullable = false)
    private Instant ultimaTentativaEm;

    public Long getId() {
        return id;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getCursoSlug() {
        return cursoSlug;
    }

    public void setCursoSlug(String cursoSlug) {
        this.cursoSlug = cursoSlug;
    }

    public int getMelhorPontuacao() {
        return melhorPontuacao;
    }

    public void setMelhorPontuacao(int melhorPontuacao) {
        this.melhorPontuacao = melhorPontuacao;
    }

    public int getMelhorNumeroAcertos() {
        return melhorNumeroAcertos;
    }

    public void setMelhorNumeroAcertos(int melhorNumeroAcertos) {
        this.melhorNumeroAcertos = melhorNumeroAcertos;
    }

    public Instant getUltimaTentativaEm() {
        return ultimaTentativaEm;
    }

    public void setUltimaTentativaEm(Instant ultimaTentativaEm) {
        this.ultimaTentativaEm = ultimaTentativaEm;
    }
}
