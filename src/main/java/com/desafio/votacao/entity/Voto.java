package com.desafio.votacao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "voto")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false, length = 50)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private TipoVoto opcao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Voto(
            Pauta pauta,
            String associadoId,
            TipoVoto opcao
    ) {
        this.pauta = pauta;
        this.associadoId = associadoId;
        this.opcao = opcao;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
