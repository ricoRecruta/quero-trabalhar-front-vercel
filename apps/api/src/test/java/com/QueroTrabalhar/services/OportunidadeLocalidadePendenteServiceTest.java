package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.localidade.LocalidadeResolucaoService;
import com.QueroTrabalhar.services.localidade.RegistroLocalidadePendenteService;
import com.QueroTrabalhar.services.localidade.ResultadoResolucaoLocalidade;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OportunidadeLocalidadePendenteServiceTest {

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
    void deveCriarOportunidadeComLocalidadePorIds() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L, "Camila Souza", null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        Estado estado = criarEstado(11L, pais);
        Cidade cidade = criarCidade(111L, estado);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestPorIds(pais.getId(), estado.getId(), cidade.getId());

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(paisRepository.findById(pais.getId())).thenReturn(Optional.of(pais));
        when(estadoRepository.findById(estado.getId())).thenReturn(Optional.of(estado));
        when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.of(cidade));
        prepararMockSaveOportunidade();

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        OportunidadeDeEmprego oportunidadeSalva = capturarOportunidadeSalva();
        assertSame(pais, oportunidadeSalva.getLocalizacao().getPais());
        assertSame(estado, oportunidadeSalva.getLocalizacao().getEstado());
        assertSame(cidade, oportunidadeSalva.getLocalizacao().getCidade());
        assertEquals("VALIDADA", resposta.statusLocalidade());
        verifyNoInteractions(localidadeResolucaoService, registroLocalidadePendenteService);
    }

    @Test
    void deveCriarOportunidadeComLocalidadeTextoResolvida() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(20L, "Mariana Alves", null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        Estado estado = criarEstado(12L, pais);
        Cidade cidade = criarCidade(112L, estado);
        Localidade localidadeResolvida = new Localidade(pais, estado, cidade);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestPorTexto("  Joao Pessoa  ", false);

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(localidadeResolucaoService.resolver("Joao Pessoa"))
                .thenReturn(ResultadoResolucaoLocalidade.resolvida(localidadeResolvida));
        prepararMockSaveOportunidade();

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        OportunidadeDeEmprego oportunidadeSalva = capturarOportunidadeSalva();
        assertSame(localidadeResolvida, oportunidadeSalva.getLocalizacao());
        assertEquals("VALIDADA", resposta.statusLocalidade());
        verify(localidadeResolucaoService).resolver("Joao Pessoa");
        verifyNoInteractions(paisRepository, estadoRepository, cidadeRepository, registroLocalidadePendenteService);
    }

    @Test
    void deveCriarOportunidadeComLocalidadeTextoPendente() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(30L, "Pedro Nogueira", null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        LocalidadePendente localidadePendente = criarLocalidadePendente(
                "Sao Tome das Letras",
                "Google Maps indisponivel no momento."
        );
        OportunidadeDeEmpregoRequestDTO dto = criarRequestPorTexto("  Sao Tome das Letras  ", false);

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(localidadeResolucaoService.resolver("Sao Tome das Letras"))
                .thenReturn(ResultadoResolucaoLocalidade.pendente(localidadePendente));
        prepararMockSaveOportunidade();
        mockAssociarDonoGenerico();
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                999L,
                CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.of(localidadePendente));

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        OportunidadeDeEmprego oportunidadeSalva = capturarOportunidadeSalva();
        assertNull(oportunidadeSalva.getLocalizacao());
        assertEquals("PENDENTE", resposta.statusLocalidade());
        assertEquals("Sao Tome das Letras", resposta.localidadeTextoOriginal());
        assertEquals(StatusValidacaoLocalidade.PENDENTE_VALIDACAO, resposta.statusValidacaoLocalidade());
        assertEquals("Google Maps indisponivel no momento.", resposta.motivoPendenciaLocalidade());
        assertEquals(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                localidadePendente.getTipoRecurso()
        );
        assertEquals(999L, localidadePendente.getRecursoId());
        assertEquals(CampoLocalidadePendente.LOCALIDADE, localidadePendente.getCampoAlvo());
        verify(localidadeResolucaoService).resolver("Sao Tome das Letras");
        verify(registroLocalidadePendenteService).associarDonoGenerico(
                localidadePendente,
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                999L,
                CampoLocalidadePendente.LOCALIDADE
        );
        verifyNoInteractions(paisRepository, estadoRepository, cidadeRepository);
    }

    @Test
    void deveBloquearCriacaoQuandoLocalidadeNaoForInformada() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(40L, "Ana Beatriz", null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestPorTexto("   ", false);

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);

        assertThrows(
                BusinessRuleException.class,
                () -> oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto)
        );

        verify(oportunidadeDeEmpregoRepository, never()).save(any(OportunidadeDeEmprego.class));
        verifyNoInteractions(localidadeResolucaoService, paisRepository, estadoRepository, cidadeRepository, registroLocalidadePendenteService);
    }

    @Test
    void devePriorizarIdsQuandoIdsELocalidadeTextoForemInformados() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(50L, "Lucas Martins", null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais pais = criarPais(1L);
        OportunidadeDeEmpregoRequestDTO dto = new OportunidadeDeEmpregoRequestDTO(
                "Vaga backend com localidade mista",
                tipoDeEmprego.getId(),
                Modalidade.HIBRIDO,
                pais.getId(),
                null,
                null,
                "Curitiba, PR",
                false
        );

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(paisRepository.findById(pais.getId())).thenReturn(Optional.of(pais));
        prepararMockSaveOportunidade();

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        OportunidadeDeEmprego oportunidadeSalva = capturarOportunidadeSalva();
        assertSame(pais, oportunidadeSalva.getLocalizacao().getPais());
        assertNull(oportunidadeSalva.getLocalizacao().getEstado());
        assertNull(oportunidadeSalva.getLocalizacao().getCidade());
        assertEquals("VALIDADA", resposta.statusLocalidade());
        verifyNoInteractions(localidadeResolucaoService, registroLocalidadePendenteService);
    }

    @Test
    void deveBloquearCidadeSemEstadoNoFluxoPorIds() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(60L, "Bruna Lima", null, null);
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        OportunidadeDeEmpregoRequestDTO dto = criarRequestPorIds(1L, null, 99L);

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);

        assertThrows(
                BusinessRuleException.class,
                () -> oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto)
        );

        verify(oportunidadeDeEmpregoRepository, never()).save(any(OportunidadeDeEmprego.class));
        verifyNoInteractions(localidadeResolucaoService, paisRepository, estadoRepository, cidadeRepository, registroLocalidadePendenteService);
    }

    @Test
    void deveAtualizarOportunidadeParaLocalidadeTextoResolvida() {
        Empresa empresaOriginal = criarEmpresa(70L, "Plataforma Agil");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(
                70L,
                "Fernanda Rocha",
                empresaOriginal,
                StatusVinculoEmpresa.APROVADO
        );
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais paisAntigo = criarPais(1L);
        Estado estadoAntigo = criarEstado(13L, paisAntigo);
        Cidade cidadeAntiga = criarCidade(113L, estadoAntigo);
        OportunidadeDeEmprego oportunidadeExistente = new OportunidadeDeEmprego(
                "Descricao antiga",
                tipoDeEmprego,
                Modalidade.REMOTO,
                new Localidade(paisAntigo, estadoAntigo, cidadeAntiga),
                perfilRecrutador,
                empresaOriginal
        );
        ReflectionTestUtils.setField(oportunidadeExistente, "id", 700L);

        Pais paisNovo = criarPais(2L);
        Estado estadoNovo = criarEstado(14L, paisNovo);
        Cidade cidadeNova = criarCidade(114L, estadoNovo);
        Localidade localidadeNova = new Localidade(paisNovo, estadoNovo, cidadeNova);
        OportunidadeDeEmpregoRequestDTO dto = new OportunidadeDeEmpregoRequestDTO(
                "Descricao atualizada",
                tipoDeEmprego.getId(),
                Modalidade.HIBRIDO,
                null,
                null,
                null,
                "  Belo Horizonte  ",
                false
        );

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(oportunidadeDeEmpregoRepository.findById(700L)).thenReturn(Optional.of(oportunidadeExistente));
        when(localidadeResolucaoService.resolver("Belo Horizonte"))
                .thenReturn(ResultadoResolucaoLocalidade.resolvida(localidadeNova));
        when(oportunidadeDeEmpregoRepository.save(oportunidadeExistente)).thenReturn(oportunidadeExistente);

        OportunidadeDeEmpregoResponseDTO resposta =
                oportunidadeDeEmpregoService.atualizarOportunidadeDeEmprego(700L, dto);

        assertSame(localidadeNova, oportunidadeExistente.getLocalizacao());
        assertSame(empresaOriginal, oportunidadeExistente.getEmpresa());
        assertEquals("VALIDADA", resposta.statusLocalidade());
        assertEquals(empresaOriginal.getId(), resposta.empresaId());
        verify(localidadeResolucaoService).resolver("Belo Horizonte");
        verifyNoInteractions(paisRepository, estadoRepository, cidadeRepository, registroLocalidadePendenteService);
    }

    @Test
    void deveAtualizarOportunidadeParaLocalidadeTextoPendente() {
        Empresa empresaOriginal = criarEmpresa(80L, "Nucleo Digital");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(
                80L,
                "Rafael Sousa",
                empresaOriginal,
                StatusVinculoEmpresa.APROVADO
        );
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        Pais paisAntigo = criarPais(1L);
        Estado estadoAntigo = criarEstado(15L, paisAntigo);
        Cidade cidadeAntiga = criarCidade(115L, estadoAntigo);
        OportunidadeDeEmprego oportunidadeExistente = new OportunidadeDeEmprego(
                "Descricao antiga",
                tipoDeEmprego,
                Modalidade.PRESENCIAL,
                new Localidade(paisAntigo, estadoAntigo, cidadeAntiga),
                perfilRecrutador,
                empresaOriginal
        );
        ReflectionTestUtils.setField(oportunidadeExistente, "id", 800L);

        LocalidadePendente localidadePendente = criarLocalidadePendente(
                "Vale do Silicio Paraibano",
                "Localidade não encontrada na base estruturada."
        );
        OportunidadeDeEmpregoRequestDTO dto = new OportunidadeDeEmpregoRequestDTO(
                "Descricao atualizada",
                tipoDeEmprego.getId(),
                Modalidade.HIBRIDO,
                null,
                null,
                null,
                "  Vale do Silicio Paraibano  ",
                false
        );

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(oportunidadeDeEmpregoRepository.findById(800L)).thenReturn(Optional.of(oportunidadeExistente));
        when(localidadeResolucaoService.resolver("Vale do Silicio Paraibano"))
                .thenReturn(ResultadoResolucaoLocalidade.pendente(localidadePendente));
        when(oportunidadeDeEmpregoRepository.save(oportunidadeExistente)).thenReturn(oportunidadeExistente);
        mockAssociarDonoGenerico();
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                800L,
                CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.of(localidadePendente));

        OportunidadeDeEmpregoResponseDTO resposta =
                oportunidadeDeEmpregoService.atualizarOportunidadeDeEmprego(800L, dto);

        assertNull(oportunidadeExistente.getLocalizacao());
        assertSame(empresaOriginal, oportunidadeExistente.getEmpresa());
        assertEquals("PENDENTE", resposta.statusLocalidade());
        assertEquals("Vale do Silicio Paraibano", resposta.localidadeTextoOriginal());
        assertEquals(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                localidadePendente.getTipoRecurso()
        );
        assertEquals(800L, localidadePendente.getRecursoId());
        assertEquals(CampoLocalidadePendente.LOCALIDADE, localidadePendente.getCampoAlvo());
        verify(localidadeResolucaoService).resolver("Vale do Silicio Paraibano");
        verify(registroLocalidadePendenteService).associarDonoGenerico(
                localidadePendente,
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                800L,
                CampoLocalidadePendente.LOCALIDADE
        );
        verifyNoInteractions(paisRepository, estadoRepository, cidadeRepository);
    }

    @Test
    void devePreservarPublicacaoComoEmpresaAoCriarComLocalidadeTextoPendente() {
        Empresa empresa = criarEmpresa(90L, "Inovacao Nordeste");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(
                90L,
                "Juliana Moura",
                empresa,
                StatusVinculoEmpresa.APROVADO
        );
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(1L);
        LocalidadePendente localidadePendente = criarLocalidadePendente(
                "Serra do Sol Tech",
                "Google Maps nao respondeu."
        );
        OportunidadeDeEmpregoRequestDTO dto = criarRequestPorTexto("Serra do Sol Tech", true);

        prepararMocksBasicos(perfilRecrutador, tipoDeEmprego);
        when(localidadeResolucaoService.resolver("Serra do Sol Tech"))
                .thenReturn(ResultadoResolucaoLocalidade.pendente(localidadePendente));
        prepararMockSaveOportunidade();
        mockAssociarDonoGenerico();
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                999L,
                CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.of(localidadePendente));

        OportunidadeDeEmpregoResponseDTO resposta = oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(dto);

        OportunidadeDeEmprego oportunidadeSalva = capturarOportunidadeSalva();
        assertSame(empresa, oportunidadeSalva.getEmpresa());
        assertNull(oportunidadeSalva.getLocalizacao());
        assertEquals(empresa.getId(), resposta.empresaId());
        assertEquals("PENDENTE", resposta.statusLocalidade());
        assertEquals(999L, localidadePendente.getRecursoId());
    }

    private void prepararMocksBasicos(PerfilRecrutador perfilRecrutador, TipoDeEmprego tipoDeEmprego) {
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);
        when(tipoDeEmpregoRepository.findById(tipoDeEmprego.getId())).thenReturn(Optional.of(tipoDeEmprego));
    }

    private void prepararMockSaveOportunidade() {
        when(oportunidadeDeEmpregoRepository.save(any(OportunidadeDeEmprego.class))).thenAnswer(invocation -> {
            OportunidadeDeEmprego oportunidade = invocation.getArgument(0);
            if (oportunidade.getId() == null) {
                ReflectionTestUtils.setField(oportunidade, "id", 999L);
            }
            return oportunidade;
        });
    }

    private void mockAssociarDonoGenerico() {
        when(registroLocalidadePendenteService.associarDonoGenerico(
                any(LocalidadePendente.class),
                any(TipoRecursoLocalidadePendente.class),
                any(Long.class),
                any(CampoLocalidadePendente.class)
        )).thenAnswer(invocation -> {
            LocalidadePendente pendencia = invocation.getArgument(0);
            pendencia.definirDonoGenerico(
                    invocation.getArgument(1),
                    invocation.getArgument(2),
                    invocation.getArgument(3)
            );
            return pendencia;
        });
    }

    private OportunidadeDeEmprego capturarOportunidadeSalva() {
        ArgumentCaptor<OportunidadeDeEmprego> captor = ArgumentCaptor.forClass(OportunidadeDeEmprego.class);
        verify(oportunidadeDeEmpregoRepository).save(captor.capture());
        return captor.getValue();
    }

    private Pais criarPais(Long id) {
        Pais pais = new Pais("Brasil", "BR");
        ReflectionTestUtils.setField(pais, "id", id);
        return pais;
    }

    private Estado criarEstado(Long id, Pais pais) {
        Estado estado = new Estado("Paraiba", "PB", pais);
        ReflectionTestUtils.setField(estado, "id", id);
        return estado;
    }

    private Cidade criarCidade(Long id, Estado estado) {
        Cidade cidade = new Cidade("Joao Pessoa", estado);
        ReflectionTestUtils.setField(cidade, "id", id);
        return cidade;
    }

    private TipoDeEmprego criarTipoDeEmpregoAprovado(Long id) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(
                "Desenvolvedor Backend Java",
                "Vaga para servicos com Spring Boot."
        );
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }

    private PerfilRecrutador criarPerfilRecrutador(
            Long id,
            String nomeUsuario,
            Empresa empresa,
            StatusVinculoEmpresa statusVinculoEmpresa
    ) {
        Usuario usuario = new Usuario(
                "12345678909",
                nomeUsuario,
                "83999999999",
                "recrutador" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa Legada");
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(statusVinculoEmpresa);
        return perfilRecrutador;
    }

    private Empresa criarEmpresa(Long id, String nome) {
        Empresa empresa = new Empresa(nome, null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }

    private OportunidadeDeEmpregoRequestDTO criarRequestPorIds(Long paisId, Long estadoId, Long cidadeId) {
        return new OportunidadeDeEmpregoRequestDTO(
                "Vaga backend Java 21",
                1L,
                Modalidade.REMOTO,
                paisId,
                estadoId,
                cidadeId,
                null,
                false
        );
    }

    private OportunidadeDeEmpregoRequestDTO criarRequestPorTexto(String localidadeTexto, Boolean publicarComoEmpresa) {
        return new OportunidadeDeEmpregoRequestDTO(
                "Vaga backend Java 21",
                1L,
                Modalidade.REMOTO,
                null,
                null,
                null,
                localidadeTexto,
                publicarComoEmpresa
        );
    }

    private LocalidadePendente criarLocalidadePendente(String textoOriginal, String motivoPendencia) {
        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario(textoOriginal, motivoPendencia);
        ReflectionTestUtils.setField(localidadePendente, "id", 500L);
        return localidadePendente;
    }
}
