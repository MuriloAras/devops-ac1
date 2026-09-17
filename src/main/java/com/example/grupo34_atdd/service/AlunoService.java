package com.example.grupo34_atdd.service;

import com.example.grupo34_atdd.domain.Aluno;
import com.example.grupo34_atdd.dto.AlunoRequestDTO;
import com.example.grupo34_atdd.dto.AlunoResponseDTO;
import com.example.grupo34_atdd.dto.LoginRequestDTO;
import com.example.grupo34_atdd.entity.AlunoEntity;
import com.example.grupo34_atdd.repository.AlunoRepository;
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
        return new AlunoResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<AlunoResponseDTO> listarTodos() {
        return alunoRepository.findAll().stream()
                .map(AlunoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AlunoResponseDTO buscarPorId(Long id) {
        AlunoEntity entity = alunoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado com ID: " + id));
        return new AlunoResponseDTO(entity);
    }

    @Transactional
    public AlunoResponseDTO trocarMoedas(Long alunoId, String nomeCurso) {
        AlunoEntity entity = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado com ID: " + alunoId));

        // Aplica a regra de negócio através do modelo puro de domínio Aluno
        Aluno alunoDominio = new Aluno(entity.getSaldoMoedas());
        boolean sucesso = alunoDominio.trocarMoedasPorCurso(nomeCurso);
        if (!sucesso) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo de moedas insuficiente para adquirir o curso.");
        }

        entity.setSaldoMoedas(alunoDominio.getSaldoMoedas());
        entity.getCursosAdquiridos().add(nomeCurso);

        AlunoEntity atualizado = alunoRepository.save(entity);
        return new AlunoResponseDTO(atualizado);
    }

    @Transactional(readOnly = true)
    public AlunoResponseDTO autenticar(LoginRequestDTO dto) {
        AlunoEntity entity = alunoRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));

        if (!entity.getSenha().equals(dto.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }

        return new AlunoResponseDTO(entity);
    }
}
