package com.desafio.votacao.controller;

import com.desafio.votacao.dto.sessao.AbrirSessaoRequest;
import com.desafio.votacao.dto.sessao.SessaoVotacaoResponse;
import com.desafio.votacao.exception.ConflitoNegocioException;
import com.desafio.votacao.exception.RecursoNaoEncontradoException;
import com.desafio.votacao.service.SessaoVotacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessaoVotacaoController.class)
class SessaoVotacaoControllerTest {

    private static final Long PAUTA_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessaoVotacaoService sessaoVotacaoService;

    @Test
    void deveAbrirSessaoComSucesso() throws Exception {
        givenSessaoAbertaComSucesso();

        ResultActions response = whenAbrirSessaoComDuracaoValida();

        thenDeveRetornarSessaoCriada(response);
        thenServiceDeveSerChamado();
    }

    @Test
    void naoDeveAbrirSessaoComDuracaoInvalida() throws Exception {
        ResultActions response = whenAbrirSessaoComDuracaoInvalida();

        thenDeveRetornarDadosInvalidos(response);
        thenServiceNaoDeveSerChamado();
    }

    @Test
    void deveRetornarErroQuandoPautaNaoExistir() throws Exception {
        givenPautaNaoExistente();

        ResultActions response = whenAbrirSessaoComDuracaoValida();

        thenDeveRetornarPautaNaoEncontrada(response);
    }

    @Test
    void deveRetornarConflitoQuandoPautaJaPossuirSessao() throws Exception {
        givenPautaComSessaoExistente();

        ResultActions response = whenAbrirSessaoComDuracaoValida();

        thenDeveRetornarConflito(response);
    }


    private void givenSessaoAbertaComSucesso() {
        SessaoVotacaoResponse response = new SessaoVotacaoResponse(
                10L,
                PAUTA_ID,
                OffsetDateTime.parse("2026-09-28T10:00:00-03:00"),
                OffsetDateTime.parse("2026-09-28T10:05:00-03:00")
        );

        when(sessaoVotacaoService.abrir(
                eq(PAUTA_ID),
                any(AbrirSessaoRequest.class)
        )).thenReturn(response);
    }

    private void givenPautaNaoExistente() {
        when(sessaoVotacaoService.abrir(
                eq(PAUTA_ID),
                any(AbrirSessaoRequest.class)
        )).thenThrow(
                new RecursoNaoEncontradoException(
                        "Pauta não encontrada"
                )
        );
    }

    private void givenPautaComSessaoExistente() {
        when(sessaoVotacaoService.abrir(
                eq(PAUTA_ID),
                any(AbrirSessaoRequest.class)
        )).thenThrow(
                new ConflitoNegocioException(
                        "A pauta já possui uma sessão de votação"
                )
        );
    }


    private ResultActions whenAbrirSessaoComDuracaoValida()
            throws Exception {

        String requestBody = """
                {
                  "duracaoMinutos": 5
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas/{pautaId}/sessoes", PAUTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }

    private ResultActions whenAbrirSessaoComDuracaoInvalida()
            throws Exception {

        String requestBody = """
                {
                  "duracaoMinutos": 0
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas/{pautaId}/sessoes", PAUTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }


    private void thenDeveRetornarSessaoCriada(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/pautas/1/sessoes/10"
                ))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.inicio")
                        .value("2026-09-28T10:00:00-03:00"))
                .andExpect(jsonPath("$.fim")
                        .value("2026-09-28T10:05:00-03:00"));
    }

    private void thenDeveRetornarDadosInvalidos(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.duracaoMinutos")
                        .value("Duração deve ser maior que zero"));
    }

    private void thenDeveRetornarPautaNaoEncontrada(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Pauta não encontrada"));
    }

    private void thenDeveRetornarConflito(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("A pauta já possui uma sessão de votação"));
    }

    private void thenServiceDeveSerChamado() {
        verify(sessaoVotacaoService).abrir(
                eq(PAUTA_ID),
                any(AbrirSessaoRequest.class)
        );
    }

    private void thenServiceNaoDeveSerChamado() {
        verify(sessaoVotacaoService, never()).abrir(
                eq(PAUTA_ID),
                any(AbrirSessaoRequest.class)
        );
    }
}
