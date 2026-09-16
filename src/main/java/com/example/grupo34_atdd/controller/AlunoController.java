package com.example.grupo34_atdd.controller;

import com.example.grupo34_atdd.dto.AlunoRequestDTO;
import com.example.grupo34_atdd.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.dto.LoginRequestDTO;
import com.example.grupo34_atdd.dto.TrocaMoedasRequestDTO;
import com.example.grupo34_atdd.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
@CrossOrigin(origins = "*")
@Tag(name = "Alunos", description = "Gerenciamento de alunos, autenticação e troca de moedas por cursos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo aluno com saldo inicial")
    public ResponseEntity<AlunoResponseDTO> cadastrar(@Valid @RequestBody AlunoRequestDTO dto) {
        AlunoResponseDTO response = alunoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os alunos cadastrados")
    public ResponseEntity<List<AlunoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(alunoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes de um aluno por ID")
    public ResponseEntity<AlunoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alunoService.buscarPorId(id));
    }

    @PostMapping("/{id}/trocar-moedas")
    @Operation(summary = "Trocar moedas por um curso (Regra de negócio)")
    public ResponseEntity<AlunoResponseDTO> trocarMoedas(
            @PathVariable Long id,
            @Valid @RequestBody TrocaMoedasRequestDTO dto) {
        return ResponseEntity.ok(alunoService.trocarMoedas(id, dto.getNomeCurso()));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar aluno via e-mail e senha")
    public ResponseEntity<AlunoResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(alunoService.autenticar(dto));
    }
}
