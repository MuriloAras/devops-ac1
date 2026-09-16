package com.example.grupo34_atdd.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Aluno {

    private static final int CUSTO_CURSO = 3;

    private int saldoMoedas;
    private List<String> cursosAdquiridos;

    public Aluno(int saldoMoedas) {
        this.saldoMoedas = saldoMoedas;
        this.cursosAdquiridos = new ArrayList<>();
    }

    //ETAPA BLUE
    public boolean trocarMoedasPorCurso(String nomeCurso) {
        if (saldoMoedas >= CUSTO_CURSO) {
            this.saldoMoedas -= CUSTO_CURSO;
            this.cursosAdquiridos.add(nomeCurso);
            return true;
        }
        return false;
    }

    public int getSaldoMoedas() {
        return saldoMoedas;
    }

    public List<String> getCursosAdquiridos() {
        return Collections.unmodifiableList(cursosAdquiridos);
    }
}
