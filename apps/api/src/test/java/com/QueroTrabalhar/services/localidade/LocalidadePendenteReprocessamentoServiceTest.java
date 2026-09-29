package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalidadePendenteReprocessamentoServiceTest {

    @Mock
    private LocalidadePendenteRepository localidadePendenteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

    @Mock
    private FluxoResolucaoLocalidadeService fluxoResolucaoLocalidadeService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private LocalidadePendenteReprocessamentoService localidadePendenteReprocessamentoService;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void deveReprocessarPendenciaDeEmpresaEAplicarLocalidadeValidada() {
        LocalidadePendente pendencia = criarPendencia(
                1L,
                "Joao Pessoa",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L
        );
        Empresa empresa = new Empresa("Empresa A", null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", 10L);
        Localidade localidadeValidada = criarLocalidade(1L, 11L, 111L);

        when(localidadePendenteRepository.findById(1L)).thenReturn(Optional.of(pendencia));
        when(empresaRepository.findById(10L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(empresa)).thenReturn(empresa);
        when(fluxoResolucaoLocalidadeService.resolver("Joao Pessoa")).thenReturn(
                FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade.resolvida(
                        "Joao Pessoa",
                        "abc123",
                        localidadeValidada,
                        FluxoResolucaoLocalidadeService.OrigemResolucaoLocalidade.BASE_INTERNA,
                        0
                )
        );

        LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia resultado =
                localidadePendenteReprocessamentoService.reprocessarPendencia(1L);

        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.RESOLVIDA,
                resultado.status()
        );
        assertSame(localidadeValidada, empresa.getLocalidade());
        verify(empresaRepository).save(empresa);
        verify(localidadePendenteRepository).delete(pendencia);
        verify(localidadePendenteRepository, never()).save(any(LocalidadePendente.class));
    }

    @Test
    void deveReprocessarPendenciaDeOportunidadeEAplicarLocalidadeValidada() {
        LocalidadePendente pendencia = criarPendencia(
                2L,
                "Recife",
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                20L
        );
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin("Backend", "Descricao");
        ReflectionTestUtils.setField(tipoDeEmprego, "id", 5L);
        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa antiga");
        ReflectionTestUtils.setField(perfilRecrutador, "id", 7L);
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                "Oportunidade",
                tipoDeEmprego,
                Modalidade.REMOTO,
                null,
                perfilRecrutador
        );
        ReflectionTestUtils.setField(oportunidade, "id", 20L);
        Localidade localidadeValidada = criarLocalidade(2L, 12L, 112L);

        when(localidadePendenteRepository.findById(2L)).thenReturn(Optional.of(pendencia));
        when(oportunidadeDeEmpregoRepository.findById(20L)).thenReturn(Optional.of(oportunidade));
        when(oportunidadeDeEmpregoRepository.save(oportunidade)).thenReturn(oportunidade);
        when(fluxoResolucaoLocalidadeService.resolver("Recife")).thenReturn(
                FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade.resolvida(
                        "Recife",
                        "def456",
                        localidadeValidada,
                        FluxoResolucaoLocalidadeService.OrigemResolucaoLocalidade.GOOGLE_MAPS,
                        2
                )
        );

        LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia resultado =
                localidadePendenteReprocessamentoService.reprocessarPendencia(2L);

        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.RESOLVIDA,
                resultado.status()
        );
        assertSame(localidadeValidada, oportunidade.getLocalizacao());
        verify(oportunidadeDeEmpregoRepository).save(oportunidade);
        verify(localidadePendenteRepository).delete(pendencia);
    }

    @Test
    void deveManterMesmaPendenciaQuandoContinuarAmbigua() {
        LocalidadePendente pendencia = criarPendencia(
                3L,
                "Springfield",
                TipoRecursoLocalidadePendente.EMPRESA,
                30L
        );
        Empresa empresa = new Empresa("Empresa B", null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", 30L);

        when(localidadePendenteRepository.findById(3L)).thenReturn(Optional.of(pendencia));
        when(empresaRepository.findById(30L)).thenReturn(Optional.of(empresa));
        when(localidadePendenteRepository.save(pendencia)).thenReturn(pendencia);
        when(fluxoResolucaoLocalidadeService.resolver("Springfield")).thenReturn(
                FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade.pendente(
                        "Springfield",
                        "ghi789",
                        MotivoPendenciaLocalidade.LOCALIDADE_AMBIGUA_GOOGLE,
                        1
                )
        );

        LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia resultado =
                localidadePendenteReprocessamentoService.reprocessarPendencia(3L);

        ArgumentCaptor<LocalidadePendente> pendenciaCaptor = ArgumentCaptor.forClass(LocalidadePendente.class);
        verify(localidadePendenteRepository).save(pendenciaCaptor.capture());

        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.MANTIDA,
                resultado.status()
        );
        assertSame(pendencia, pendenciaCaptor.getValue());
        assertEquals(
                MotivoPendenciaLocalidade.LOCALIDADE_AMBIGUA_GOOGLE.descricao(),
                pendencia.getMotivoPendencia()
        );
        verify(localidadePendenteRepository, never()).delete(any(LocalidadePendente.class));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveManterMesmaPendenciaQuandoGoogleContinuarFalhando() {
        LocalidadePendente pendencia = criarPendencia(
                4L,
                "Nova Esperanca",
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                40L
        );
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin("Dados", "Descricao");
        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa antiga");
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                "Oportunidade",
                tipoDeEmprego,
                Modalidade.HIBRIDO,
                null,
                perfilRecrutador
        );
        ReflectionTestUtils.setField(oportunidade, "id", 40L);

        when(localidadePendenteRepository.findById(4L)).thenReturn(Optional.of(pendencia));
        when(oportunidadeDeEmpregoRepository.findById(40L)).thenReturn(Optional.of(oportunidade));
        when(localidadePendenteRepository.save(pendencia)).thenReturn(pendencia);
        when(fluxoResolucaoLocalidadeService.resolver("Nova Esperanca")).thenReturn(
                FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade.pendente(
                        "Nova Esperanca",
                        "jkl012",
                        MotivoPendenciaLocalidade.FALHA_INTEGRACAO_EXTERNA,
                        2
                )
        );

        LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia resultado =
                localidadePendenteReprocessamentoService.reprocessarPendencia(4L);

        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.MANTIDA,
                resultado.status()
        );
        assertEquals(
                MotivoPendenciaLocalidade.FALHA_INTEGRACAO_EXTERNA.descricao(),
                pendencia.getMotivoPendencia()
        );
        verify(localidadePendenteRepository).save(pendencia);
        verify(localidadePendenteRepository, never()).delete(any(LocalidadePendente.class));
    }

    @Test
    void deveManterLoteQuandoPendenciaSemDonoNaoEncontrarRecurso() {
        LocalidadePendente pendenciaOrfa = criarPendencia(
                5L,
                "Vale Orfao",
                TipoRecursoLocalidadePendente.EMPRESA,
                50L
        );
        LocalidadePendente pendenciaValida = criarPendencia(
                6L,
                "Recife",
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                60L
        );
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin("Backend", "Descricao");
        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa antiga");
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                "Oportunidade",
                tipoDeEmprego,
                Modalidade.REMOTO,
                null,
                perfilRecrutador
        );
        ReflectionTestUtils.setField(oportunidade, "id", 60L);
        Localidade localidadeValidada = criarLocalidade(3L, 13L, 113L);

        when(localidadePendenteRepository.findPendenciasAbertas()).thenReturn(List.of(pendenciaOrfa, pendenciaValida));
        when(localidadePendenteRepository.findById(5L)).thenReturn(Optional.of(pendenciaOrfa));
        when(localidadePendenteRepository.findById(6L)).thenReturn(Optional.of(pendenciaValida));
        when(empresaRepository.findById(50L)).thenReturn(Optional.empty());
        when(oportunidadeDeEmpregoRepository.findById(60L)).thenReturn(Optional.of(oportunidade));
        when(oportunidadeDeEmpregoRepository.save(oportunidade)).thenReturn(oportunidade);
        when(fluxoResolucaoLocalidadeService.resolver("Recife")).thenReturn(
                FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade.resolvida(
                        "Recife",
                        "mno345",
                        localidadeValidada,
                        FluxoResolucaoLocalidadeService.OrigemResolucaoLocalidade.GOOGLE_MAPS,
                        1
                )
        );

        List<LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia> resultados =
                localidadePendenteReprocessamentoService.reprocessarPendenciasAbertas();

        assertEquals(2, resultados.size());
        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.SEM_DONO,
                resultados.get(0).status()
        );
        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.RESOLVIDA,
                resultados.get(1).status()
        );
        verify(fluxoResolucaoLocalidadeService, never()).resolver("Vale Orfao");
        verify(fluxoResolucaoLocalidadeService).resolver("Recife");
        verify(localidadePendenteRepository).delete(pendenciaValida);
    }

    @Test
    void deveTratarTipoRecursoNaoSuportadoSemQuebrarOLote() {
        LocalidadePendente pendenciaTipoInvalido = mock(LocalidadePendente.class);
        when(pendenciaTipoInvalido.getId()).thenReturn(7L);
        when(pendenciaTipoInvalido.possuiDonoGenerico()).thenReturn(true);
        when(pendenciaTipoInvalido.getTipoRecurso()).thenReturn(null);
        when(pendenciaTipoInvalido.getRecursoId()).thenReturn(70L);
        when(pendenciaTipoInvalido.getCampoAlvo()).thenReturn(CampoLocalidadePendente.LOCALIDADE);

        LocalidadePendente pendenciaValida = criarPendencia(
                8L,
                "Joao Pessoa",
                TipoRecursoLocalidadePendente.EMPRESA,
                80L
        );
        Empresa empresa = new Empresa("Empresa C", null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", 80L);
        Localidade localidadeValidada = criarLocalidade(4L, 14L, 114L);

        when(localidadePendenteRepository.findPendenciasAbertas()).thenReturn(List.of(pendenciaTipoInvalido, pendenciaValida));
        when(localidadePendenteRepository.findById(7L)).thenReturn(Optional.of(pendenciaTipoInvalido));
        when(localidadePendenteRepository.findById(8L)).thenReturn(Optional.of(pendenciaValida));
        when(empresaRepository.findById(80L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(empresa)).thenReturn(empresa);
        when(fluxoResolucaoLocalidadeService.resolver("Joao Pessoa")).thenReturn(
                FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade.resolvida(
                        "Joao Pessoa",
                        "pqr678",
                        localidadeValidada,
                        FluxoResolucaoLocalidadeService.OrigemResolucaoLocalidade.BASE_INTERNA,
                        0
                )
        );

        List<LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia> resultados =
                localidadePendenteReprocessamentoService.reprocessarPendenciasAbertas();

        assertEquals(2, resultados.size());
        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.TIPO_RECURSO_NAO_SUPORTADO,
                resultados.get(0).status()
        );
        assertEquals(
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.RESOLVIDA,
                resultados.get(1).status()
        );
        verify(fluxoResolucaoLocalidadeService, never()).resolver("Tipo invalido");
        verify(fluxoResolucaoLocalidadeService).resolver("Joao Pessoa");
        verify(localidadePendenteRepository).delete(pendenciaValida);
    }

    private LocalidadePendente criarPendencia(
            Long id,
            String textoOriginal,
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId
    ) {
        LocalidadePendente pendencia = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                textoOriginal,
                "Motivo inicial",
                tipoRecurso,
                recursoId,
                CampoLocalidadePendente.LOCALIDADE
        );
        ReflectionTestUtils.setField(pendencia, "id", id);
        return pendencia;
    }

    private Localidade criarLocalidade(Long paisId, Long estadoId, Long cidadeId) {
        Pais pais = new Pais("Brasil", "BR");
        ReflectionTestUtils.setField(pais, "id", paisId);
        Estado estado = new Estado("Estado " + estadoId, "E" + estadoId, pais);
        ReflectionTestUtils.setField(estado, "id", estadoId);
        Cidade cidade = new Cidade("Cidade " + cidadeId, estado);
        ReflectionTestUtils.setField(cidade, "id", cidadeId);
        return new Localidade(pais, estado, cidade);
    }
}
