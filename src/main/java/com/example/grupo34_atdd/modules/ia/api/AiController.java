package com.example.grupo34_atdd.modules.ia.api;

import com.example.grupo34_atdd.modules.ia.api.dto.PerguntaAssistenteDTO;
import com.example.grupo34_atdd.modules.ia.api.dto.RecomendacoesDTO;
import com.example.grupo34_atdd.modules.ia.api.dto.RespostaAssistenteDTO;
import com.example.grupo34_atdd.modules.ia.application.AiServiceClient;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class AiController {
    private final AiServiceClient aiService;

    public AiController(AiServiceClient aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/recomendacoes")
    public ResponseEntity<RecomendacoesDTO> recomendar() {
        return ResponseEntity.ok(aiService.recomendar());
    }

    @PostMapping("/assistente")
    public ResponseEntity<RespostaAssistenteDTO> perguntar(@Valid @RequestBody PerguntaAssistenteDTO request) {
        return ResponseEntity.ok(aiService.perguntar(request.pergunta()));
    }
}
