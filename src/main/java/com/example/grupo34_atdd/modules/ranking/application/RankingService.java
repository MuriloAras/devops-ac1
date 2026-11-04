package com.example.grupo34_atdd.modules.ranking.application;

import com.example.grupo34_atdd.modules.ranking.api.dto.PosicaoRankingDTO;
import com.example.grupo34_atdd.modules.ranking.infrastructure.persistence.ProjecaoRanking;
import com.example.grupo34_atdd.modules.ranking.infrastructure.persistence.ResultadoAvaliacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private final ResultadoAvaliacaoRepository resultadoRepository;

    public RankingService(ResultadoAvaliacaoRepository resultadoRepository) {
        this.resultadoRepository = resultadoRepository;
    }

    @Transactional(readOnly = true)
    public List<PosicaoRankingDTO> consultar() {
        List<ProjecaoRanking> registros = resultadoRepository.buscarRanking();
        List<PosicaoRankingDTO> ranking = new ArrayList<>(registros.size());
        long pontosAnteriores = Long.MIN_VALUE;
        int posicaoAtual = 0;

        for (int indice = 0; indice < registros.size(); indice++) {
            ProjecaoRanking registro = registros.get(indice);
            long pontos = registro.getPontos() == null ? 0 : registro.getPontos();
            if (pontos != pontosAnteriores) {
                posicaoAtual = indice + 1;
                pontosAnteriores = pontos;
            }
            ranking.add(new PosicaoRankingDTO(posicaoAtual, registro.getAlunoId(), registro.getNome(), pontos));
        }
        return ranking;
    }
}
