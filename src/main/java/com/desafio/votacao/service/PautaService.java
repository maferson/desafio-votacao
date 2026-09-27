package com.desafio.votacao.service;

import com.desafio.votacao.dto.pauta.CriarPautaRequest;
import com.desafio.votacao.dto.pauta.PautaResponse;
import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.repository.PautaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    @Transactional
    public PautaResponse criar(CriarPautaRequest request) {
        var pauta = new Pauta(
                request.titulo(),
                request.descricao()
        );

        var pautaSalva = pautaRepository.save(pauta);

        return new PautaResponse(
                pautaSalva.getId(),
                pautaSalva.getTitulo(),
                pautaSalva.getDescricao(),
                pautaSalva.getCreatedAt()
        );
    }
}
