package com.desafio.votacao.controller;

import com.desafio.votacao.dto.voto.RegistrarVotoRequest;
import com.desafio.votacao.dto.voto.ResultadoVotacaoResponse;
import com.desafio.votacao.dto.voto.VotoResponse;
import com.desafio.votacao.entity.TipoVoto;
import com.desafio.votacao.exception.ConflitoNegocioException;
import com.desafio.votacao.exception.RecursoNaoEncontradoException;
import com.desafio.votacao.service.VotoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
class VotoControllerTest {

    private static final Long PAUTA_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotoService votoService;

    @Test
    void deveRegistrarVotoComSucesso() throws Exception {
        givenVotoRegistradoComSucesso();

        ResultActions response = whenRegistrarVotoValido();

        thenDeveRetornarVotoCriado(response);
        thenRegistrarVotoDeveSerChamado();
    }

    @Test
    void naoDeveRegistrarVotoSemAssociado() throws Exception {
        ResultActions response = whenRegistrarVotoSemAssociado();

        thenDeveRetornarDadosInvalidos(response);
        thenRegistrarVotoNaoDeveSerChamado();
    }

    @Test
    void naoDeveRegistrarOpcaoDeVotoInvalida() throws Exception {
        ResultActions response = whenRegistrarOpcaoInvalida();

        thenDeveRetornarCorpoInvalido(response);
        thenRegistrarVotoNaoDeveSerChamado();
    }

    @Test
    void deveRetornarConflitoQuandoAssociadoJaVotou() throws Exception {
        givenAssociadoJaVotou();

        ResultActions response = whenRegistrarVotoValido();

        thenDeveRetornarConflito(response);
    }

    @Test
    void deveRetornarResultadoDaVotacao() throws Exception {
        givenResultadoDaVotacao();

        ResultActions response = whenConsultarResultado();

        thenDeveRetornarResultado(response);
    }

    @Test
    void deveRetornarErroAoConsultarResultadoDePautaInexistente()
            throws Exception {

        givenResultadoDePautaInexistente();

        ResultActions response = whenConsultarResultado();

        thenDeveRetornarPautaNaoEncontrada(response);
    }


    private void givenVotoRegistradoComSucesso() {
        VotoResponse response = new VotoResponse(
                10L,
                PAUTA_ID,
                "ASSOCIADO-001",
                TipoVoto.SIM,
                OffsetDateTime.parse("2026-09-28T10:00:00-03:00")
        );

        when(votoService.registrar(
                eq(PAUTA_ID),
                any(RegistrarVotoRequest.class)
        )).thenReturn(response);
    }

    private void givenAssociadoJaVotou() {
        when(votoService.registrar(
                eq(PAUTA_ID),
                any(RegistrarVotoRequest.class)
        )).thenThrow(
                new ConflitoNegocioException(
                        "Associado já votou nesta pauta"
                )
        );
    }

    private void givenResultadoDaVotacao() {
        ResultadoVotacaoResponse response =
                new ResultadoVotacaoResponse(
                        PAUTA_ID,
                        3L,
                        2L,
                        5L
                );

        when(votoService.resultado(PAUTA_ID))
                .thenReturn(response);
    }

    private void givenResultadoDePautaInexistente() {
        when(votoService.resultado(PAUTA_ID))
                .thenThrow(
                        new RecursoNaoEncontradoException(
                                "Pauta não encontrada"
                        )
                );
    }


    private ResultActions whenRegistrarVotoValido()
            throws Exception {

        String requestBody = """
                {
                  "associadoId": "ASSOCIADO-001",
                  "opcao": "SIM"
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas/{pautaId}/votos", PAUTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }

    private ResultActions whenRegistrarVotoSemAssociado()
            throws Exception {

        String requestBody = """
                {
                  "associadoId": "",
                  "opcao": "SIM"
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas/{pautaId}/votos", PAUTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }

    private ResultActions whenRegistrarOpcaoInvalida()
            throws Exception {

        String requestBody = """
                {
                  "associadoId": "ASSOCIADO-001",
                  "opcao": "TALVEZ"
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas/{pautaId}/votos", PAUTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }

    private ResultActions whenConsultarResultado()
            throws Exception {

        return mockMvc.perform(
                get("/api/v1/pautas/{pautaId}/votos/resultado", PAUTA_ID)
        );
    }


    private void thenDeveRetornarVotoCriado(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/pautas/1/votos/10"
                ))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.associadoId")
                        .value("ASSOCIADO-001"))
                .andExpect(jsonPath("$.opcao")
                        .value("SIM"));
    }

    private void thenDeveRetornarDadosInvalidos(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.associadoId")
                        .value("Identificador do associado é obrigatório"));
    }

    private void thenDeveRetornarCorpoInvalido(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Corpo da requisição inválido"));
    }

    private void thenDeveRetornarConflito(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Associado já votou nesta pauta"));
    }

    private void thenDeveRetornarResultado(
            ResultActions response
    ) throws Exception {

        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.sim").value(3))
                .andExpect(jsonPath("$.nao").value(2))
                .andExpect(jsonPath("$.total").value(5));
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

    private void thenRegistrarVotoDeveSerChamado() {
        verify(votoService).registrar(
                eq(PAUTA_ID),
                any(RegistrarVotoRequest.class)
        );
    }

    private void thenRegistrarVotoNaoDeveSerChamado() {
        verify(votoService, never()).registrar(
                eq(PAUTA_ID),
                any(RegistrarVotoRequest.class)
        );
    }

}