package com.desafio.votacao.dto.voto;

import com.desafio.votacao.entity.TipoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarVotoRequest(
        
        @NotBlank(message = "Identificador do associado é obrigatório")
        @Size(max = 50, message = "Identificador do associado deve ter no máximo 50 caracteres")
        String associadoId,

        @NotNull(message = "Opção de voto é obrigatória")
        TipoVoto opcao

) {
}
