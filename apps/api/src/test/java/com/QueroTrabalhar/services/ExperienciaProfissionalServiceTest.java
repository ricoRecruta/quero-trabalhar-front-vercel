package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalFilterDTO;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalRequestDTO;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalResponseDTO;
import com.QueroTrabalhar.domain.entity.ExperienciaProfissional;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.ExperienciaProfissionalRepository;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperienciaProfissionalServiceTest {

    @Mock
    private ExperienciaProfissionalRepository experienciaProfissionalRepository;

    @Mock
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private ExperienciaProfissionalService experienciaProfissionalService;

    @Test
    void deveListarMinhasExperiencias() {
        // Arrange
        PerfilCandidato perfilCandidato = criarPerfilCandidato(1L, "Ana Lima");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmprego(10L, "Backend");
        ExperienciaProfissional experiencia = criarExperiencia(
                100L,
                perfilCandidato,
                tipoDeEmprego,
                "Atuacao com Java 21 e Spring Boot 3",
                LocalDate.of(2023, 1, 10),
                LocalDate.of(2024, 3, 20)
        );
        perfilCandidato.adicionarExperiencia(experiencia);

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);

        // Act
        List<?> resposta = experienciaProfissionalService.listarTodasAsExperienciaProfissionais();

        // Assert
        assertEquals(1, resposta.size());
        assertInstanceOf(ExperienciaProfissionalResponseDTO.class, resposta.get(0));
        assertNotSame(experiencia, resposta.get(0));

        ExperienciaProfissionalResponseDTO dto = (ExperienciaProfissionalResponseDTO) resposta.get(0);
        assertAll(
                () -> assertEquals(100L, dto.id()),
                () -> assertEquals(10L, dto.tipoDeEmprego()),
                () -> assertEquals("Atuacao com Java 21 e Spring Boot 3", dto.descricao()),
                () -> assertEquals(LocalDate.of(2023, 1, 10), dto.dataInicio()),
                () -> assertEquals(LocalDate.of(2024, 3, 20), dto.dataFim())
        );

        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verifyNoInteractions(experienciaProfissionalRepository, tipoDeEmpregoRepository);
    }

    @Test
    void deveBuscarMinhaExperienciaComSucesso() {
        // Arrange
        Long experienciaId = 101L;
        PerfilCandidato perfilCandidato = criarPerfilCandidato(2L, "Bruno Costa");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmprego(11L, "Tech Lead");
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                perfilCandidato,
                tipoDeEmprego,
                "Lideranca tecnica em plataforma de empregabilidade",
                LocalDate.of(2022, 5, 1),
                null
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        ExperienciaProfissionalResponseDTO resposta =
                experienciaProfissionalService.buscarMinhaExperienciaPorId(experienciaId);

        // Assert
        assertAll(
                () -> assertEquals(101L, resposta.id()),
                () -> assertEquals(11L, resposta.tipoDeEmprego()),
                () -> assertEquals("Lideranca tecnica em plataforma de empregabilidade", resposta.descricao()),
                () -> assertEquals(LocalDate.of(2022, 5, 1), resposta.dataInicio()),
                () -> assertNull(resposta.dataFim())
        );

        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(experienciaProfissionalRepository).findById(experienciaId);
        verifyNoInteractions(tipoDeEmpregoRepository);
    }

    @Test
    void deveBloquearBuscaDeExperienciaDeOutroCandidato() {
        // Arrange
        Long experienciaId = 102L;
        PerfilCandidato perfilAutenticado = criarPerfilCandidato(3L, "Carla Dias");
        PerfilCandidato outroPerfil = criarPerfilCandidato(4L, "Diego Alves");
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                outroPerfil,
                criarTipoDeEmprego(12L, "QA"),
                "Automacao de testes",
                LocalDate.of(2021, 4, 1),
                null
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilAutenticado);
        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        assertThrows(
                BusinessRuleException.class,
                () -> experienciaProfissionalService.buscarMinhaExperienciaPorId(experienciaId)
        );

        // Assert
        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(experienciaProfissionalRepository).findById(experienciaId);
        verifyNoInteractions(tipoDeEmpregoRepository);
    }

    @Test
    void deveAdicionarMinhaExperienciaComSucesso() {
        // Arrange
        PerfilCandidato perfilCandidato = criarPerfilCandidato(5L, "Eduarda Rocha");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmprego(13L, "Backend Senior");
        ExperienciaProfissionalRequestDTO request = new ExperienciaProfissionalRequestDTO(
                13L,
                "Criacao e manutencao de APIs REST",
                LocalDate.of(2020, 2, 3),
                LocalDate.of(2023, 8, 31)
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(tipoDeEmpregoRepository.findById(request.tipoDeEmpregoId())).thenReturn(Optional.of(tipoDeEmprego));
        when(experienciaProfissionalRepository.save(any(ExperienciaProfissional.class))).thenAnswer(invocation -> {
            ExperienciaProfissional experienciaSalva = invocation.getArgument(0);
            ReflectionTestUtils.setField(experienciaSalva, "id", 103L);
            return experienciaSalva;
        });

        // Act
        ExperienciaProfissionalResponseDTO resposta =
                experienciaProfissionalService.adicionarMinhaExperienciaProfissional(request);

        // Assert
        ArgumentCaptor<ExperienciaProfissional> experienciaCaptor =
                ArgumentCaptor.forClass(ExperienciaProfissional.class);

        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(tipoDeEmpregoRepository).findById(request.tipoDeEmpregoId());
        verify(experienciaProfissionalRepository).save(experienciaCaptor.capture());

        ExperienciaProfissional experienciaSalva = experienciaCaptor.getValue();
        assertAll(
                () -> assertEquals(103L, resposta.id()),
                () -> assertEquals(13L, resposta.tipoDeEmprego()),
                () -> assertEquals(request.descricao(), resposta.descricao()),
                () -> assertEquals(request.dataInicio(), resposta.dataInicio()),
                () -> assertEquals(request.dataFim(), resposta.dataFim()),
                () -> assertSame(perfilCandidato, experienciaSalva.getPerfilCandidato()),
                () -> assertSame(tipoDeEmprego, experienciaSalva.getTipoDeEmprego()),
                () -> assertTrue(perfilCandidato.getExperiencias().contains(experienciaSalva))
        );
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoTipoDeEmpregoNaoExistirAoAdicionar() {
        // Arrange
        PerfilCandidato perfilCandidato = criarPerfilCandidato(6L, "Felipe Matos");
        ExperienciaProfissionalRequestDTO request = new ExperienciaProfissionalRequestDTO(
                999L,
                "Atuacao full cycle",
                LocalDate.of(2021, 1, 1),
                null
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(tipoDeEmpregoRepository.findById(request.tipoDeEmpregoId())).thenReturn(Optional.empty());

        // Act
        assertThrows(
                ObjectNotFoundException.class,
                () -> experienciaProfissionalService.adicionarMinhaExperienciaProfissional(request)
        );

        // Assert
        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(tipoDeEmpregoRepository).findById(request.tipoDeEmpregoId());
        verify(experienciaProfissionalRepository, never()).save(any(ExperienciaProfissional.class));
    }

    @Test
    void deveDeletarMinhaExperienciaComSucesso() {
        // Arrange
        Long experienciaId = 104L;
        PerfilCandidato perfilCandidato = criarPerfilCandidato(7L, "Gabriela Nunes");
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                perfilCandidato,
                criarTipoDeEmprego(14L, "Arquiteto de Software"),
                "Definicao de arquitetura e ownership de servicos",
                LocalDate.of(2019, 7, 1),
                null
        );
        perfilCandidato.adicionarExperiencia(experiencia);

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilCandidato);
        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        experienciaProfissionalService.deletarMinhaExperiencia(experienciaId);

        // Assert
        assertFalse(perfilCandidato.getExperiencias().contains(experiencia));
        assertNull(experiencia.getPerfilCandidato());
        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(experienciaProfissionalRepository).findById(experienciaId);
        verify(experienciaProfissionalRepository).delete(same(experiencia));
        verifyNoInteractions(tipoDeEmpregoRepository);
    }

    @Test
    void deveBloquearDelecaoDeExperienciaDeOutroCandidato() {
        // Arrange
        Long experienciaId = 105L;
        PerfilCandidato perfilAutenticado = criarPerfilCandidato(8L, "Helena Prado");
        PerfilCandidato outroPerfil = criarPerfilCandidato(9L, "Igor Ramos");
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                outroPerfil,
                criarTipoDeEmprego(15L, "Dados"),
                "Pipelines e observabilidade",
                LocalDate.of(2020, 6, 1),
                null
        );

        when(usuarioAutenticadoService.obterPerfilCandidatoAutenticado()).thenReturn(perfilAutenticado);
        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        assertThrows(
                BusinessRuleException.class,
                () -> experienciaProfissionalService.deletarMinhaExperiencia(experienciaId)
        );

        // Assert
        verify(usuarioAutenticadoService).obterPerfilCandidatoAutenticado();
        verify(experienciaProfissionalRepository).findById(experienciaId);
        verify(experienciaProfissionalRepository, never()).delete(any(ExperienciaProfissional.class));
        verifyNoInteractions(tipoDeEmpregoRepository);
    }

    @Test
    void deveAtualizarExperienciaComoAdmin() {
        // Arrange
        Long experienciaId = 106L;
        PerfilCandidato perfilCandidato = criarPerfilCandidato(10L, "Julia Lopes");
        TipoDeEmprego tipoAtual = criarTipoDeEmprego(16L, "Backend");
        TipoDeEmprego novoTipo = criarTipoDeEmprego(17L, "Staff Engineer");
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                perfilCandidato,
                tipoAtual,
                "Responsavel por APIs internas",
                LocalDate.of(2021, 3, 15),
                null
        );
        ExperienciaProfissionalRequestDTO request = new ExperienciaProfissionalRequestDTO(
                17L,
                "Responsavel por plataforma e padroes de servico",
                LocalDate.of(2021, 4, 1),
                LocalDate.of(2024, 4, 30)
        );

        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));
        when(tipoDeEmpregoRepository.findById(request.tipoDeEmpregoId())).thenReturn(Optional.of(novoTipo));
        when(experienciaProfissionalRepository.save(any(ExperienciaProfissional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ExperienciaProfissionalResponseDTO resposta =
                experienciaProfissionalService.atualizarExperienciaComoAdmin(experienciaId, request);

        // Assert
        ArgumentCaptor<ExperienciaProfissional> experienciaCaptor =
                ArgumentCaptor.forClass(ExperienciaProfissional.class);

        verify(experienciaProfissionalRepository).findById(experienciaId);
        verify(tipoDeEmpregoRepository).findById(request.tipoDeEmpregoId());
        verify(experienciaProfissionalRepository).save(experienciaCaptor.capture());

        ExperienciaProfissional experienciaSalva = experienciaCaptor.getValue();
        assertAll(
                () -> assertSame(experiencia, experienciaSalva),
                () -> assertSame(novoTipo, experienciaSalva.getTipoDeEmprego()),
                () -> assertEquals(request.descricao(), experienciaSalva.getDescricao()),
                () -> assertEquals(request.dataInicio(), experienciaSalva.getDataInicio()),
                () -> assertEquals(request.dataFim(), experienciaSalva.getDataFim()),
                () -> assertEquals(106L, resposta.id()),
                () -> assertEquals(17L, resposta.tipoDeEmprego()),
                () -> assertEquals(request.descricao(), resposta.descricao()),
                () -> assertEquals(request.dataInicio(), resposta.dataInicio()),
                () -> assertEquals(request.dataFim(), resposta.dataFim())
        );
        verifyNoInteractions(usuarioAutenticadoService);
    }

    @Test
    void deveDeletarExperienciaComoAdminComPerfilCandidato() {
        // Arrange
        Long experienciaId = 107L;
        PerfilCandidato perfilCandidato = criarPerfilCandidato(11L, "Karen Souza");
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                perfilCandidato,
                criarTipoDeEmprego(18L, "Gerente de Engenharia"),
                "Coordenacao de times backend",
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2020, 12, 31)
        );
        perfilCandidato.adicionarExperiencia(experiencia);

        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        experienciaProfissionalService.deletarExperienciaComoAdmin(experienciaId);

        // Assert
        assertFalse(perfilCandidato.getExperiencias().contains(experiencia));
        assertNull(experiencia.getPerfilCandidato());
        verify(experienciaProfissionalRepository).findById(experienciaId);
        verify(experienciaProfissionalRepository).delete(same(experiencia));
        verifyNoInteractions(tipoDeEmpregoRepository, usuarioAutenticadoService);
    }

    @Test
    void deveDeletarExperienciaComoAdminSemPerfilCandidato() {
        // Arrange
        Long experienciaId = 108L;
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                null,
                criarTipoDeEmprego(19L, "Consultor"),
                "Atuacao pontual em diagnostico tecnico",
                LocalDate.of(2017, 2, 1),
                LocalDate.of(2017, 12, 1)
        );

        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        experienciaProfissionalService.deletarExperienciaComoAdmin(experienciaId);

        // Assert
        assertNull(experiencia.getPerfilCandidato());
        verify(experienciaProfissionalRepository).findById(experienciaId);
        verify(experienciaProfissionalRepository).delete(same(experiencia));
        verifyNoInteractions(tipoDeEmpregoRepository, usuarioAutenticadoService);
    }

    @Test
    void deveListarTodasComoAdmin() {
        // Arrange
        PerfilCandidato perfilCandidato = criarPerfilCandidato(12L, "Lucas Farias");
        ExperienciaProfissional primeiraExperiencia = criarExperiencia(
                109L,
                perfilCandidato,
                criarTipoDeEmprego(20L, "Platform Engineer"),
                "Infraestrutura como codigo",
                LocalDate.of(2019, 9, 1),
                null
        );
        ExperienciaProfissional segundaExperiencia = criarExperiencia(
                110L,
                perfilCandidato,
                criarTipoDeEmprego(21L, "SRE"),
                "Confiabilidade e incidentes",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2022, 10, 31)
        );
        ExperienciaProfissionalFilterDTO filtro = new ExperienciaProfissionalFilterDTO(
                "  platform  ",
                null,
                null,
                null,
                null,
                null,
                null
        );
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        when(experienciaProfissionalRepository.findAll(any(Specification.class), same(pageable)))
                .thenReturn(new PageImpl<>(List.of(primeiraExperiencia, segundaExperiencia), pageable, 2));

        // Act
        Page<?> resposta = experienciaProfissionalService.listarTodasExperienciasComoAdmin(filtro, pageable);

        // Assert
        assertEquals(2, resposta.getTotalElements());
        assertEquals(2, resposta.getContent().size());
        assertInstanceOf(ExperienciaProfissionalResponseDTO.class, resposta.getContent().get(0));
        assertInstanceOf(ExperienciaProfissionalResponseDTO.class, resposta.getContent().get(1));
        assertNotSame(primeiraExperiencia, resposta.getContent().get(0));
        assertNotSame(segundaExperiencia, resposta.getContent().get(1));

        ExperienciaProfissionalResponseDTO primeiroDto =
                (ExperienciaProfissionalResponseDTO) resposta.getContent().get(0);
        ExperienciaProfissionalResponseDTO segundoDto =
                (ExperienciaProfissionalResponseDTO) resposta.getContent().get(1);
        assertAll(
                () -> assertEquals(109L, primeiroDto.id()),
                () -> assertEquals(20L, primeiroDto.tipoDeEmprego()),
                () -> assertEquals("Infraestrutura como codigo", primeiroDto.descricao()),
                () -> assertEquals(110L, segundoDto.id()),
                () -> assertEquals(21L, segundoDto.tipoDeEmprego()),
                () -> assertEquals("Confiabilidade e incidentes", segundoDto.descricao())
        );

        verify(experienciaProfissionalRepository).findAll(any(Specification.class), same(pageable));
        verifyNoInteractions(tipoDeEmpregoRepository, usuarioAutenticadoService);
    }

    @Test
    void deveBuscarPorIdComoAdmin() {
        // Arrange
        Long experienciaId = 111L;
        ExperienciaProfissional experiencia = criarExperiencia(
                experienciaId,
                criarPerfilCandidato(13L, "Mariana Teixeira"),
                criarTipoDeEmprego(22L, "Especialista Backend"),
                "Evolucao de monolito para microsservicos",
                LocalDate.of(2022, 11, 1),
                null
        );

        when(experienciaProfissionalRepository.findById(experienciaId)).thenReturn(Optional.of(experiencia));

        // Act
        ExperienciaProfissionalResponseDTO resposta =
                experienciaProfissionalService.buscarExperienciaPorIdComoAdmin(experienciaId);

        // Assert
        assertAll(
                () -> assertEquals(111L, resposta.id()),
                () -> assertEquals(22L, resposta.tipoDeEmprego()),
                () -> assertEquals("Evolucao de monolito para microsservicos", resposta.descricao()),
                () -> assertEquals(LocalDate.of(2022, 11, 1), resposta.dataInicio()),
                () -> assertNull(resposta.dataFim())
        );

        verify(experienciaProfissionalRepository).findById(experienciaId);
        verifyNoInteractions(tipoDeEmpregoRepository, usuarioAutenticadoService);
    }

    private PerfilCandidato criarPerfilCandidato(Long id, String nomeUsuario) {
        Usuario usuario = new Usuario(
                "12345678909",
                nomeUsuario,
                "83999999999",
                "usuario" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilCandidato perfilCandidato = new PerfilCandidato(usuario);
        ReflectionTestUtils.setField(perfilCandidato, "id", id);
        usuario.adicionarPerfilCandidato(perfilCandidato);

        return perfilCandidato;
    }

    private TipoDeEmprego criarTipoDeEmprego(Long id, String titulo) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(titulo, "Descricao do tipo de emprego");
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }

    private ExperienciaProfissional criarExperiencia(
            Long id,
            PerfilCandidato perfilCandidato,
            TipoDeEmprego tipoDeEmprego,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        ExperienciaProfissional experiencia = new ExperienciaProfissional(
                perfilCandidato,
                tipoDeEmprego,
                descricao,
                dataInicio,
                dataFim
        );
        ReflectionTestUtils.setField(experiencia, "id", id);
        return experiencia;
    }
}
