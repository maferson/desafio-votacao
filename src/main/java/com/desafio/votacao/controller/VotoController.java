package com.desafio.votacao.controller;

import com.desafio.votacao.dto.voto.RegistrarVotoRequest;
import com.desafio.votacao.dto.voto.VotoResponse;
import com.desafio.votacao.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    public ResponseEntity<VotoResponse> registrar(
            @PathVariable Long pautaId,
            @Valid @RequestBody RegistrarVotoRequest request
    ) {
        var response = votoService.registrar(pautaId, request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/pautas/%d/votos/%d"
                                        .formatted(pautaId, response.id())
                        )
                )
                .body(response);
    }
}
