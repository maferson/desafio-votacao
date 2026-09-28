package com.desafio.votacao.service;

import com.desafio.votacao.dto.voto.RegistrarVotoRequest;
import com.desafio.votacao.dto.voto.ResultadoVotacaoResponse;
import com.desafio.votacao.dto.voto.VotoResponse;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.entity.TipoVoto;
import com.desafio.votacao.entity.Voto;
import com.desafio.votacao.exception.ConflitoNegocioException;
import com.desafio.votacao.exception.RecursoNaoEncontradoException;
import com.desafio.votacao.repository.PautaRepository;
import com.desafio.votacao.repository.SessaoVotacaoRepository;
import com.desafio.votacao.repository.VotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    static final Long PAUTA_ID = 1L;
    private static final String ASSOCIADO_ID = "ASSOCIADO-001";

    @InjectMocks
    private VotoService service;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @Test
    void deveRegistrarVotoComSucesso() {
        givenSessaoAberta();
        givenAssociadoAindaNaoVotou();
        givenVotoSalvoComSucesso();

        VotoResponse response = whenRegistrarVoto(TipoVoto.SIM);

        thenVotoDeveSerRegistrado(response, TipoVoto.SIM);
        thenVotoDeveSerSalvo();
    }

    @Test
    void naoDevePermitirVotoDuplicado() {
        givenSessaoAberta();
        givenAssociadoJaVotou();

        whenRegistrarVotoThenExpectConflito(TipoVoto.SIM);

        thenVotoNaoDeveSerSalvo();
    }

    @Test
    void naoDevePermitirVotoComSessaoEncerrada() {
        givenSessaoEncerrada();

        whenRegistrarVotoThenExpectConflito(TipoVoto.SIM);

        thenVotoNaoDeveSerSalvo();
    }

    @Test
    void deveRetornarErroQuandoSessaoNaoExistir() {
        givenSessaoNaoExistente();

        whenRegistrarVotoThenExpectRecursoNaoEncontrado(TipoVoto.SIM);

        thenVotoNaoDeveSerSalvo();
    }

    @Test
    void deveTratarConcorrenciaDeVotoDuplicado() {
        givenSessaoAberta();
        givenAssociadoAindaNaoVotou();
        givenErroDeIntegridadeAoSalvarVoto();

        whenRegistrarVotoThenExpectConflito(TipoVoto.SIM);
    }

    @Test
    void deveRetornarResultadoDaVotacao() {
        givenPautaExistente();
        givenTresVotosSim();
        givenDoisVotosNao();

        ResultadoVotacaoResponse response = whenConsultarResultado();

        thenResultadoDeveSer(response, 3L, 2L, 5L);
    }

    @Test
    void deveRetornarErroAoConsultarResultadoDePautaInexistente() {
        givenPautaNaoExistente();

        whenConsultarResultadoThenExpectRecursoNaoEncontrado();
    }


    private void givenSessaoAberta() {
        Pauta pauta = new Pauta(
                "Pauta teste",
                "Descrição"
        );

        SessaoVotacao sessao = new SessaoVotacao(
                pauta,
                OffsetDateTime.now().minusMinutes(1),
                OffsetDateTime.now().plusMinutes(5)
        );

        when(sessaoVotacaoRepository.findByPauta_Id(PAUTA_ID))
                .thenReturn(Optional.of(sessao));
    }

    private void givenSessaoEncerrada() {
        Pauta pauta = new Pauta(
                "Pauta teste",
                "Descrição"
        );

        SessaoVotacao sessao = new SessaoVotacao(
                pauta,
                OffsetDateTime.now().minusMinutes(10),
                OffsetDateTime.now().minusMinutes(1)
        );

        when(sessaoVotacaoRepository.findByPauta_Id(PAUTA_ID))
                .thenReturn(Optional.of(sessao));
    }

    private void givenSessaoNaoExistente() {
        when(sessaoVotacaoRepository.findByPauta_Id(PAUTA_ID))
                .thenReturn(Optional.empty());
    }

    private void givenAssociadoAindaNaoVotou() {
        when(votoRepository.existsByPauta_IdAndAssociadoId(
                PAUTA_ID,
                ASSOCIADO_ID
        )).thenReturn(false);
    }

    private void givenAssociadoJaVotou() {
        when(votoRepository.existsByPauta_IdAndAssociadoId(
                PAUTA_ID,
                ASSOCIADO_ID
        )).thenReturn(true);
    }

    private void givenVotoSalvoComSucesso() {
        when(votoRepository.saveAndFlush(any(Voto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenErroDeIntegridadeAoSalvarVoto() {
        when(votoRepository.saveAndFlush(any(Voto.class)))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "Voto duplicado"
                        )
                );
    }

    private void givenPautaExistente() {
        when(pautaRepository.existsById(PAUTA_ID))
                .thenReturn(true);
    }

    private void givenPautaNaoExistente() {
        when(pautaRepository.existsById(PAUTA_ID))
                .thenReturn(false);
    }

    private void givenTresVotosSim() {
        when(votoRepository.countByPauta_IdAndOpcao(
                PAUTA_ID,
                TipoVoto.SIM
        )).thenReturn(3L);
    }

    private void givenDoisVotosNao() {
        when(votoRepository.countByPauta_IdAndOpcao(
                PAUTA_ID,
                TipoVoto.NAO
        )).thenReturn(2L);
    }


    private VotoResponse whenRegistrarVoto(TipoVoto tipoVoto) {
        return service.registrar(
                PAUTA_ID,
                new RegistrarVotoRequest(
                        ASSOCIADO_ID,
                        tipoVoto
                )
        );
    }

    private void whenRegistrarVotoThenExpectConflito(
            TipoVoto tipoVoto
    ) {
        assertThrows(
                ConflitoNegocioException.class,
                () -> whenRegistrarVoto(tipoVoto)
        );
    }

    private void whenRegistrarVotoThenExpectRecursoNaoEncontrado(
            TipoVoto tipoVoto
    ) {
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> whenRegistrarVoto(tipoVoto)
        );
    }

    private ResultadoVotacaoResponse whenConsultarResultado() {
        return service.resultado(PAUTA_ID);
    }

    private void whenConsultarResultadoThenExpectRecursoNaoEncontrado() {
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> service.resultado(PAUTA_ID)
        );
    }


    private void thenVotoDeveSerRegistrado(
            VotoResponse response,
            TipoVoto tipoVotoEsperado
    ) {
        assertEquals(
                ASSOCIADO_ID,
                response.associadoId()
        );

        assertEquals(
                tipoVotoEsperado,
                response.opcao()
        );
    }

    private void thenVotoDeveSerSalvo() {
        verify(votoRepository)
                .saveAndFlush(any(Voto.class));
    }

    private void thenVotoNaoDeveSerSalvo() {
        verify(votoRepository, never())
                .saveAndFlush(any(Voto.class));
    }

    private void thenResultadoDeveSer(
            ResultadoVotacaoResponse response,
            long votosSim,
            long votosNao,
            long total
    ) {
        assertEquals(
                PAUTA_ID,
                response.pautaId()
        );

        assertEquals(
                votosSim,
                response.sim()
        );

        assertEquals(
                votosNao,
                response.nao()
        );

        assertEquals(
                total,
                response.total()
        );
    }
}