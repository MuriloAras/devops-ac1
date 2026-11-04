package com.example.grupo34_atdd.modules.aluno.application;

import com.example.grupo34_atdd.modules.aluno.domain.Aluno;
import com.example.grupo34_atdd.modules.aluno.api.dto.AlunoRequestDTO;
import com.example.grupo34_atdd.modules.aluno.api.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.modules.aluno.api.dto.LoginRequestDTO;
import com.example.grupo34_atdd.modules.aluno.infrastructure.persistence.AlunoEntity;
import com.example.grupo34_atdd.modules.aluno.infrastructure.persistence.AlunoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public AlunoResponseDTO cadastrar(AlunoRequestDTO dto) {
        if (alunoRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado no sistema.");
        }

        AlunoEntity entity = new AlunoEntity(
                dto.getNome(),
                dto.getEmail(),
                dto.getSenha(),
                dto.getSaldoMoedas()
        );

        AlunoEntity salvo = alunoRepository.save(entity);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<AlunoResponseDTO> listarTodos() {
        return alunoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlunoResponseDTO buscarPorId(Long id) {
        AlunoEntity entity = alunoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado com ID: " + id));
        return toResponse(entity);
    }

    @Transactional
    public AlunoResponseDTO trocarMoedas(Long alunoId, String nomeCurso) {
        AlunoEntity entity = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado com ID: " + alunoId));

        Aluno alunoDominio = new Aluno(entity.getSaldoMoedas());
        boolean sucesso = alunoDominio.trocarMoedasPorCurso(nomeCurso);
        if (!sucesso) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo de moedas insuficiente para adquirir o curso.");
        }

        entity.setSaldoMoedas(alunoDominio.getSaldoMoedas());
        entity.getCursosAdquiridos().add(nomeCurso);

        AlunoEntity atualizado = alunoRepository.save(entity);
        return toResponse(atualizado);
    }

    @Transactional(readOnly = true)
    public AlunoResponseDTO autenticar(LoginRequestDTO dto) {
        AlunoEntity entity = alunoRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));

        if (!entity.getSenha().equals(dto.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public boolean temCursoAdquirido(Long alunoId, String nomeCurso) {
        return alunoRepository.findById(alunoId)
                .map(aluno -> aluno.getCursosAdquiridos().stream()
                        .anyMatch(curso -> curso.equalsIgnoreCase(nomeCurso)))
                .orElse(false);
    }

    private AlunoResponseDTO toResponse(AlunoEntity entity) {
        return new AlunoResponseDTO(entity.getId(), entity.getNome(), entity.getEmail(),
                entity.getSaldoMoedas(), entity.getCursosAdquiridos());
    }
}
