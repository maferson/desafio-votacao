package com.desafio.votacao.dto.sessao;

import java.time.OffsetDateTime;

public record SessaoVotacaoResponse(

        Long id,
        Long pautaId,
        OffsetDateTime inicio,
        OffsetDateTime fim
) {
}
