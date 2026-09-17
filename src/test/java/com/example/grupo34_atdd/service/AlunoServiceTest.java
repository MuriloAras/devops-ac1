package com.example.grupo34_atdd.service;

import com.example.grupo34_atdd.dto.AlunoRequestDTO;
import com.example.grupo34_atdd.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.dto.LoginRequestDTO;
import com.example.grupo34_atdd.entity.AlunoEntity;
import com.example.grupo34_atdd.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoService alunoService;

    private AlunoEntity entity;

    @BeforeEach
    void setUp() {
        entity = new AlunoEntity("Pedro", "pedro@email.com", "senha123", 100);
        entity.setId(1L);
    }

    @Test
    @DisplayName("Deve cadastrar um novo aluno com sucesso")
    void deveCadastrarAlunoComSucesso() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setNome("Pedro");
        dto.setEmail("pedro@email.com");
        dto.setSenha("senha123");
        dto.setSaldoMoedas(100);

        when(alunoRepository.existsByEmail("pedro@email.com")).thenReturn(false);
        when(alunoRepository.save(any(AlunoEntity.class))).thenReturn(entity);

        AlunoResponseDTO response = alunoService.cadastrar(dto);

        assertNotNull(response);
        assertEquals("Pedro", response.getNome());
        assertEquals("pedro@email.com", response.getEmail());
        assertEquals(100, response.getSaldoMoedas());
        verify(alunoRepository, times(1)).save(any(AlunoEntity.class));
    }

    @Test
    @DisplayName("Deve lançar conflito ao tentar cadastrar email já existente")
    void deveLancarConflitoAoCadastrarEmailExistente() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setEmail("pedro@email.com");

        when(alunoRepository.existsByEmail("pedro@email.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> alunoService.cadastrar(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(alunoRepository, never()).save(any(AlunoEntity.class));
    }

    @Test
    @DisplayName("Deve listar todos os alunos")
    void deveListarTodosOsAlunos() {
        when(alunoRepository.findAll()).thenReturn(List.of(entity));

        List<AlunoResponseDTO> lista = alunoService.listarTodos();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals("Pedro", lista.get(0).getNome());
    }

    @Test
    @DisplayName("Deve buscar aluno por ID com sucesso")
    void deveBuscarAlunoPorIdComSucesso() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(entity));

        AlunoResponseDTO response = alunoService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Deve lançar 404 ao buscar aluno por ID inexistente")
    void deveLancar404AoBuscarPorIdInexistente() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> alunoService.buscarPorId(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve trocar moedas com sucesso")
    void deveTrocarMoedasComSucesso() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(alunoRepository.save(any(AlunoEntity.class))).thenReturn(entity);

        AlunoResponseDTO response = alunoService.trocarMoedas(1L, "DevOps");

        assertNotNull(response);
        verify(alunoRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Deve lançar 404 ao trocar moedas de aluno inexistente")
    void deveLancar404AoTrocarMoedasDeAlunoInexistente() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> alunoService.trocarMoedas(99L, "DevOps"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar 400 ao trocar moedas com saldo insuficiente")
    void deveLancar400AoTrocarMoedasComSaldoInsuficiente() {
        AlunoEntity alunoSemSaldo = new AlunoEntity("Beatriz", "bia@email.com", "123", 1);
        alunoSemSaldo.setId(2L);

        when(alunoRepository.findById(2L)).thenReturn(Optional.of(alunoSemSaldo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> alunoService.trocarMoedas(2L, "DevOps"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve autenticar aluno com sucesso")
    void deveAutenticarAlunoComSucesso() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("pedro@email.com");
        dto.setSenha("senha123");

        when(alunoRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(entity));

        AlunoResponseDTO response = alunoService.autenticar(dto);

        assertNotNull(response);
        assertEquals("Pedro", response.getNome());
    }

    @Test
    @DisplayName("Deve lançar 401 ao autenticar com email não cadastrado")
    void deveLancar401AoAutenticarComEmailInexistente() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("inexistente@email.com");
        dto.setSenha("123");

        when(alunoRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> alunoService.autenticar(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    @DisplayName("Deve lançar 401 ao autenticar com senha incorreta")
    void deveLancar401AoAutenticarComSenhaIncorreta() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("pedro@email.com");
        dto.setSenha("senhaErrada");

        when(alunoRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(entity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> alunoService.autenticar(dto));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
