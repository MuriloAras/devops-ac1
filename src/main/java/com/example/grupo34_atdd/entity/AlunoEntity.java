package com.example.grupo34_atdd.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alunos")
public class AlunoEntity {

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

    public AlunoEntity() {
    }

    public AlunoEntity(String nome, String email, String senha, int saldoMoedas) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.saldoMoedas = saldoMoedas;
        this.cursosAdquiridos = new ArrayList<>();
    }

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
        return cursosAdquiridos;
    }

    public void setCursosAdquiridos(List<String> cursosAdquiridos) {
        this.cursosAdquiridos = cursosAdquiridos != null ? cursosAdquiridos : new ArrayList<>();
    }
}
