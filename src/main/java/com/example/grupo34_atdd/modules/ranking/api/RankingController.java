package com.example.grupo34_atdd.modules.ranking.api;

import com.example.grupo34_atdd.modules.ranking.api.dto.PosicaoRankingDTO;
import com.example.grupo34_atdd.modules.ranking.application.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@CrossOrigin(origins = "*")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    @Operation(summary = "Consultar o ranking de alunos por pontos")
    public ResponseEntity<List<PosicaoRankingDTO>> consultar() {
        return ResponseEntity.ok(rankingService.consultar());
    }
}
