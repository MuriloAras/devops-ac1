package com.example.grupo34_atdd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class AlunoRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @Min(value = 0, message = "O saldo inicial de moedas não pode ser negativo")
    private int saldoMoedas;

    public AlunoRequestDTO() {
    }

    public AlunoRequestDTO(String nome, String email, String senha, int saldoMoedas) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.saldoMoedas = saldoMoedas;
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
}
