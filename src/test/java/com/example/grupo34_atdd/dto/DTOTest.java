package com.example.grupo34_atdd.dto;

import com.example.grupo34_atdd.domain.Aluno;
import com.example.grupo34_atdd.entity.AlunoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DTOTest {

    @Test
    @DisplayName("DTO: Deve testar AlunoRequestDTO")
    void testAlunoRequestDTO() {
        AlunoRequestDTO dto = new AlunoRequestDTO("Nome", "email@email.com", "senha", 50);
        dto.setNome("Nome2");
        dto.setEmail("email2@email.com");
        dto.setSenha("senha2");
        dto.setSaldoMoedas(60);

        assertEquals("Nome2", dto.getNome());
        assertEquals("email2@email.com", dto.getEmail());
        assertEquals("senha2", dto.getSenha());
        assertEquals(60, dto.getSaldoMoedas());

        AlunoRequestDTO dtoPadrao = new AlunoRequestDTO();
        assertEquals(0, dtoPadrao.getSaldoMoedas());
    }

    @Test
    @DisplayName("DTO: Deve testar AlunoResponseDTO")
    void testAlunoResponseDTO() {
        AlunoEntity entity = new AlunoEntity("Nome", "email@email.com", "senha", 100);
        entity.setId(10L);
        entity.setCursosAdquiridos(List.of("Curso 1"));

        AlunoResponseDTO dto = new AlunoResponseDTO(entity);
        assertEquals(10L, dto.getId());
        assertEquals("Nome", dto.getNome());
        assertEquals("email@email.com", dto.getEmail());
        assertEquals(100, dto.getSaldoMoedas());
        assertEquals(1, dto.getCursosAdquiridos().size());

        AlunoResponseDTO dto2 = new AlunoResponseDTO(20L, "Nome2", "email2@email.com", 80, List.of("C1", "C2"));
        dto2.setId(30L);
        dto2.setNome("Nome3");
        dto2.setEmail("email3@email.com");
        dto2.setSaldoMoedas(90);
        dto2.setCursosAdquiridos(List.of("C3"));

        assertEquals(30L, dto2.getId());
        assertEquals("Nome3", dto2.getNome());
        assertEquals("email3@email.com", dto2.getEmail());
        assertEquals(90, dto2.getSaldoMoedas());
        assertEquals(1, dto2.getCursosAdquiridos().size());

        AlunoResponseDTO dtoVazio = new AlunoResponseDTO();
        assertNotNull(dtoVazio);
    }

    @Test
    @DisplayName("DTO: Deve testar LoginRequestDTO")
    void testLoginRequestDTO() {
        LoginRequestDTO dto = new LoginRequestDTO("email@email.com", "senha123");
        dto.setEmail("outro@email.com");
        dto.setSenha("outraSenha");

        assertEquals("outro@email.com", dto.getEmail());
        assertEquals("outraSenha", dto.getSenha());

        LoginRequestDTO dtoVazio = new LoginRequestDTO();
        assertNotNull(dtoVazio);
    }

    @Test
    @DisplayName("DTO: Deve testar TrocaMoedasRequestDTO")
    void testTrocaMoedasRequestDTO() {
        TrocaMoedasRequestDTO dto = new TrocaMoedasRequestDTO("DevOps");
        dto.setNomeCurso("Docker");

        assertEquals("Docker", dto.getNomeCurso());

        TrocaMoedasRequestDTO dtoVazio = new TrocaMoedasRequestDTO();
        assertNotNull(dtoVazio);
    }
}
