package com.desafio.votacao.dto.pauta;

import java.time.OffsetDateTime;

public record PautaResponse(
        Long id,
        String titulo,
        String descricao,
        OffsetDateTime createdAt
) {
}
