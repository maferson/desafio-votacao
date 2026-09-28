package com.desafio.votacao.service;

import com.desafio.votacao.dto.voto.RegistrarVotoRequest;
import com.desafio.votacao.dto.voto.ResultadoVotacaoResponse;
import com.desafio.votacao.dto.voto.VotoResponse;
import com.desafio.votacao.entity.TipoVoto;
import com.desafio.votacao.entity.Voto;
import com.desafio.votacao.exception.ConflitoNegocioException;
import com.desafio.votacao.exception.RecursoNaoEncontradoException;
import com.desafio.votacao.repository.PautaRepository;
import com.desafio.votacao.repository.SessaoVotacaoRepository;
import com.desafio.votacao.repository.VotoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class VotoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final VotoRepository votoRepository;

    public VotoService(
            PautaRepository pautaRepository,
            SessaoVotacaoRepository sessaoVotacaoRepository,
            VotoRepository votoRepository
    ) {
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.votoRepository = votoRepository;
    }

    @Transactional
    public VotoResponse registrar(
            Long pautaId,
            RegistrarVotoRequest request
    ) {
        var sessao = sessaoVotacaoRepository.findByPauta_Id(pautaId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Sessão de votação não encontrada"
                        )
                );

        var agora = OffsetDateTime.now();

        if (!agora.isBefore(sessao.getFim())) {
            throw new ConflitoNegocioException(
                    "Sessão de votação encerrada"
            );
        }

        var associadoId = request.associadoId().trim();

        if (votoRepository.existsByPauta_IdAndAssociadoId(
                pautaId,
                associadoId
        )) {
            throw new ConflitoNegocioException(
                    "Associado já votou nesta pauta"
            );
        }

        var voto = new Voto(
                sessao.getPauta(),
                associadoId,
                request.opcao()
        );

        try {
            var votoSalvo = votoRepository.saveAndFlush(voto);

            return new VotoResponse(
                    votoSalvo.getId(),
                    pautaId,
                    votoSalvo.getAssociadoId(),
                    votoSalvo.getOpcao(),
                    votoSalvo.getCreatedAt()
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoNegocioException(
                    "Associado já votou nesta pauta"
            );
        }
    }

    @Transactional(readOnly = true)
    public ResultadoVotacaoResponse resultado(Long pautaId) {

        if (!pautaRepository.existsById(pautaId)) {
            throw new RecursoNaoEncontradoException(
                    "Pauta não encontrada"
            );
        }

        long votosSim = votoRepository.countByPauta_IdAndOpcao(
                pautaId,
                TipoVoto.SIM
        );

        long votosNao = votoRepository.countByPauta_IdAndOpcao(
                pautaId,
                TipoVoto.NAO
        );

        return new ResultadoVotacaoResponse(
                pautaId,
                votosSim,
                votosNao,
                votosSim + votosNao
        );
    }
}