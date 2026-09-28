package com.desafio.votacao.dto.voto;

import com.desafio.votacao.entity.TipoVoto;
import java.time.OffsetDateTime;

public record VotoResponse(
        Long id,
        Long pautaId,
        String associadoId,
        TipoVoto opcao,
        OffsetDateTime createdAt
) {
}
