package com.example.grupo34_atdd.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlunoTest {

    @Test
    @DisplayName("Cenário Murilo - Deve trocar moedas por curso quando saldo suficiente (3 moedas)")
    void deveTrocarMoedasPorCursoQuandoSaldoSuficiente() {
        Aluno aluno = new Aluno(3);

        boolean trocou = aluno.trocarMoedasPorCurso("Java Avançado");

        assertTrue(trocou);
        assertTrue(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(0, aluno.getSaldoMoedas());
    }

    @Test
    @DisplayName("Cenário Beatriz - Não deve trocar moedas quando saldo for insuficiente (2 moedas)")
    void naoDeveTrocarMoedasPorCursoQuandoSaldoInsuficiente() {
        Aluno aluno = new Aluno(2);

        boolean trocou = aluno.trocarMoedasPorCurso("Java Avançado");

        assertFalse(trocou);
        assertFalse(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(2, aluno.getSaldoMoedas());
    }

    @Test
    @DisplayName("Cenário Pedro - Deve trocar moedas por curso quando saldo superior (5 moedas)")
    void deveTrocarMoedasPorCursoQuandoSaldoSuperior() {
        Aluno aluno = new Aluno(5);

        boolean trocou = aluno.trocarMoedasPorCurso("Java Avançado");

        assertTrue(trocou);
        assertTrue(aluno.getCursosAdquiridos().contains("Java Avançado"));
        assertEquals(2, aluno.getSaldoMoedas());
    }
}
