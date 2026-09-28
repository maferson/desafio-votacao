package com.desafio.votacao.dto.voto;

public record ResultadoVotacaoResponse(

        Long pautaId,
        long sim,
        long nao,
        long total
) {
}
