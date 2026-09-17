package com.example.grupo34_atdd.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlunoEntityTest {

    @Test
    @DisplayName("Entity: Deve testar construtores, getters e setters da entidade JPA")
    void testAlunoEntity() {
        AlunoEntity entity = new AlunoEntity("Pedro", "pedro@email.com", "senha123", 100);
        entity.setId(1L);
        entity.setNome("Pedro Silva");
        entity.setEmail("pedro.silva@email.com");
        entity.setSenha("novaSenha");
        entity.setSaldoMoedas(80);
        entity.setCursosAdquiridos(List.of("DevOps"));

        assertEquals(1L, entity.getId());
        assertEquals("Pedro Silva", entity.getNome());
        assertEquals("pedro.silva@email.com", entity.getEmail());
        assertEquals("novaSenha", entity.getSenha());
        assertEquals(80, entity.getSaldoMoedas());
        assertEquals(1, entity.getCursosAdquiridos().size());
        assertEquals("DevOps", entity.getCursosAdquiridos().get(0));

        entity.setCursosAdquiridos(null);
        assertTrue(entity.getCursosAdquiridos().isEmpty());

        AlunoEntity vazio = new AlunoEntity();
        assertNotNull(vazio);
    }
}
