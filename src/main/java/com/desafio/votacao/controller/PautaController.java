package com.desafio.votacao.controller;

import com.desafio.votacao.dto.pauta.CriarPautaRequest;
import com.desafio.votacao.dto.pauta.PautaResponse;
import com.desafio.votacao.service.PautaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    public ResponseEntity<PautaResponse> criar(
            @Valid @RequestBody CriarPautaRequest request
    ) {
        var response = pautaService.criar(request);

        return ResponseEntity
                .created(URI.create("/api/v1/pautas/" + response.id()))
                .body(response);
    }
}
