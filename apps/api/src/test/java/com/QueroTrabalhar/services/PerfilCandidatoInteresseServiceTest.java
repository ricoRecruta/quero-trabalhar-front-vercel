package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeInteresseCandidatoFilterDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilCandidatoInteresseServiceTest {

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

    @Mock
    private LocalidadePendenteRepository localidadePendenteRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private PerfilCandidatoService perfilCandidatoService;

    @Test
    void deveDemonstrarInteresseEmVagaComCandidatoAutenticado() {
        Long vagaId = 100L;
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(1L, "Joao da Silva");
        OportunidadeDeEmprego vaga = criarOportunidade(
                vagaId,
                "Pessoa desenvolvedora Java 21",
                Modalidade.REMOTO,
                criarTipoDeEmpregoAprovado(5L, "Backend"),
                criarPerfilRecrutador(20L, "Marina Costa"),
                null,
                criarPais(1L, "Brasil", "BR")
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        perfilCandidatoService.demonstrarInteresseEmVaga(vagaId);

        assertTrue(perfilCandidato.getVagasDeInteresse().contains(vaga));
        verify(perfilCandidatoRepository).save(perfilCandidato);
    }

    @Test
    void deveBloquearInteresseDuplicadoNaMesmaVaga() {
        Long vagaId = 101L;
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(2L, "Livia Andrade");
        OportunidadeDeEmprego vaga = criarOportunidade(
                vagaId,
                "Tech lead para microsservicos",
                Modalidade.HIBRIDO,
                criarTipoDeEmpregoAprovado(6L, "Lideranca tecnica"),
                criarPerfilRecrutador(21L, "Rafael Dias"),
                null,
                criarPais(1L, "Brasil", "BR")
        );
        perfilCandidato.demonstrarInteresse(vaga);

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        assertThrows(
                BusinessRuleException.class,
                () -> perfilCandidatoService.demonstrarInteresseEmVaga(vagaId)
        );

        verify(perfilCandidatoRepository, never()).save(any(PerfilCandidato.class));
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoVagaNaoExistirAoDemonstrarInteresse() {
        Long vagaId = 999L;
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(3L, "Erica Pires");

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> perfilCandidatoService.demonstrarInteresseEmVaga(vagaId)
        );

        verify(perfilCandidatoRepository, never()).save(any(PerfilCandidato.class));
    }

    @Test
    void deveRemoverInteresseEmVagaComCandidatoAutenticado() {
        Long vagaId = 102L;
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(4L, "Carlos Henrique");
        OportunidadeDeEmprego vaga = criarOportunidade(
                vagaId,
                "Especialista em integracoes REST",
                Modalidade.PRESENCIAL,
                criarTipoDeEmpregoAprovado(7L, "Integracoes"),
                criarPerfilRecrutador(22L, "Fernanda Rocha"),
                null,
                criarPais(1L, "Brasil", "BR")
        );
        perfilCandidato.demonstrarInteresse(vaga);

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        perfilCandidatoService.removerInteresseEmVaga(vagaId);

        assertFalse(perfilCandidato.getVagasDeInteresse().contains(vaga));
        verify(perfilCandidatoRepository).save(perfilCandidato);
    }

    @Test
    void deveBloquearRemocaoDeInteresseInexistente() {
        Long vagaId = 103L;
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(5L, "Ana Paula");
        OportunidadeDeEmprego vaga = criarOportunidade(
                vagaId,
                "Desenvolvimento orientado a eventos",
                Modalidade.REMOTO,
                criarTipoDeEmpregoAprovado(8L, "Mensageria"),
                criarPerfilRecrutador(23L, "Patricia Mendes"),
                null,
                criarPais(1L, "Brasil", "BR")
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        assertThrows(
                BusinessRuleException.class,
                () -> perfilCandidatoService.removerInteresseEmVaga(vagaId)
        );

        verify(perfilCandidatoRepository, never()).save(any(PerfilCandidato.class));
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoVagaNaoExistirAoRemoverInteresse() {
        Long vagaId = 1000L;
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(6L, "Marcelo Junior");

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThrows(
                ObjectNotFoundException.class,
                () -> perfilCandidatoService.removerInteresseEmVaga(vagaId)
        );

        verify(perfilCandidatoRepository, never()).save(any(PerfilCandidato.class));
    }

    @Test
    void deveListarMinhasVagasDeInteresseEmPagina() {
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(7L, "Joana DAvila");
        PerfilRecrutador recrutador = criarPerfilRecrutador(30L, "Camila Araujo");
        Empresa empresa = criarEmpresa(40L, "Inovacao Publica");
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        OportunidadeDeEmprego vaga = criarOportunidade(
                104L,
                "Backend Java para servicos digitais",
                Modalidade.HIBRIDO,
                criarTipoDeEmpregoAprovado(9L, "Backend Senior"),
                recrutador,
                empresa,
                criarPais(1L, "Brasil", "BR")
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(vaga), pageable, 1)
        );

        Page<?> resposta = perfilCandidatoService.listarMinhasVagasDeInteresse(
                criarFiltroInteresseCandidatoVazio(),
                pageable
        );

        assertEquals(1, resposta.getTotalElements());
        assertEquals(1, resposta.getContent().size());
        assertInstanceOf(OportunidadeDeEmpregoResponseDTO.class, resposta.getContent().get(0));
        assertNotSame(vaga, resposta.getContent().get(0));

        OportunidadeDeEmpregoResponseDTO dto = (OportunidadeDeEmpregoResponseDTO) resposta.getContent().get(0);
        assertAll(
                () -> assertEquals(104L, dto.id()),
                () -> assertEquals("Backend Java para servicos digitais", dto.descricao()),
                () -> assertEquals(30L, dto.recrutadorId()),
                () -> assertEquals("Camila Araujo", dto.recrutadorNome()),
                () -> assertEquals(40L, dto.empresaId()),
                () -> assertEquals("Inovacao Publica", dto.empresaNome())
        );
        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(oportunidadeDeEmpregoRepository).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(perfilCandidatoRepository, localidadePendenteRepository);
    }

    @Test
    void deveBuscarPendenciasEmLoteAoListarMinhasVagasDeInteresse() {
        PerfilCandidato perfilCandidato = criarPerfilCandidatoAutenticado(8L, "Carla Mendes");
        PerfilRecrutador recrutador = criarPerfilRecrutador(31L, "Renata Alves");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(10L, "Backend");
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));

        OportunidadeDeEmprego vagaPendente = new OportunidadeDeEmprego(
                "Fila tecnica 1",
                tipoDeEmprego,
                Modalidade.REMOTO,
                null,
                recrutador,
                null
        );
        ReflectionTestUtils.setField(vagaPendente, "id", 401L);

        OportunidadeDeEmprego vagaPendente2 = new OportunidadeDeEmprego(
                "Fila tecnica 2",
                tipoDeEmprego,
                Modalidade.HIBRIDO,
                null,
                recrutador,
                null
        );
        ReflectionTestUtils.setField(vagaPendente2, "id", 402L);

        LocalidadePendente primeiraPendencia = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario",
                "Localidade não encontrada"
        );
        primeiraPendencia.definirDonoGenerico(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                401L,
                CampoLocalidadePendente.LOCALIDADE
        );

        LocalidadePendente segundaPendencia = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Serra do Sol Tech",
                "Localidade ambigua"
        );
        segundaPendencia.definirDonoGenerico(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                402L,
                CampoLocalidadePendente.LOCALIDADE
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(oportunidadeDeEmpregoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(vagaPendente, vagaPendente2), pageable, 2)
        );
        when(localidadePendenteRepository.findByTipoRecursoAndCampoAlvoAndRecursoIdInOrderByRecursoIdAscAtualizadaEmDescCriadaEmDesc(
                org.mockito.ArgumentMatchers.eq(TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO),
                org.mockito.ArgumentMatchers.eq(CampoLocalidadePendente.LOCALIDADE),
                org.mockito.ArgumentMatchers.argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(401L, 402L)))
        )).thenReturn(List.of(primeiraPendencia, segundaPendencia));

        Page<OportunidadeDeEmpregoResponseDTO> resposta = perfilCandidatoService.listarMinhasVagasDeInteresse(
                criarFiltroInteresseCandidatoVazio(),
                pageable
        );

        assertAll(
                () -> assertEquals(2, resposta.getContent().size()),
                () -> assertTrue(
                        resposta.getContent().stream().allMatch(dto -> "PENDENTE".equals(dto.statusLocalidade()))
                ),
                () -> assertEquals(
                        Set.of("Vale Imaginario", "Serra do Sol Tech"),
                        resposta.getContent().stream()
                                .map(OportunidadeDeEmpregoResponseDTO::localidadeTextoOriginal)
                                .collect(java.util.stream.Collectors.toSet())
                )
        );
        verify(oportunidadeDeEmpregoRepository).findAll(any(Specification.class), eq(pageable));
        verify(localidadePendenteRepository).findByTipoRecursoAndCampoAlvoAndRecursoIdInOrderByRecursoIdAscAtualizadaEmDescCriadaEmDesc(
                org.mockito.ArgumentMatchers.eq(TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO),
                org.mockito.ArgumentMatchers.eq(CampoLocalidadePendente.LOCALIDADE),
                org.mockito.ArgumentMatchers.argThat(ids -> ids.size() == 2 && ids.containsAll(List.of(401L, 402L)))
        );
    }

    private OportunidadeInteresseCandidatoFilterDTO criarFiltroInteresseCandidatoVazio() {
        return new OportunidadeInteresseCandidatoFilterDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PerfilCandidato criarPerfilCandidatoAutenticado(Long id, String nomeUsuario) {
        Usuario usuario = new Usuario(
                "12345678909",
                nomeUsuario,
                "83999999999",
                "candidato" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilCandidato perfilCandidato = new PerfilCandidato(usuario);
        ReflectionTestUtils.setField(perfilCandidato, "id", id);
        usuario.adicionarPerfilCandidato(perfilCandidato);

        return perfilCandidato;
    }

    private PerfilRecrutador criarPerfilRecrutador(Long id, String nomeUsuario) {
        Usuario usuario = new Usuario(
                "98765432100",
                nomeUsuario,
                "83988888888",
                "recrutador" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa Legada");
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);

        return perfilRecrutador;
    }

    private OportunidadeDeEmprego criarOportunidade(
            Long id,
            String descricao,
            Modalidade modalidade,
            TipoDeEmprego tipoDeEmprego,
            PerfilRecrutador perfilRecrutador,
            Empresa empresa,
            Pais pais
    ) {
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                descricao,
                tipoDeEmprego,
                modalidade,
                new Localidade(pais),
                perfilRecrutador,
                empresa
        );
        ReflectionTestUtils.setField(oportunidade, "id", id);
        return oportunidade;
    }

    private TipoDeEmprego criarTipoDeEmpregoAprovado(Long id, String titulo) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(titulo, "Descricao do tipo");
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }

    private Pais criarPais(Long id, String nome, String sigla) {
        Pais pais = new Pais(nome, sigla);
        ReflectionTestUtils.setField(pais, "id", id);
        return pais;
    }

    private Empresa criarEmpresa(Long id, String nome) {
        Empresa empresa = new Empresa(nome, null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }
}
