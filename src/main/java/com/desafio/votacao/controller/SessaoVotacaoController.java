package com.desafio.votacao.controller;

import com.desafio.votacao.dto.sessao.AbrirSessaoRequest;
import com.desafio.votacao.dto.sessao.SessaoVotacaoResponse;
import com.desafio.votacao.service.SessaoVotacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/sessoes")
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoVotacaoService;

    public SessaoVotacaoController(
            SessaoVotacaoService sessaoVotacaoService
    ) {
        this.sessaoVotacaoService = sessaoVotacaoService;
    }

    @PostMapping
    public ResponseEntity<SessaoVotacaoResponse> abrir(
            @PathVariable Long pautaId,
            @Valid @RequestBody AbrirSessaoRequest request
    ) {
        var response = sessaoVotacaoService.abrir(pautaId, request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/pautas/%d/sessoes/%d"
                                        .formatted(pautaId, response.id())
                        )
                )
                .body(response);
    }
}
