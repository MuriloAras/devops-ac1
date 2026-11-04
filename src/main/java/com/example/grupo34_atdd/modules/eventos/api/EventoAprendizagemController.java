package com.example.grupo34_atdd.modules.eventos.api;

import com.example.grupo34_atdd.modules.eventos.api.dto.AprendizagemEventoDTO;
import com.example.grupo34_atdd.modules.eventos.api.dto.IngressoEventoDTO;
import com.example.grupo34_atdd.modules.eventos.application.EventoAprendizagemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eventos-aprendizagem")
@CrossOrigin(origins = "*")
public class EventoAprendizagemController {
    private final EventoAprendizagemService service;

    public EventoAprendizagemController(EventoAprendizagemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<IngressoEventoDTO> receber(@Valid @RequestBody AprendizagemEventoDTO evento) {
        return ResponseEntity.accepted().body(service.registrar(evento));
    }
}
