package com.example.grupo34_atdd.modules.ranking.application;

import com.example.grupo34_atdd.modules.ranking.api.dto.PosicaoRankingDTO;
import com.example.grupo34_atdd.modules.ranking.domain.EntradaRanking;
import com.example.grupo34_atdd.modules.ranking.domain.ResultadoAvaliacaoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private final ResultadoAvaliacaoPort resultadoPort;

    public RankingService(ResultadoAvaliacaoPort resultadoPort) {
        this.resultadoPort = resultadoPort;
    }

    @Transactional(readOnly = true)
    public List<PosicaoRankingDTO> consultar() {
        List<EntradaRanking> registros = resultadoPort.listarRanking();
        List<PosicaoRankingDTO> ranking = new ArrayList<>(registros.size());
        long pontosAnteriores = Long.MIN_VALUE;
        int posicaoAtual = 0;

        for (int indice = 0; indice < registros.size(); indice++) {
            EntradaRanking registro = registros.get(indice);
            long pontos = registro.pontos();
            if (pontos != pontosAnteriores) {
                posicaoAtual = indice + 1;
                pontosAnteriores = pontos;
            }
            ranking.add(new PosicaoRankingDTO(posicaoAtual, registro.alunoId(), registro.nome(), pontos));
        }
        return ranking;
    }
}
