package com.desafio.votacao.repository;

import com.desafio.votacao.entity.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {

    boolean existsByPauta_Id(Long pautaId);

    Optional<SessaoVotacao> findByPauta_Id(Long pautaId);
}
