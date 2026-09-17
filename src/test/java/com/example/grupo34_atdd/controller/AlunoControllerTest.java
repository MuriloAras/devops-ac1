package com.example.grupo34_atdd.controller;

import com.example.grupo34_atdd.dto.AlunoRequestDTO;
import com.example.grupo34_atdd.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.dto.LoginRequestDTO;
import com.example.grupo34_atdd.dto.TrocaMoedasRequestDTO;
import com.example.grupo34_atdd.service.AlunoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlunoControllerTest {

    @Mock
    private AlunoService alunoService;

    @InjectMocks
    private AlunoController alunoController;

    @InjectMocks
    private HomeController homeController;

    @Test
    @DisplayName("Controller: Deve cadastrar aluno")
    void deveCadastrar() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        AlunoResponseDTO response = new AlunoResponseDTO();
        when(alunoService.cadastrar(any(AlunoRequestDTO.class))).thenReturn(response);

        ResponseEntity<AlunoResponseDTO> result = alunoController.cadastrar(dto);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Controller: Deve listar alunos")
    void deveListarTodos() {
        when(alunoService.listarTodos()).thenReturn(List.of(new AlunoResponseDTO()));

        ResponseEntity<List<AlunoResponseDTO>> result = alunoController.listarTodos();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("Controller: Deve buscar aluno por ID")
    void deveBuscarPorId() {
        AlunoResponseDTO response = new AlunoResponseDTO();
        when(alunoService.buscarPorId(1L)).thenReturn(response);

        ResponseEntity<AlunoResponseDTO> result = alunoController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Controller: Deve trocar moedas")
    void deveTrocarMoedas() {
        TrocaMoedasRequestDTO dto = new TrocaMoedasRequestDTO();
        dto.setNomeCurso("Java");
        AlunoResponseDTO response = new AlunoResponseDTO();
        when(alunoService.trocarMoedas(eq(1L), eq("Java"))).thenReturn(response);

        ResponseEntity<AlunoResponseDTO> result = alunoController.trocarMoedas(1L, dto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Controller: Deve fazer login")
    void deveFazerLogin() {
        LoginRequestDTO dto = new LoginRequestDTO();
        AlunoResponseDTO response = new AlunoResponseDTO();
        when(alunoService.autenticar(any(LoginRequestDTO.class))).thenReturn(response);

        ResponseEntity<AlunoResponseDTO> result = alunoController.login(dto);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Controller: Home deve redirecionar para Swagger")
    void homeDeveRedirecionar() {
        String redirect = homeController.home();
        assertEquals("redirect:/swagger-ui/index.html", redirect);
    }
}
