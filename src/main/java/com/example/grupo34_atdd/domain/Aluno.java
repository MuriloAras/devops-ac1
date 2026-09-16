package com.example.grupo34_atdd.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "alunos")
public class Aluno {

    private static final int CUSTO_CURSO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    private int saldoMoedas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aluno_cursos", joinColumns = @JoinColumn(name = "aluno_id"))
    @Column(name = "curso")
    private List<String> cursosAdquiridos = new ArrayList<>();

    // Construtor padrão
    public Aluno() {
        this.cursosAdquiridos = new ArrayList<>();
    }

    // Construtor utilizado nos testes de domínio
    public Aluno(int saldoMoedas) {
        this.saldoMoedas = saldoMoedas;
        this.cursosAdquiridos = new ArrayList<>();
    }

    // Construtor para autenticação
    public Aluno(String nome, String email, String senha, int saldoMoedas) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.saldoMoedas = saldoMoedas;
        this.cursosAdquiridos = new ArrayList<>();
    }

    // ETAPA BLUE: Regra de negócio de troca de moedas por curso
    public boolean trocarMoedasPorCurso(String nomeCurso) {
        if (saldoMoedas >= CUSTO_CURSO) {
            this.saldoMoedas -= CUSTO_CURSO;
            this.cursosAdquiridos.add(nomeCurso);
            return true;
        }
        return false;
    }

    // Get e Set
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getSaldoMoedas() {
        return saldoMoedas;
    }

    public void setSaldoMoedas(int saldoMoedas) {
        this.saldoMoedas = saldoMoedas;
    }

    public List<String> getCursosAdquiridos() {
        return Collections.unmodifiableList(cursosAdquiridos);
    }

    public void setCursosAdquiridos(List<String> cursosAdquiridos) {
        this.cursosAdquiridos = cursosAdquiridos != null ? cursosAdquiridos : new ArrayList<>();
    }
}

