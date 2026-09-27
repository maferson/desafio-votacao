package com.desafio.votacao.dto.pauta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPautaRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String titulo,

        String descricao
) {
}
