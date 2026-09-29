package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.localidade.LocalidadeResolucaoService;
import com.QueroTrabalhar.services.localidade.RegistroLocalidadePendenteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OportunidadeEmpresaFeatureServiceTest {

    @Mock
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

    @Mock
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @Mock
    private PaisRepository paisRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private CidadeRepository cidadeRepository;

    @Mock
    private LocalidadePendenteRepository localidadePendenteRepository;

    @Mock
    private LocalidadeResolucaoService localidadeResolucaoService;

    @Mock
    private RegistroLocalidadePendenteService registroLocalidadePendenteService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private OportunidadeDeEmpregoService oportunidadeDeEmpregoService;

    @Test
    void deveCriarOportunidadePessoalQuandoPublicarComoEmpresaForNulo() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L, null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestDTO(null);

        prepararMocksBasicosComLocalidade(perfilRecrutador, tipoDeEmprego, pais);
        prepararMockSaveOportunidade();

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        ArgumentCaptor<OportunidadeDeEmprego> oportunidadeCaptor =
                ArgumentCaptor.forClass(OportunidadeDeEmprego.class);
        verify(oportunidadeDeEmpregoRepository).save(oportunidadeCaptor.capture());
        OportunidadeDeEmprego oportunidadeSalva = oportunidadeCaptor.getValue();

        assertNull(oportunidadeSalva.getEmpresa());
        assertNull(resposta.empresaId());
        assertEquals(1, perfilRecrutador.getOportunidadesPostadas().size());
    }

    @Test
    void deveCriarOportunidadePessoalQuandoPublicarComoEmpresaForFalse() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L, null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestDTO(false);

        prepararMocksBasicosComLocalidade(perfilRecrutador, tipoDeEmprego, pais);
        prepararMockSaveOportunidade();

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        ArgumentCaptor<OportunidadeDeEmprego> oportunidadeCaptor =
                ArgumentCaptor.forClass(OportunidadeDeEmprego.class);
        verify(oportunidadeDeEmpregoRepository).save(oportunidadeCaptor.capture());

        assertNull(oportunidadeCaptor.getValue().getEmpresa());
        assertNull(resposta.empresaId());
    }

    @Test
    void deveCriarOportunidadeEmNomeDaEmpresaQuandoPublicarComoEmpresaForTrueEVinculoAprovado() {
        Empresa empresa = criarEmpresa(20L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutador(10L, empresa, StatusVinculoEmpresa.APROVADO);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestDTO(true);

        prepararMocksBasicosComLocalidade(perfilRecrutador, tipoDeEmprego, pais);
        prepararMockSaveOportunidade();

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        ArgumentCaptor<OportunidadeDeEmprego> oportunidadeCaptor =
                ArgumentCaptor.forClass(OportunidadeDeEmprego.class);
        verify(oportunidadeDeEmpregoRepository).save(oportunidadeCaptor.capture());
        OportunidadeDeEmprego oportunidadeSalva = oportunidadeCaptor.getValue();

        assertSame(empresa, oportunidadeSalva.getEmpresa());
        assertEquals(empresa.getId(), resposta.empresaId());
        assertEquals("Empresa ACME", resposta.empresaNome());
    }

    @Test
    void deveBloquearPublicacaoComoEmpresaQuandoRecrutadorNaoPossuirEmpresaVinculada() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L, null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestDTO(true);

        prepararMocksBasicosSemLocalidade(perfilRecrutador, tipoDeEmprego);

        assertThrows(
                BusinessRuleException.class,
                () -> oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto)
        );

        verify(oportunidadeDeEmpregoRepository, never()).save(any(OportunidadeDeEmprego.class));
    }

    @Test
    void deveBloquearPublicacaoComoEmpresaQuandoVinculoForPendente() {
        deveBloquearPublicacaoComoEmpresaQuandoVinculoNaoEstiverAprovado(StatusVinculoEmpresa.PENDENTE);
    }

    @Test
    void deveBloquearPublicacaoComoEmpresaQuandoVinculoForRecusado() {
        deveBloquearPublicacaoComoEmpresaQuandoVinculoNaoEstiverAprovado(StatusVinculoEmpresa.RECUSADO);
    }

    @Test
    void deveBloquearPublicacaoComoEmpresaQuandoVinculoForRemovido() {
        deveBloquearPublicacaoComoEmpresaQuandoVinculoNaoEstiverAprovado(StatusVinculoEmpresa.REMOVIDO);
    }

    @Test
    void devePreservarEmpresaOriginalDaOportunidadeNoUpdate() {
        Empresa empresaOriginal = criarEmpresa(20L, "Empresa ACME");
        PerfilRecrutador perfilRecrutadorAutenticado =
                criarPerfilRecrutador(10L, empresaOriginal, StatusVinculoEmpresa.APROVADO);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        OportunidadeDeEmprego oportunidadeExistente = new OportunidadeDeEmprego(
                "Descricao antiga",
                tipoDeEmprego,
                Modalidade.REMOTO,
                new Localidade(pais),
                perfilRecrutadorAutenticado,
                empresaOriginal
        );
        ReflectionTestUtils.setField(oportunidadeExistente, "id", 100L);

        OportunidadeDeEmpregoRequestDTO dtoAtualizacao = new OportunidadeDeEmpregoRequestDTO(
                "Descricao atualizada",
                tipoDeEmprego.getId(),
                Modalidade.HIBRIDO,
                pais.getId(),
                null,
                null,
                null,
                false
        );

        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutadorAutenticado);
        when(oportunidadeDeEmpregoRepository.findById(100L)).thenReturn(Optional.of(oportunidadeExistente));
        when(tipoDeEmpregoRepository.findById(tipoDeEmprego.getId())).thenReturn(Optional.of(tipoDeEmprego));
        when(paisRepository.findById(pais.getId())).thenReturn(Optional.of(pais));
        when(oportunidadeDeEmpregoRepository.save(oportunidadeExistente)).thenReturn(oportunidadeExistente);

        OportunidadeDeEmpregoResponseDTO resposta =
                oportunidadeDeEmpregoService.atualizarOportunidadeDeEmprego(100L, dtoAtualizacao);

        assertSame(empresaOriginal, oportunidadeExistente.getEmpresa());
        assertEquals("Descricao atualizada", oportunidadeExistente.getDescricao());
        assertEquals(empresaOriginal.getId(), resposta.empresaId());
        verify(oportunidadeDeEmpregoRepository).save(oportunidadeExistente);
    }

    private void deveBloquearPublicacaoComoEmpresaQuandoVinculoNaoEstiverAprovado(
            StatusVinculoEmpresa statusVinculoEmpresa
    ) {
        Empresa empresa = criarEmpresa(20L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutador(10L, empresa, statusVinculoEmpresa);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestDTO(true);

        prepararMocksBasicosSemLocalidade(perfilRecrutador, tipoDeEmprego);

        assertThrows(
                BusinessRuleException.class,
                () -> oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto)
        );

        verify(oportunidadeDeEmpregoRepository, never()).save(any(OportunidadeDeEmprego.class));
    }

    private void prepararMocksBasicosSemLocalidade(
            PerfilRecrutador perfilRecrutador,
            TipoDeEmprego tipoDeEmprego
    ) {
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);
        when(tipoDeEmpregoRepository.findById(tipoDeEmprego.getId())).thenReturn(Optional.of(tipoDeEmprego));
    }

    private void prepararMocksBasicosComLocalidade(
            PerfilRecrutador perfilRecrutador,
            TipoDeEmprego tipoDeEmprego,
            Pais pais
    ) {
        prepararMocksBasicosSemLocalidade(perfilRecrutador, tipoDeEmprego);
        when(paisRepository.findById(pais.getId())).thenReturn(Optional.of(pais));
    }

    private void prepararMockSaveOportunidade() {
        when(oportunidadeDeEmpregoRepository.save(any(OportunidadeDeEmprego.class))).thenAnswer(invocation -> {
            OportunidadeDeEmprego oportunidade = invocation.getArgument(0);
            ReflectionTestUtils.setField(oportunidade, "id", 100L);
            return oportunidade;
        });
    }

    private OportunidadeDeEmpregoRequestDTO criarRequestDTO(Boolean publicarComoEmpresa) {
        return new OportunidadeDeEmpregoRequestDTO(
                "Vaga para backend Java",
                1L,
                Modalidade.REMOTO,
                1L,
                null,
                null,
                null,
                publicarComoEmpresa
        );
    }

    private PerfilRecrutador criarPerfilRecrutador(
            Long id,
            Empresa empresa,
            StatusVinculoEmpresa statusVinculoEmpresa
    ) {
        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa antiga");
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(statusVinculoEmpresa);
        return perfilRecrutador;
    }

    private Empresa criarEmpresa(Long id, String nome) {
        Empresa empresa = new Empresa(nome, null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }

    private TipoDeEmprego criarTipoDeEmpregoAprovado(Long id) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(
                "Desenvolvedor Backend",
                "Descricao"
        );
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }

    private Pais criarPais(Long id) {
        Pais pais = new Pais("Brasil", "BR");
        ReflectionTestUtils.setField(pais, "id", id);
        return pais;
    }
}
