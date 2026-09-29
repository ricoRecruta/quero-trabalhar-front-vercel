package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class LocalidadePendenteRepositoryTest {

    @Autowired
    private LocalidadePendenteRepository localidadePendenteRepository;

    @Test
    void deveBuscarPendenciaMaisRecentePorTipoRecursoRecursoIdECampoAlvo() {
        LocalidadePendente pendenciaMaisAntiga = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario antigo",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                pendenciaMaisAntiga,
                LocalDateTime.of(2026, 5, 1, 10, 0, 0),
                LocalDateTime.of(2026, 5, 1, 10, 30, 0)
        );
        localidadePendenteRepository.saveAndFlush(pendenciaMaisAntiga);

        LocalidadePendente pendenciaMaisRecente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario atual",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                pendenciaMaisRecente,
                LocalDateTime.of(2026, 5, 2, 10, 0, 0),
                LocalDateTime.of(2026, 5, 2, 10, 30, 0)
        );
        pendenciaMaisRecente = localidadePendenteRepository.saveAndFlush(pendenciaMaisRecente);

        localidadePendenteRepository.saveAndFlush(
                LocalidadePendente.criarPendenteInformadaPeloUsuario(
                        "Serra do Sol",
                        "Aguardando validacao",
                        TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                        25L,
                        CampoLocalidadePendente.LOCALIDADE
                )
        );

        Optional<LocalidadePendente> resultado =
                localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                        TipoRecursoLocalidadePendente.EMPRESA,
                        10L,
                        CampoLocalidadePendente.LOCALIDADE
                );

        assertTrue(resultado.isPresent());
        assertEquals(pendenciaMaisRecente.getId(), resultado.get().getId());
        assertEquals("Vale Imaginario atual", resultado.get().getTextoOriginal());
    }

    @Test
    void deveManterCompatibilidadeNoMetodoLegadoDeBuscaPorDonoGenerico() {
        LocalidadePendente pendenciaMaisAntiga = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario antigo",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                pendenciaMaisAntiga,
                LocalDateTime.of(2026, 5, 1, 10, 0, 0),
                LocalDateTime.of(2026, 5, 1, 10, 30, 0)
        );
        localidadePendenteRepository.saveAndFlush(pendenciaMaisAntiga);

        LocalidadePendente pendenciaMaisRecente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario atual",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                pendenciaMaisRecente,
                LocalDateTime.of(2026, 5, 2, 10, 0, 0),
                LocalDateTime.of(2026, 5, 2, 10, 30, 0)
        );
        pendenciaMaisRecente = localidadePendenteRepository.saveAndFlush(pendenciaMaisRecente);

        Optional<LocalidadePendente> resultado =
                localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvo(
                        TipoRecursoLocalidadePendente.EMPRESA,
                        10L,
                        CampoLocalidadePendente.LOCALIDADE
                );

        assertTrue(resultado.isPresent());
        assertEquals(pendenciaMaisRecente.getId(), resultado.get().getId());
    }

    @Test
    void deveBuscarPendenciasEmLotePorTipoRecursoCampoAlvoERecursoIds() {
        LocalidadePendente empresaPrimeiraPendencia = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Empresa 10 antiga",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                empresaPrimeiraPendencia,
                LocalDateTime.of(2026, 5, 1, 8, 0, 0),
                LocalDateTime.of(2026, 5, 1, 8, 15, 0)
        );
        empresaPrimeiraPendencia = localidadePendenteRepository.saveAndFlush(empresaPrimeiraPendencia);

        LocalidadePendente empresaSegundaPendencia = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Empresa 10 atual",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                empresaSegundaPendencia,
                LocalDateTime.of(2026, 5, 1, 9, 0, 0),
                LocalDateTime.of(2026, 5, 1, 9, 30, 0)
        );
        empresaSegundaPendencia = localidadePendenteRepository.saveAndFlush(empresaSegundaPendencia);

        LocalidadePendente outraEmpresa = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Empresa 12",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                12L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                outraEmpresa,
                LocalDateTime.of(2026, 5, 1, 7, 0, 0),
                LocalDateTime.of(2026, 5, 1, 7, 30, 0)
        );
        outraEmpresa = localidadePendenteRepository.saveAndFlush(outraEmpresa);

        localidadePendenteRepository.saveAndFlush(
                LocalidadePendente.criarPendenteInformadaPeloUsuario(
                        "Empresa 20",
                        "Aguardando validacao",
                        TipoRecursoLocalidadePendente.EMPRESA,
                        20L,
                        CampoLocalidadePendente.LOCALIDADE
                )
        );
        localidadePendenteRepository.saveAndFlush(
                LocalidadePendente.criarPendenteInformadaPeloUsuario(
                        "Oportunidade 10",
                        "Aguardando validacao",
                        TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                        10L,
                        CampoLocalidadePendente.LOCALIDADE
                )
        );

        List<LocalidadePendente> resultado =
                localidadePendenteRepository.findByTipoRecursoAndCampoAlvoAndRecursoIdIn(
                        TipoRecursoLocalidadePendente.EMPRESA,
                        CampoLocalidadePendente.LOCALIDADE,
                        List.of(10L, 12L)
                );

        assertEquals(3, resultado.size());
        assertIterableEquals(
                List.of(
                        empresaSegundaPendencia.getId(),
                        empresaPrimeiraPendencia.getId(),
                        outraEmpresa.getId()
                ),
                resultado.stream().map(LocalidadePendente::getId).toList()
        );
    }

    @Test
    void deveBuscarPendenciasAbertasOrdenadasPorAtualizacaoECriacao() {
        LocalidadePendente pendenciaMaisRecente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Regiao recente",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                20L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                pendenciaMaisRecente,
                LocalDateTime.of(2026, 5, 3, 10, 0, 0),
                LocalDateTime.of(2026, 5, 3, 10, 30, 0)
        );
        pendenciaMaisRecente = localidadePendenteRepository.saveAndFlush(pendenciaMaisRecente);

        LocalidadePendente pendenciaMaisAntiga = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Regiao antiga",
                "Aguardando validacao",
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                30L,
                CampoLocalidadePendente.LOCALIDADE
        );
        definirAuditoria(
                pendenciaMaisAntiga,
                LocalDateTime.of(2026, 5, 1, 10, 0, 0),
                LocalDateTime.of(2026, 5, 1, 10, 15, 0)
        );
        pendenciaMaisAntiga = localidadePendenteRepository.saveAndFlush(pendenciaMaisAntiga);

        LocalidadePendente pendenciaFechada = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Regiao fechada",
                "Nao deve entrar no lote",
                TipoRecursoLocalidadePendente.EMPRESA,
                40L,
                CampoLocalidadePendente.LOCALIDADE
        );
        pendenciaFechada.setStatusValidacao(StatusValidacaoLocalidade.CONFIRMADA_PELO_USUARIO);
        localidadePendenteRepository.saveAndFlush(pendenciaFechada);

        List<LocalidadePendente> resultado = localidadePendenteRepository.findPendenciasAbertas();

        assertIterableEquals(
                List.of(pendenciaMaisAntiga.getId(), pendenciaMaisRecente.getId()),
                resultado.stream().map(LocalidadePendente::getId).toList()
        );
    }

    private static void definirAuditoria(
            LocalidadePendente localidadePendente,
            LocalDateTime criadaEm,
            LocalDateTime atualizadaEm
    ) {
        ReflectionTestUtils.setField(localidadePendente, "criadaEm", criadaEm);
        ReflectionTestUtils.setField(localidadePendente, "atualizadaEm", atualizadaEm);
    }
}
