package com.desafio.votacao.dto.sessao;

import jakarta.validation.constraints.Positive;

public record AbrirSessaoRequest(

        @Positive(message = "Duração deve ser maior que zero")
        Long duracaoMinutos

) {
}
