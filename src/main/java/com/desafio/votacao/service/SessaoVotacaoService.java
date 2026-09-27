package com.desafio.votacao.service;

import com.desafio.votacao.dto.sessao.AbrirSessaoRequest;
import com.desafio.votacao.dto.sessao.SessaoVotacaoResponse;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.exception.ConflitoNegocioException;
import com.desafio.votacao.exception.RecursoNaoEncontradoException;
import com.desafio.votacao.repository.PautaRepository;
import com.desafio.votacao.repository.SessaoVotacaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SessaoVotacaoService {

    private static final long DURACAO_PADRAO_MINUTOS = 1L;

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;

    public SessaoVotacaoService(
            PautaRepository pautaRepository,
            SessaoVotacaoRepository sessaoVotacaoRepository
    ) {
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
    }

    @Transactional
    public SessaoVotacaoResponse abrir(
            Long pautaId,
            AbrirSessaoRequest request
    ) {
        var pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Pauta não encontrada"
                        )
                );

        if (sessaoVotacaoRepository.existsByPauta_Id(pautaId)) {
            throw new ConflitoNegocioException(
                    "A pauta já possui uma sessão de votação"
            );
        }

        long duracaoMinutos = request.duracaoMinutos() != null
                ? request.duracaoMinutos()
                : DURACAO_PADRAO_MINUTOS;

        var inicio = OffsetDateTime.now();
        var fim = inicio.plusMinutes(duracaoMinutos);

        var sessao = new SessaoVotacao(
                pauta,
                inicio,
                fim
        );

        try {
            var sessaoSalva = sessaoVotacaoRepository.saveAndFlush(sessao);

            return new SessaoVotacaoResponse(
                    sessaoSalva.getId(),
                    pautaId,
                    sessaoSalva.getInicio(),
                    sessaoSalva.getFim()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoNegocioException(
                    "A pauta já possui uma sessão de votação"
            );
        }
    }
}
