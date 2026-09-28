package com.desafio.votacao.service;

import com.desafio.votacao.client.AssociadoClient;
import com.desafio.votacao.client.StatusVotoAssociado;
import com.desafio.votacao.dto.voto.RegistrarVotoRequest;
import com.desafio.votacao.dto.voto.ResultadoVotacaoResponse;
import com.desafio.votacao.dto.voto.VotoResponse;
import com.desafio.votacao.entity.SessaoVotacao;
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
    private final AssociadoClient associadoClient;

    public VotoService(
            PautaRepository pautaRepository,
            SessaoVotacaoRepository sessaoVotacaoRepository,
            VotoRepository votoRepository,
            AssociadoClient associadoClient
    ) {
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.votoRepository = votoRepository;
        this.associadoClient = associadoClient;
    }

    @Transactional
    public VotoResponse registrar(
            Long pautaId,
            RegistrarVotoRequest request
    ) {
        SessaoVotacao sessao = sessaoVotacaoRepository
                .findByPauta_Id(pautaId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Sessão de votação não encontrada"
                        )
                );

        OffsetDateTime agora = OffsetDateTime.now();

        if (!agora.isBefore(sessao.getFim())) {
            throw new ConflitoNegocioException(
                    "Sessão de votação encerrada"
            );
        }

        String associadoId = request.associadoId().trim();

        if (votoRepository.existsByPauta_IdAndAssociadoId(
                pautaId,
                associadoId
        )) {
            throw new ConflitoNegocioException(
                    "Associado já votou nesta pauta"
            );
        }

        StatusVotoAssociado status =
                associadoClient.consultarSituacao(associadoId);

        if (status != StatusVotoAssociado.ABLE_TO_VOTE) {
            throw new RecursoNaoEncontradoException(
                    "Associado não está apto a votar"
            );
        }

        Voto voto = new Voto(
                sessao.getPauta(),
                associadoId,
                request.opcao()
        );

        try {
            Voto votoSalvo = votoRepository.saveAndFlush(voto);

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

        long sim = votoRepository.countByPauta_IdAndOpcao(
                pautaId,
                TipoVoto.SIM
        );

        long nao = votoRepository.countByPauta_IdAndOpcao(
                pautaId,
                TipoVoto.NAO
        );

        return new ResultadoVotacaoResponse(
                pautaId,
                sim,
                nao,
                sim + nao
        );
    }
}