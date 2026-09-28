package com.desafio.votacao.controller;

import com.desafio.votacao.dto.pauta.CriarPautaRequest;
import com.desafio.votacao.dto.pauta.PautaResponse;
import com.desafio.votacao.service.PautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    @Test
    void deveCriarPautaComSucesso() throws Exception {
        givenPautaCriadaComSucesso();

        ResultActions response = whenCriarPautaComDadosValidos();

        thenDeveRetornarPautaCriada(response);
        thenServiceDeveSerChamado();
    }

    @Test
    void naoDeveCriarPautaSemTitulo() throws Exception {
        ResultActions response = whenCriarPautaSemTitulo();

        thenDeveRetornarDadosInvalidos(response);
        thenServiceNaoDeveSerChamado();
    }


    private void givenPautaCriadaComSucesso() {
        PautaResponse response = new PautaResponse(
                1L,
                "Pauta teste",
                "Descrição da pauta",
                OffsetDateTime.parse("2026-09-28T10:00:00-03:00")
        );

        when(pautaService.criar(any(CriarPautaRequest.class)))
                .thenReturn(response);
    }


    private ResultActions whenCriarPautaComDadosValidos() throws Exception {
        String requestBody = """
                {
                  "titulo": "Pauta teste",
                  "descricao": "Descrição da pauta"
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }

    private ResultActions whenCriarPautaSemTitulo() throws Exception {
        String requestBody = """
                {
                  "titulo": "",
                  "descricao": "Descrição da pauta"
                }
                """;

        return mockMvc.perform(
                post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );
    }


    private void thenDeveRetornarPautaCriada(
            ResultActions response
    ) throws Exception {
        response
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/pautas/1"
                ))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Pauta teste"))
                .andExpect(jsonPath("$.descricao")
                        .value("Descrição da pauta"));
    }

    private void thenDeveRetornarDadosInvalidos(
            ResultActions response
    ) throws Exception {
        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.titulo")
                        .value("Título é obrigatório"));
    }

    private void thenServiceDeveSerChamado() {
        verify(pautaService)
                .criar(any(CriarPautaRequest.class));
    }

    private void thenServiceNaoDeveSerChamado() {
        verify(pautaService, never())
                .criar(any(CriarPautaRequest.class));
    }
}
