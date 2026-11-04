package com.example.grupo34_atdd.modules.cursos.api;

import com.example.grupo34_atdd.modules.cursos.api.dto.CursoConteudoDTO;
import com.example.grupo34_atdd.modules.cursos.api.dto.CursoResumoDTO;
import com.example.grupo34_atdd.modules.cursos.application.CursoService;
import com.example.grupo34_atdd.modules.ranking.api.dto.RespostaAvaliacaoDTO;
import com.example.grupo34_atdd.modules.ranking.api.dto.ResultadoAvaliacaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping("/cursos")
    @Operation(summary = "Listar cursos disponíveis")
    public ResponseEntity<List<CursoResumoDTO>> listarCursos() {
        return ResponseEntity.ok(cursoService.listarCursos());
    }

    @GetMapping("/alunos/{alunoId}/cursos/{slug}")
    @Operation(summary = "Acessar vídeo e avaliação de um curso adquirido")
    public ResponseEntity<CursoConteudoDTO> acessarCurso(@PathVariable Long alunoId, @PathVariable String slug) {
        return ResponseEntity.ok(cursoService.acessarCurso(alunoId, slug));
    }

    @PostMapping("/alunos/{alunoId}/cursos/{slug}/avaliacoes")
    @Operation(summary = "Enviar as respostas da avaliação de um curso")
    public ResponseEntity<ResultadoAvaliacaoDTO> avaliar(@PathVariable Long alunoId, @PathVariable String slug,
                                                         @Valid @RequestBody RespostaAvaliacaoDTO dto) {
        return ResponseEntity.ok(cursoService.avaliar(alunoId, slug, dto.getRespostas()));
    }
}
