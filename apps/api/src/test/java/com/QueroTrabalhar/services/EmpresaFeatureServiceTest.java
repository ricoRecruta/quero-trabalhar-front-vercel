package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.empresa.EmpresaRequestDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadesDaEmpresaFilterDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.RecrutadorDaEmpresaResponseDTO;
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
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import com.QueroTrabalhar.services.localidade.LocalidadeResolucaoService;
import com.QueroTrabalhar.services.localidade.RegistroLocalidadePendenteService;
import com.QueroTrabalhar.services.localidade.ResultadoResolucaoLocalidade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaFeatureServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PerfilRecrutadorRepository perfilRecrutadorRepository;

    @Mock
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

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

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void deveCriarEmpresaComLocalidadeValida() {
        Pais pais = criarPais(1L, "Brasil", "BR");
        Estado estado = criarEstado(10L, "Paraiba", "PB", pais);
        Cidade cidade = criarCidade(100L, "Joao Pessoa", estado);
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "  Empresa ACME  ",
                "  Plataforma de empregabilidade  ",
                "  https://acme.com  ",
                "  contato@acme.com  ",
                "  83999999999  ",
                pais.getId(),
                estado.getId(),
                cidade.getId(),
                null
        );

        when(paisRepository.findById(pais.getId())).thenReturn(Optional.of(pais));
        when(estadoRepository.findById(estado.getId())).thenReturn(Optional.of(estado));
        when(cidadeRepository.findById(cidade.getId())).thenReturn(Optional.of(cidade));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresaSalva = invocation.getArgument(0);
            ReflectionTestUtils.setField(empresaSalva, "id", 50L);
            return empresaSalva;
        });

        EmpresaResponseDTO resposta = empresaService.criarEmpresa(dto);

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresaCaptor.capture());
        Empresa empresaSalva = empresaCaptor.getValue();

        assertEquals(50L, resposta.id());
        assertEquals("Empresa ACME", resposta.nome());
        assertEquals("Plataforma de empregabilidade", resposta.descricao());
        assertEquals("https://acme.com", resposta.site());
        assertEquals("contato@acme.com", resposta.emailPublico());
        assertEquals("83999999999", resposta.telefonePublico());
        assertEquals(1L, resposta.paisId());
        assertEquals(10L, resposta.estadoId());
        assertEquals(100L, resposta.cidadeId());
        assertSame(pais, empresaSalva.getLocalidade().getPais());
        assertSame(estado, empresaSalva.getLocalidade().getEstado());
        assertSame(cidade, empresaSalva.getLocalidade().getCidade());
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoPaisNaoExistir() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                99L,
                null,
                null,
                null
        );

        when(paisRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> empresaService.criarEmpresa(dto));

        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveLancarBusinessRuleExceptionQuandoCidadeForInformadaSemEstado() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                1L,
                null,
                100L,
                null
        );

        assertThrows(BusinessRuleException.class, () -> empresaService.criarEmpresa(dto));

        verify(paisRepository, never()).findById(any(Long.class));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveLancarBusinessRuleExceptionQuandoEstadoNaoPertencerAoPais() {
        Pais brasil = criarPais(1L, "Brasil", "BR");
        Pais argentina = criarPais(2L, "Argentina", "AR");
        Estado cordoba = criarEstado(10L, "Cordoba", "CB", argentina);
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                brasil.getId(),
                cordoba.getId(),
                null,
                null
        );

        when(paisRepository.findById(brasil.getId())).thenReturn(Optional.of(brasil));
        when(estadoRepository.findById(cordoba.getId())).thenReturn(Optional.of(cordoba));

        assertThrows(BusinessRuleException.class, () -> empresaService.criarEmpresa(dto));

        verify(cidadeRepository, never()).findById(any(Long.class));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveLancarBusinessRuleExceptionQuandoCidadeNaoPertencerAoEstado() {
        Pais pais = criarPais(1L, "Brasil", "BR");
        Estado paraiba = criarEstado(10L, "Paraiba", "PB", pais);
        Estado pernambuco = criarEstado(20L, "Pernambuco", "PE", pais);
        Cidade recife = criarCidade(100L, "Recife", pernambuco);
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                pais.getId(),
                paraiba.getId(),
                recife.getId(),
                null
        );

        when(paisRepository.findById(pais.getId())).thenReturn(Optional.of(pais));
        when(estadoRepository.findById(paraiba.getId())).thenReturn(Optional.of(paraiba));
        when(cidadeRepository.findById(recife.getId())).thenReturn(Optional.of(recife));

        assertThrows(BusinessRuleException.class, () -> empresaService.criarEmpresa(dto));

        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveCriarEmpresaComLocalidadeResolvidaPorTextoLivre() {
        Pais pais = criarPais(1L, "Brasil", "BR");
        Estado estado = criarEstado(10L, "Paraiba", "PB", pais);
        Cidade cidade = criarCidade(100L, "Joao Pessoa", estado);
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "  Joao Pessoa  "
        );

        when(localidadeResolucaoService.resolver("Joao Pessoa"))
                .thenReturn(ResultadoResolucaoLocalidade.resolvida(new Localidade(pais, estado, cidade)));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresaSalva = invocation.getArgument(0);
            ReflectionTestUtils.setField(empresaSalva, "id", 60L);
            return empresaSalva;
        });

        EmpresaResponseDTO resposta = empresaService.criarEmpresa(dto);

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresaCaptor.capture());
        Empresa empresaSalva = empresaCaptor.getValue();

        assertEquals(60L, resposta.id());
        assertEquals("VALIDADA", resposta.statusLocalidade());
        assertEquals(1L, resposta.paisId());
        assertEquals(10L, resposta.estadoId());
        assertEquals(100L, resposta.cidadeId());
        assertSame(pais, empresaSalva.getLocalidade().getPais());
        assertSame(estado, empresaSalva.getLocalidade().getEstado());
        assertSame(cidade, empresaSalva.getLocalidade().getCidade());
    }

    @Test
    void deveCriarEmpresaComLocalidadePendenteQuandoTextoLivreNaoForResolvido() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "  Vale Imaginario  "
        );
        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario",
                "Localidade não encontrada"
        );

        when(localidadeResolucaoService.resolver("Vale Imaginario"))
                .thenReturn(ResultadoResolucaoLocalidade.pendente(localidadePendente));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresaSalva = invocation.getArgument(0);
            ReflectionTestUtils.setField(empresaSalva, "id", 61L);
            return empresaSalva;
        });
        when(registroLocalidadePendenteService.associarDonoGenerico(
                localidadePendente,
                TipoRecursoLocalidadePendente.EMPRESA,
                61L,
                CampoLocalidadePendente.LOCALIDADE
        )).thenAnswer(invocation -> {
            LocalidadePendente pendencia = invocation.getArgument(0);
            pendencia.definirDonoGenerico(
                    invocation.getArgument(1),
                    invocation.getArgument(2),
                    invocation.getArgument(3)
            );
            return pendencia;
        });
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.EMPRESA,
                61L,
                CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.of(localidadePendente));

        EmpresaResponseDTO resposta = empresaService.criarEmpresa(dto);

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresaCaptor.capture());
        Empresa empresaSalva = empresaCaptor.getValue();

        assertEquals(61L, resposta.id());
        assertEquals("PENDENTE", resposta.statusLocalidade());
        assertEquals("Vale Imaginario", resposta.localidadeTextoOriginal());
        assertEquals(StatusValidacaoLocalidade.PENDENTE_VALIDACAO, resposta.statusValidacaoLocalidade());
        assertNull(resposta.paisId());
        assertNull(empresaSalva.getLocalidade());
        assertEquals(TipoRecursoLocalidadePendente.EMPRESA, localidadePendente.getTipoRecurso());
        assertEquals(61L, localidadePendente.getRecursoId());
        assertEquals(CampoLocalidadePendente.LOCALIDADE, localidadePendente.getCampoAlvo());
        verify(registroLocalidadePendenteService).associarDonoGenerico(
                localidadePendente,
                TipoRecursoLocalidadePendente.EMPRESA,
                61L,
                CampoLocalidadePendente.LOCALIDADE
        );
    }

    @Test
    void deveLancarBusinessRuleExceptionQuandoLocalidadeNaoForInformada() {
        EmpresaRequestDTO dto = new EmpresaRequestDTO(
                "Empresa ACME",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "   "
        );

        assertThrows(BusinessRuleException.class, () -> empresaService.criarEmpresa(dto));

        verify(localidadeResolucaoService, never()).resolver(any(String.class));
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void deveListarEmpresasPublicasComLocalidadeValidada() {
        Pais pais = criarPais(1L, "Brasil", "BR");
        Empresa empresaAlpha = criarEmpresaValidada(1L, "Empresa Alpha", new Localidade(pais));
        empresaAlpha.setDescricao("Plataforma de talentos");
        empresaAlpha.setSite("https://alpha.com");
        Empresa empresaBeta = criarEmpresaValidada(2L, "Empresa Beta", new Localidade(pais));
        empresaBeta.setEmailPublico("contato@beta.com");
        Pageable pageable = PageRequest.of(0, 10);

        when(empresaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(empresaAlpha, empresaBeta), pageable, 2));

        Page<EmpresaPublicaResponseDTO> resposta = empresaService.listarEmpresas(null, pageable);

        assertEquals(2, resposta.getTotalElements());
        assertEquals(2, resposta.getContent().size());
        assertNotSame(empresaAlpha, resposta.getContent().get(0));
        assertInstanceOf(EmpresaPublicaResponseDTO.class, resposta.getContent().get(0));

        EmpresaPublicaResponseDTO primeiraEmpresa = resposta.getContent().get(0);
        EmpresaPublicaResponseDTO segundaEmpresa = resposta.getContent().get(1);

        assertAll(
                () -> assertEquals(1L, primeiraEmpresa.id()),
                () -> assertEquals("Empresa Alpha", primeiraEmpresa.nome()),
                () -> assertEquals("Plataforma de talentos", primeiraEmpresa.descricao()),
                () -> assertEquals("https://alpha.com", primeiraEmpresa.site()),
                () -> assertEquals(2L, segundaEmpresa.id()),
                () -> assertEquals("Empresa Beta", segundaEmpresa.nome()),
                () -> assertEquals("contato@beta.com", segundaEmpresa.emailPublico())
        );
        verify(empresaRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveBuscarEmpresaPorIdComSucesso() {
        Empresa empresa = criarEmpresa(7L, "Empresa Busca");
        empresa.setDescricao("Consultoria especializada");
        empresa.setTelefonePublico("83988887777");

        when(empresaRepository.findByIdAndLocalidadePaisIsNotNull(7L)).thenReturn(Optional.of(empresa));

        EmpresaPublicaResponseDTO resposta = empresaService.buscarEmpresaPorId(7L);

        assertNotSame(empresa, resposta);
        assertAll(
                () -> assertEquals(7L, resposta.id()),
                () -> assertEquals("Empresa Busca", resposta.nome()),
                () -> assertEquals("Consultoria especializada", resposta.descricao()),
                () -> assertEquals("83988887777", resposta.telefonePublico())
        );
        verify(empresaRepository).findByIdAndLocalidadePaisIsNotNull(7L);
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoEmpresaNaoExistir() {
        when(empresaRepository.findByIdAndLocalidadePaisIsNotNull(404L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> empresaService.buscarEmpresaPorId(404L));

        verify(empresaRepository).findByIdAndLocalidadePaisIsNotNull(404L);
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoEmpresaPossuirLocalidadePendenteNaConsultaPublica() {
        when(empresaRepository.findByIdAndLocalidadePaisIsNotNull(405L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> empresaService.buscarEmpresaPorId(405L));

        verify(empresaRepository).findByIdAndLocalidadePaisIsNotNull(405L);
    }

    @Test
    void deveListarRecrutadoresAprovadosDaEmpresa() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador recrutadorComNomeZeca = criarPerfilRecrutador(10L, "Zeca", empresa, StatusVinculoEmpresa.APROVADO);
        PerfilRecrutador recrutadorComNomeAna = criarPerfilRecrutador(11L, "Ana", empresa, StatusVinculoEmpresa.APROVADO);

        when(empresaRepository.findByIdAndLocalidadePaisIsNotNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(perfilRecrutadorRepository.findByEmpresaVinculadaIdAndStatusVinculoEmpresa(
                empresa.getId(),
                StatusVinculoEmpresa.APROVADO
        )).thenReturn(List.of(recrutadorComNomeZeca, recrutadorComNomeAna));

        List<RecrutadorDaEmpresaResponseDTO> resposta =
                empresaService.listarRecrutadoresAprovadosDaEmpresa(empresa.getId());

        assertEquals(2, resposta.size());
        assertEquals("Ana", resposta.get(0).nome());
        assertEquals("Zeca", resposta.get(1).nome());
        assertEquals(empresa.getId(), resposta.get(0).empresaId());
        verify(empresaRepository).findByIdAndLocalidadePaisIsNotNull(empresa.getId());
        verify(perfilRecrutadorRepository)
                .findByEmpresaVinculadaIdAndStatusVinculoEmpresa(empresa.getId(), StatusVinculoEmpresa.APROVADO);
    }

    @Test
    void deveListarOportunidadesPublicadasEmNomeDaEmpresaComPaginacaoEFiltros() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        Pais pais = criarPais(1L, "Brasil", "BR");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(20L, "Desenvolvedor Backend");
        PerfilRecrutador recrutador = criarPerfilRecrutador(10L, "Ana", empresa, StatusVinculoEmpresa.APROVADO);
        OportunidadesDaEmpresaFilterDTO filtro = new OportunidadesDaEmpresaFilterDTO(
                "  Java  ",
                tipoDeEmprego.getId(),
                recrutador.getId(),
                pais.getId(),
                null,
                null,
                Modalidade.REMOTO
        );
        Pageable pageable = PageRequest.of(0, 10);
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                "Vaga Java",
                tipoDeEmprego,
                Modalidade.REMOTO,
                new Localidade(pais),
                recrutador,
                empresa
        );
        ReflectionTestUtils.setField(oportunidade, "id", 100L);

        when(empresaRepository.findByIdAndLocalidadePaisIsNotNull(empresa.getId())).thenReturn(Optional.of(empresa));
        when(oportunidadeDeEmpregoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(oportunidade), pageable, 1));

        Page<OportunidadeDeEmpregoPublicaResponseDTO> resposta =
                empresaService.listarOportunidadesDaEmpresa(empresa.getId(), filtro, pageable);

        assertEquals(1, resposta.getTotalElements());
        assertEquals(1, resposta.getContent().size());
        assertEquals(100L, resposta.getContent().get(0).id());
        assertEquals("Vaga Java", resposta.getContent().get(0).descricao());
        assertEquals(empresa.getId(), resposta.getContent().get(0).empresaId());
        assertEquals("Empresa ACME", resposta.getContent().get(0).empresaNome());
        verify(empresaRepository).findByIdAndLocalidadePaisIsNotNull(empresa.getId());
        verify(oportunidadeDeEmpregoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void devePreservarNotFoundQuandoEmpresaNaoExistirAoListarOportunidades() {
        Pageable pageable = PageRequest.of(0, 10);

        when(empresaRepository.findByIdAndLocalidadePaisIsNotNull(404L)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> empresaService.listarOportunidadesDaEmpresa(404L, null, pageable)
        );

        verify(empresaRepository).findByIdAndLocalidadePaisIsNotNull(404L);
        verify(oportunidadeDeEmpregoRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    private Pais criarPais(Long id, String nome, String sigla) {
        Pais pais = new Pais(nome, sigla);
        ReflectionTestUtils.setField(pais, "id", id);
        return pais;
    }

    private Estado criarEstado(Long id, String nome, String sigla, Pais pais) {
        Estado estado = new Estado(nome, sigla, pais);
        ReflectionTestUtils.setField(estado, "id", id);
        return estado;
    }

    private Cidade criarCidade(Long id, String nome, Estado estado) {
        Cidade cidade = new Cidade(nome, estado);
        ReflectionTestUtils.setField(cidade, "id", id);
        return cidade;
    }

    private Empresa criarEmpresa(Long id, String nome) {
        Empresa empresa = new Empresa(nome, null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }

    private Empresa criarEmpresaValidada(Long id, String nome, Localidade localidade) {
        Empresa empresa = new Empresa(nome, null, null, null, null, localidade);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
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
                nomeUsuario.toLowerCase() + "@email.com",
                "SenhaForte123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(usuario, "Empresa antiga");
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(statusVinculoEmpresa);
        return perfilRecrutador;
    }

    private TipoDeEmprego criarTipoDeEmpregoAprovado(Long id, String titulo) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(titulo, "Descricao");
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }
}
