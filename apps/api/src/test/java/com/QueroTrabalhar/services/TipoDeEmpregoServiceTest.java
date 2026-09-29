package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.services.exceptions.DataIntegrityViolationException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoDeEmpregoServiceTest {

    @Mock
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @InjectMocks
    private TipoDeEmpregoService tipoDeEmpregoService;

    @Test
    void deveCriarTipoDeEmpregoComoAdminComSucesso() {
        // Arrange
        TipoDeEmpregoRequestDTO request = new TipoDeEmpregoRequestDTO(
                "Backend Senior",
                "Atuacao com APIs e arquitetura de servicos"
        );

        when(tipoDeEmpregoRepository.existsByTitulo(request.titulo())).thenReturn(false);
        when(tipoDeEmpregoRepository.save(any(TipoDeEmprego.class))).thenAnswer(invocation -> {
            TipoDeEmprego tipoSalvo = invocation.getArgument(0);
            ReflectionTestUtils.setField(tipoSalvo, "id", 1L);
            return tipoSalvo;
        });

        // Act
        TipoDeEmpregoResponseDTO resposta = tipoDeEmpregoService.criarNoCatalogo(request);

        // Assert
        ArgumentCaptor<TipoDeEmprego> tipoCaptor = ArgumentCaptor.forClass(TipoDeEmprego.class);
        verify(tipoDeEmpregoRepository).existsByTitulo(request.titulo());
        verify(tipoDeEmpregoRepository).save(tipoCaptor.capture());

        TipoDeEmprego tipoSalvo = tipoCaptor.getValue();
        assertAll(
                () -> assertEquals(1L, resposta.id()),
                () -> assertEquals(request.titulo(), resposta.titulo()),
                () -> assertEquals(request.descricao(), resposta.descricao()),
                () -> assertTrue(resposta.aprovado()),
                () -> assertEquals(1L, tipoSalvo.getId()),
                () -> assertEquals(request.titulo(), tipoSalvo.getTitulo()),
                () -> assertEquals(request.descricao(), tipoSalvo.getDescricao()),
                () -> assertTrue(tipoSalvo.isAprovado())
        );
    }

    @Test
    void deveBloquearCriacaoAdminQuandoTituloJaExistir() {
        // Arrange
        TipoDeEmpregoRequestDTO request = new TipoDeEmpregoRequestDTO(
                "Backend Senior",
                "Descricao duplicada"
        );

        when(tipoDeEmpregoRepository.existsByTitulo(request.titulo())).thenReturn(true);

        // Act
        assertThrows(
                DataIntegrityViolationException.class,
                () -> tipoDeEmpregoService.criarNoCatalogo(request)
        );

        // Assert
        verify(tipoDeEmpregoRepository).existsByTitulo(request.titulo());
        verify(tipoDeEmpregoRepository, never()).save(any(TipoDeEmprego.class));
    }

    @Test
    void deveSugerirTipoDeEmpregoComSucesso() {
        // Arrange
        TipoDeEmpregoRequestDTO request = new TipoDeEmpregoRequestDTO(
                "Engenheiro de Plataforma",
                "Construcao de pipelines e observabilidade"
        );

        when(tipoDeEmpregoRepository.existsByTitulo(request.titulo())).thenReturn(false);
        when(tipoDeEmpregoRepository.save(any(TipoDeEmprego.class))).thenAnswer(invocation -> {
            TipoDeEmprego tipoSalvo = invocation.getArgument(0);
            ReflectionTestUtils.setField(tipoSalvo, "id", 2L);
            return tipoSalvo;
        });

        // Act
        TipoDeEmpregoResponseDTO resposta = tipoDeEmpregoService.sugerirNoCatalogo(request);

        // Assert
        ArgumentCaptor<TipoDeEmprego> tipoCaptor = ArgumentCaptor.forClass(TipoDeEmprego.class);
        verify(tipoDeEmpregoRepository).existsByTitulo(request.titulo());
        verify(tipoDeEmpregoRepository).save(tipoCaptor.capture());

        TipoDeEmprego tipoSalvo = tipoCaptor.getValue();
        assertAll(
                () -> assertEquals(2L, resposta.id()),
                () -> assertEquals(request.titulo(), resposta.titulo()),
                () -> assertEquals(request.descricao(), resposta.descricao()),
                () -> assertFalse(resposta.aprovado()),
                () -> assertEquals(2L, tipoSalvo.getId()),
                () -> assertEquals(request.titulo(), tipoSalvo.getTitulo()),
                () -> assertEquals(request.descricao(), tipoSalvo.getDescricao()),
                () -> assertFalse(tipoSalvo.isAprovado())
        );
    }

    @Test
    void deveBloquearSugestaoQuandoTituloJaExistir() {
        // Arrange
        TipoDeEmpregoRequestDTO request = new TipoDeEmpregoRequestDTO(
                "Engenheiro de Plataforma",
                "Descricao duplicada"
        );

        when(tipoDeEmpregoRepository.existsByTitulo(request.titulo())).thenReturn(true);

        // Act
        assertThrows(
                DataIntegrityViolationException.class,
                () -> tipoDeEmpregoService.sugerirNoCatalogo(request)
        );

        // Assert
        verify(tipoDeEmpregoRepository).existsByTitulo(request.titulo());
        verify(tipoDeEmpregoRepository, never()).save(any(TipoDeEmprego.class));
    }

    @Test
    void deveListarAprovados() {
        // Arrange
        TipoDeEmprego primeiroTipo = criarTipoDeEmpregoAprovado(10L, "Backend");
        TipoDeEmprego segundoTipo = criarTipoDeEmpregoAprovado(11L, "SRE");

        when(tipoDeEmpregoRepository.findByAprovadoTrue()).thenReturn(List.of(primeiroTipo, segundoTipo));

        // Act
        List<TipoDeEmpregoResponseDTO> resposta = tipoDeEmpregoService.listarAprovados();

        // Assert
        assertEquals(2, resposta.size());
        assertInstanceOf(TipoDeEmpregoResponseDTO.class, resposta.get(0));
        assertInstanceOf(TipoDeEmpregoResponseDTO.class, resposta.get(1));
        assertNotSame(primeiroTipo, resposta.get(0));
        assertNotSame(segundoTipo, resposta.get(1));
        assertAll(
                () -> assertEquals(10L, resposta.get(0).id()),
                () -> assertEquals("Backend", resposta.get(0).titulo()),
                () -> assertTrue(resposta.get(0).aprovado()),
                () -> assertEquals(11L, resposta.get(1).id()),
                () -> assertEquals("SRE", resposta.get(1).titulo()),
                () -> assertTrue(resposta.get(1).aprovado())
        );

        verify(tipoDeEmpregoRepository).findByAprovadoTrue();
    }

    @Test
    void deveListarNaoAprovadosPaginadosComFiltros() {
        // Arrange
        TipoDeEmprego primeiroTipo = criarTipoDeEmpregoPendente(20L, "DevRel");
        TipoDeEmprego segundoTipo = criarTipoDeEmpregoPendente(21L, "QA Automation");
        TipoDeEmpregoFilterDTO filtro = new TipoDeEmpregoFilterDTO("  automation  ");
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        when(tipoDeEmpregoRepository.findAll(any(Specification.class), same(pageable)))
                .thenReturn(new PageImpl<>(List.of(primeiroTipo, segundoTipo), pageable, 2));

        // Act
        Page<TipoDeEmpregoResponseDTO> resposta = tipoDeEmpregoService.listarNaoAprovados(filtro, pageable);

        // Assert
        assertNotNull(resposta);
        assertEquals(2, resposta.getTotalElements());
        assertEquals(2, resposta.getContent().size());
        assertInstanceOf(TipoDeEmpregoResponseDTO.class, resposta.getContent().get(0));
        assertInstanceOf(TipoDeEmpregoResponseDTO.class, resposta.getContent().get(1));
        assertNotSame(primeiroTipo, resposta.getContent().get(0));
        assertNotSame(segundoTipo, resposta.getContent().get(1));

        TipoDeEmpregoResponseDTO primeiroDto = resposta.getContent().get(0);
        TipoDeEmpregoResponseDTO segundoDto = resposta.getContent().get(1);
        assertAll(
                () -> assertEquals(20L, primeiroDto.id()),
                () -> assertEquals("DevRel", primeiroDto.titulo()),
                () -> assertFalse(primeiroDto.aprovado()),
                () -> assertEquals(21L, segundoDto.id()),
                () -> assertEquals("QA Automation", segundoDto.titulo()),
                () -> assertFalse(segundoDto.aprovado())
        );

        verify(tipoDeEmpregoRepository).findAll(any(Specification.class), same(pageable));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        // Arrange
        Long id = 30L;
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoPendente(id, "Tech Lead");

        when(tipoDeEmpregoRepository.findById(id)).thenReturn(Optional.of(tipoDeEmprego));

        // Act
        TipoDeEmpregoResponseDTO resposta = tipoDeEmpregoService.buscarPorId(id);

        // Assert
        assertNotSame(tipoDeEmprego, resposta);
        assertAll(
                () -> assertEquals(id, resposta.id()),
                () -> assertEquals("Tech Lead", resposta.titulo()),
                () -> assertFalse(resposta.aprovado())
        );

        verify(tipoDeEmpregoRepository).findById(id);
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoBuscarPorIdInexistente() {
        // Arrange
        Long id = 31L;
        when(tipoDeEmpregoRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        assertThrows(
                ObjectNotFoundException.class,
                () -> tipoDeEmpregoService.buscarPorId(id)
        );

        // Assert
        verify(tipoDeEmpregoRepository).findById(id);
    }

    @Test
    void deveBuscarAprovadoPorIdComSucesso() {
        // Arrange
        Long id = 40L;
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoAprovado(id, "Arquiteto de Software");

        when(tipoDeEmpregoRepository.findById(id)).thenReturn(Optional.of(tipoDeEmprego));

        // Act
        TipoDeEmpregoResponseDTO resposta = tipoDeEmpregoService.buscarAprovadoPorId(id);

        // Assert
        assertAll(
                () -> assertEquals(id, resposta.id()),
                () -> assertEquals("Arquiteto de Software", resposta.titulo()),
                () -> assertTrue(resposta.aprovado())
        );

        verify(tipoDeEmpregoRepository).findById(id);
    }

    @Test
    void deveOcultarTipoNaoAprovadoNaBuscaPublicaPorId() {
        // Arrange
        Long id = 41L;
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmpregoPendente(id, "Especialista em Dados");

        when(tipoDeEmpregoRepository.findById(id)).thenReturn(Optional.of(tipoDeEmprego));

        // Act
        assertThrows(
                ObjectNotFoundException.class,
                () -> tipoDeEmpregoService.buscarAprovadoPorId(id)
        );

        // Assert
        verify(tipoDeEmpregoRepository).findById(id);
    }

    @Test
    void deveAprovarSugestaoComSucesso() {
        // Arrange
        Long id = 50L;
        TipoDeEmprego tipoPendente = criarTipoDeEmpregoPendente(id, "Back-end");

        when(tipoDeEmpregoRepository.findById(id)).thenReturn(Optional.of(tipoPendente));
        when(tipoDeEmpregoRepository.save(any(TipoDeEmprego.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TipoDeEmpregoResponseDTO resposta = tipoDeEmpregoService.aprovarSugestao(
                id,
                "Backend",
                "Atuacao com microsservicos e mensageria"
        );

        // Assert
        ArgumentCaptor<TipoDeEmprego> tipoCaptor = ArgumentCaptor.forClass(TipoDeEmprego.class);
        verify(tipoDeEmpregoRepository).findById(id);
        verify(tipoDeEmpregoRepository, times(2)).save(tipoCaptor.capture());

        List<TipoDeEmprego> saves = tipoCaptor.getAllValues();
        assertEquals(2, saves.size());
        assertSame(tipoPendente, saves.get(0));
        assertSame(tipoPendente, saves.get(1));
        assertAll(
                () -> assertEquals(id, resposta.id()),
                () -> assertEquals("Backend", resposta.titulo()),
                () -> assertEquals("Atuacao com microsservicos e mensageria", resposta.descricao()),
                () -> assertTrue(resposta.aprovado()),
                () -> assertEquals("Backend", tipoPendente.getTitulo()),
                () -> assertEquals("Atuacao com microsservicos e mensageria", tipoPendente.getDescricao()),
                () -> assertTrue(tipoPendente.isAprovado())
        );
    }

    @Test
    void deveAprovarSugestaoJaAprovadaSemQuebrarConformeContratoAtual() {
        // Arrange
        Long id = 51L;
        TipoDeEmprego tipoJaAprovado = criarTipoDeEmpregoAprovado(id, "Platform Engineer");

        when(tipoDeEmpregoRepository.findById(id)).thenReturn(Optional.of(tipoJaAprovado));
        when(tipoDeEmpregoRepository.save(any(TipoDeEmprego.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TipoDeEmpregoResponseDTO resposta = tipoDeEmpregoService.aprovarSugestao(
                id,
                "Platform Engineer Senior",
                "Operacao de plataforma interna"
        );

        // Assert
        verify(tipoDeEmpregoRepository).findById(id);
        verify(tipoDeEmpregoRepository, times(2)).save(tipoJaAprovado);
        assertAll(
                () -> assertEquals(id, resposta.id()),
                () -> assertEquals("Platform Engineer Senior", resposta.titulo()),
                () -> assertEquals("Operacao de plataforma interna", resposta.descricao()),
                () -> assertTrue(resposta.aprovado()),
                () -> assertTrue(tipoJaAprovado.isAprovado())
        );
    }

    @Test
    void deveAprovarSugestoesEmLote() {
        // Arrange
        List<Long> ids = List.of(60L, 61L, 62L);
        when(tipoDeEmpregoRepository.aprovarEmLote(ids)).thenReturn(3);

        // Act
        assertDoesNotThrow(() -> tipoDeEmpregoService.aprovarEmLote(ids));

        // Assert
        verify(tipoDeEmpregoRepository).aprovarEmLote(ids);
    }

    @Test
    void deveLancarDataIntegrityViolationExceptionQuandoAprovarEmLoteComListaVazia() {
        // Arrange
        List<Long> ids = List.of();

        // Act
        assertThrows(
                DataIntegrityViolationException.class,
                () -> tipoDeEmpregoService.aprovarEmLote(ids)
        );

        // Assert
        verify(tipoDeEmpregoRepository, never()).aprovarEmLote(any());
    }

    @Test
    void deveLancarDataIntegrityViolationExceptionQuandoAprovarEmLoteNaoAtualizarRegistros() {
        // Arrange
        List<Long> ids = List.of(63L, 64L);
        when(tipoDeEmpregoRepository.aprovarEmLote(ids)).thenReturn(0);

        // Act
        assertThrows(
                DataIntegrityViolationException.class,
                () -> tipoDeEmpregoService.aprovarEmLote(ids)
        );

        // Assert
        verify(tipoDeEmpregoRepository).aprovarEmLote(ids);
    }

    @Test
    void deveDeletarTipoDeEmpregoComSucesso() {
        // Arrange
        Long id = 70L;
        when(tipoDeEmpregoRepository.existsById(id)).thenReturn(true);

        // Act
        tipoDeEmpregoService.deletar(id);

        // Assert
        verify(tipoDeEmpregoRepository).existsById(id);
        verify(tipoDeEmpregoRepository).deleteById(id);
    }

    @Test
    void deveLancarObjectNotFoundExceptionAoDeletarTipoInexistente() {
        // Arrange
        Long id = 71L;
        when(tipoDeEmpregoRepository.existsById(id)).thenReturn(false);

        // Act
        assertThrows(
                ObjectNotFoundException.class,
                () -> tipoDeEmpregoService.deletar(id)
        );

        // Assert
        verify(tipoDeEmpregoRepository).existsById(id);
        verify(tipoDeEmpregoRepository, never()).deleteById(id);
    }

    @Test
    void deveLancarDataIntegrityViolationExceptionQuandoDeleteFalharPorUso() {
        // Arrange
        Long id = 72L;
        when(tipoDeEmpregoRepository.existsById(id)).thenReturn(true);
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("em uso"))
                .when(tipoDeEmpregoRepository)
                .deleteById(id);

        // Act
        assertThrows(
                DataIntegrityViolationException.class,
                () -> tipoDeEmpregoService.deletar(id)
        );

        // Assert
        verify(tipoDeEmpregoRepository).existsById(id);
        verify(tipoDeEmpregoRepository).deleteById(id);
    }

    private TipoDeEmprego criarTipoDeEmpregoAprovado(Long id, String titulo) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(
                titulo,
                "Descricao do tipo de emprego"
        );
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }

    private TipoDeEmprego criarTipoDeEmpregoPendente(Long id, String titulo) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoSugeridoPeloUsuario(
                titulo,
                "Descricao do tipo de emprego"
        );
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }
}
