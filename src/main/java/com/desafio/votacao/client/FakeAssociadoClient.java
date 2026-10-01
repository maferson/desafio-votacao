package com.desafio.votacao.client;

import com.desafio.votacao.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("!performance")
public class FakeAssociadoClient implements AssociadoClient {

    @Override
    public StatusVotoAssociado consultarSituacao(String associadoId) {
        boolean cpfValido = ThreadLocalRandom.current().nextBoolean();

        if (!cpfValido) {
            throw new RecursoNaoEncontradoException("CPF inválido");
        }

        boolean apto = ThreadLocalRandom.current().nextBoolean();

        return apto
                ? StatusVotoAssociado.ABLE_TO_VOTE
                : StatusVotoAssociado.UNABLE_TO_VOTE;
    }
}
