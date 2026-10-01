package com.desafio.votacao.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("performance")
public class PerformanceAssociadoClient implements AssociadoClient {

    @Override
    public StatusVotoAssociado consultarSituacao(String associadoId) {
        return StatusVotoAssociado.ABLE_TO_VOTE;
    }
}
