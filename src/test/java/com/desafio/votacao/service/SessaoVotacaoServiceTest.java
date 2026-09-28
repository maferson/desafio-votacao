package com.desafio.votacao.service;

import com.desafio.votacao.dto.sessao.AbrirSessaoRequest;
import com.desafio.votacao.dto.sessao.SessaoVotacaoResponse;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.exception.ConflitoNegocioException;
import com.desafio.votacao.repository.PautaRepository;
import com.desafio.votacao.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoServiceTest {

    static final Long PAUTA_ID = 1L;

    @InjectMocks
    private SessaoVotacaoService service;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Test
    void deveAbrirSessaoComDuracaoPadraoDeUmMinuto() {
        givenPautaExistente();
        givenPautaSemSessao();
        givenSessaoSalvaComSucesso();

        var response = whenAbrirSessao(null);

        thenDuracaoDeveSer(response, 1);
    }

    @Test
    void deveAbrirSessaoComDuracaoInformada() {
        givenPautaExistente();
        givenPautaSemSessao();
        givenSessaoSalvaComSucesso();

        var response = whenAbrirSessao(5L);

        thenDuracaoDeveSer(response, 5);
    }

    @Test
    void naoDeveAbrirSegundaSessaoParaMesmaPauta() {
        givenPautaExistente();
        givenPautaComSessao();

        whenAbrirSessaoThenExpectConflito(5L);

        thenSessaoNaoDeveSerSalva();
    }


    private void givenPautaExistente() {
        var pauta = new Pauta(
                "Pauta teste",
                "Descrição"
        );

        when(pautaRepository.findById(PAUTA_ID))
                .thenReturn(Optional.of(pauta));
    }

    private void givenPautaSemSessao() {
        when(sessaoVotacaoRepository.existsByPauta_Id(PAUTA_ID))
                .thenReturn(false);
    }

    private void givenPautaComSessao() {
        when(sessaoVotacaoRepository.existsByPauta_Id(PAUTA_ID))
                .thenReturn(true);
    }

    private void givenSessaoSalvaComSucesso() {
        when(sessaoVotacaoRepository.saveAndFlush(
                any(SessaoVotacao.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));
    }


    private SessaoVotacaoResponse whenAbrirSessao(Long duracaoMinutos) {
        return service.abrir(
                PAUTA_ID,
                new AbrirSessaoRequest(duracaoMinutos)
        );
    }

    private void whenAbrirSessaoThenExpectConflito(Long duracaoMinutos) {
        assertThrows(
                ConflitoNegocioException.class,
                () -> whenAbrirSessao(duracaoMinutos)
        );
    }


    private void thenDuracaoDeveSer(
            SessaoVotacaoResponse response,
            long duracaoEsperada
    ) {
        var duracao = Duration.between(
                response.inicio(),
                response.fim()
        );

        assertEquals(
                duracaoEsperada,
                duracao.toMinutes()
        );
    }

    private void thenSessaoNaoDeveSerSalva() {
        verify(sessaoVotacaoRepository, never())
                .saveAndFlush(any(SessaoVotacao.class));
    }
}
