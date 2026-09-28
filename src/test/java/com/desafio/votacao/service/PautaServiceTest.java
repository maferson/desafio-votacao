package com.desafio.votacao.service;

import com.desafio.votacao.dto.pauta.CriarPautaRequest;
import com.desafio.votacao.dto.pauta.PautaResponse;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.repository.PautaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    private static final String TITULO = "Pauta teste";
    private static final String DESCRICAO = "Descrição da pauta";

    @InjectMocks
    private PautaService service;

    @Mock
    private PautaRepository pautaRepository;

    @Test
    void deveCriarPautaComSucesso() {
        givenPautaSalvaComSucesso();

        PautaResponse response = whenCriarPauta();

        thenPautaDeveSerCriada(response);
        thenPautaDeveSerSalva();
    }


    private void givenPautaSalvaComSucesso() {
        when(pautaRepository.save(any(Pauta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }


    private PautaResponse whenCriarPauta() {
        CriarPautaRequest request = new CriarPautaRequest(
                TITULO,
                DESCRICAO
        );

        return service.criar(request);
    }


    private void thenPautaDeveSerCriada(PautaResponse response) {
        assertEquals(TITULO, response.titulo());
        assertEquals(DESCRICAO, response.descricao());
    }

    private void thenPautaDeveSerSalva() {
        verify(pautaRepository)
                .save(any(Pauta.class));
    }

}