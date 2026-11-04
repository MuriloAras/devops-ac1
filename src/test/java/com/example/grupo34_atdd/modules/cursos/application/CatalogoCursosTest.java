package com.example.grupo34_atdd.modules.cursos.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogoCursosTest {

    @Test
    void catalogoContemOsTresCursosComVideoEDezQuestoesCada() {
        var cursos = new CatalogoCursos().listar();

        assertEquals(3, cursos.size());
        assertEquals(CatalogoCursos.TITULO_PYTHON, cursos.get(0).titulo());
        assertEquals(CatalogoCursos.TITULO_CICD, cursos.get(1).titulo());
        assertEquals(CatalogoCursos.TITULO_CSHARP, cursos.get(2).titulo());
        assertEquals("https://www.youtube-nocookie.com/embed/95r8nJLq5zU", cursos.get(1).videoUrl());
        assertEquals("https://www.youtube-nocookie.com/embed/HOpCMKNJEFg", cursos.get(2).videoUrl());
        assertTrue(cursos.stream().allMatch(curso -> curso.questoes().size() == 10));
    }
}
