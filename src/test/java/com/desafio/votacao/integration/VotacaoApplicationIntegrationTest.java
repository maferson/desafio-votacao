package com.desafio.votacao.integration;

import com.desafio.votacao.entity.Pauta;
import com.desafio.votacao.entity.SessaoVotacao;
import com.desafio.votacao.entity.TipoVoto;
import com.desafio.votacao.entity.Voto;
import com.desafio.votacao.repository.PautaRepository;
import com.desafio.votacao.repository.SessaoVotacaoRepository;
import com.desafio.votacao.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class VotacaoApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @BeforeEach
    void cleanDatabase() {
        votoRepository.deleteAll();
        sessaoVotacaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    void deveSubirAplicacaoComPostgresEExecutarMigrations() {
        Long quantidadeMigrations = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE success = true
                """,
                Long.class
        );

        assertEquals(3L, quantidadeMigrations);
    }

    @Test
    void deveImpedirVotoDuplicadoNoBanco() {
        Pauta pauta = givenPautaPersistida();

        givenVotoPersistido(
                pauta,
                "ASSOCIADO-001",
                TipoVoto.SIM
        );

        Voto votoDuplicado = new Voto(
                pauta,
                "ASSOCIADO-001",
                TipoVoto.NAO
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> votoRepository.saveAndFlush(votoDuplicado)
        );
    }

    @Test
    void deveImpedirSegundaSessaoParaMesmaPautaNoBanco() {
        Pauta pauta = givenPautaPersistida();

        givenSessaoPersistida(pauta);

        SessaoVotacao segundaSessao = new SessaoVotacao(
                pauta,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(5)
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> sessaoVotacaoRepository.saveAndFlush(segundaSessao)
        );
    }

    private Pauta givenPautaPersistida() {
        Pauta pauta = new Pauta(
                "Pauta integração",
                "Teste com PostgreSQL"
        );

        return pautaRepository.saveAndFlush(pauta);
    }

    private void givenVotoPersistido(
            Pauta pauta,
            String associadoId,
            TipoVoto tipoVoto
    ) {
        Voto voto = new Voto(
                pauta,
                associadoId,
                tipoVoto
        );

        votoRepository.saveAndFlush(voto);
    }

    private void givenSessaoPersistida(Pauta pauta) {
        SessaoVotacao sessao = new SessaoVotacao(
                pauta,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(5)
        );

        sessaoVotacaoRepository.saveAndFlush(sessao);
    }

}
