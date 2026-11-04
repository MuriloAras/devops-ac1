package com.example.grupo34_atdd.modules.eventos.application;

import com.example.grupo34_atdd.modules.eventos.domain.ConsumidorNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConsumidorNotificacaoDemo implements ConsumidorNotificacao {
    private static final Logger log = LoggerFactory.getLogger(ConsumidorNotificacaoDemo.class);
    private final boolean ativo;

    public ConsumidorNotificacaoDemo(@Value("${supercursos.notificacoes.ativas:true}") boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public void consumir(String tipo, String payload) {
        if (!ativo) {
            throw new IllegalStateException("Consumidor de notificações desativado para simulação.");
        }
        log.info("Notificação processada: tipo={}, dados={}", tipo, payload);
    }
}
