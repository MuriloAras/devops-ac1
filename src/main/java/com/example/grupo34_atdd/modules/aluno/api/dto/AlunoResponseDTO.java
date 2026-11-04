package com.example.grupo34_atdd.modules.aluno.api.dto;

import java.util.List;

public class AlunoResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private int saldoMoedas;
    private List<String> cursosAdquiridos;

    public AlunoResponseDTO() {
    }

    public AlunoResponseDTO(Long id, String nome, String email, int saldoMoedas, List<String> cursosAdquiridos) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.saldoMoedas = saldoMoedas;
        this.cursosAdquiridos = cursosAdquiridos;
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
        this.cursosAdquiridos = cursosAdquiridos;
    }
}
