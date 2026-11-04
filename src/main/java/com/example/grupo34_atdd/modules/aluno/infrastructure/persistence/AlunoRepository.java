package com.example.grupo34_atdd.modules.aluno.infrastructure.persistence;

import com.example.grupo34_atdd.modules.aluno.infrastructure.persistence.AlunoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<AlunoEntity, Long> {

    Optional<AlunoEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
