package com.desafio.votacao.repository;

import com.desafio.votacao.entity.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPauta_IdAndAssociadoId(
            Long pautaId,
            String associadoId
    );
    
}
