package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.localidade.CidadeResponseDTO;
import com.QueroTrabalhar.domain.dtos.localidade.EstadoResponseDTO;
import com.QueroTrabalhar.domain.dtos.localidade.PaisResponseDTO;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalidadeServiceTest {

    @Mock
    private PaisRepository paisRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private CidadeRepository cidadeRepository;

    @InjectMocks
    private LocalidadeService localidadeService;

    @Test
    void deveRetornarPaisesExistentesLocalmente() {
        String termoBusca = "bra";
        Pais brasil = criarPais(1L, "Brasil", "BR");
        Pais brunei = criarPais(2L, "Brunei", "BN");
        when(paisRepository.findByNomeContainingIgnoreCase(termoBusca)).thenReturn(List.of(brasil, brunei));

        List<PaisResponseDTO> resultado = localidadeService.buscarPais(termoBusca);

        assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals(1L, resultado.getFirst().id()),
                () -> assertEquals("Brasil", resultado.getFirst().nome()),
                () -> assertEquals("BR", resultado.getFirst().sigla()),
                () -> assertEquals(2L, resultado.get(1).id()),
                () -> assertEquals("Brunei", resultado.get(1).nome()),
                () -> assertEquals("BN", resultado.get(1).sigla())
        );
        verify(paisRepository).findByNomeContainingIgnoreCase(termoBusca);
        verifyNoInteractions(estadoRepository, cidadeRepository);
    }

    @Test
    void deveRetornarEstadosExistentesPorPais() {
        Long paisId = 1L;
        String termoBusca = "per";
        Pais brasil = criarPais(paisId, "Brasil", "BR");
        Estado pernambuco = criarEstado(10L, "Pernambuco", "PE", brasil);
        when(paisRepository.findById(paisId)).thenReturn(Optional.of(brasil));
        when(estadoRepository.findByPaisAndNomeContainingIgnoreCase(brasil, termoBusca))
                .thenReturn(List.of(pernambuco));

        List<EstadoResponseDTO> resultado = localidadeService.buscarEstado(paisId, termoBusca);

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(10L, resultado.getFirst().id()),
                () -> assertEquals("Pernambuco", resultado.getFirst().nome()),
                () -> assertEquals("PE", resultado.getFirst().sigla()),
                () -> assertEquals(paisId, resultado.getFirst().pais_id())
        );
        verify(paisRepository).findById(paisId);
        verify(estadoRepository).findByPaisAndNomeContainingIgnoreCase(brasil, termoBusca);
        verifyNoInteractions(cidadeRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremEstadosNoCatalogoDoPais() {
        Long paisId = 1L;
        String termoBusca = "acre";
        Pais brasil = criarPais(paisId, "Brasil", "BR");
        when(paisRepository.findById(paisId)).thenReturn(Optional.of(brasil));
        when(estadoRepository.findByPaisAndNomeContainingIgnoreCase(brasil, termoBusca)).thenReturn(List.of());

        List<EstadoResponseDTO> resultado = localidadeService.buscarEstado(paisId, termoBusca);

        assertTrue(resultado.isEmpty());
        verify(paisRepository).findById(paisId);
        verify(estadoRepository).findByPaisAndNomeContainingIgnoreCase(brasil, termoBusca);
        verifyNoInteractions(cidadeRepository);
    }

    @Test
    void deveLancarExcecaoQuandoPaisNaoExistirAoBuscarEstado() {
        Long paisId = 99L;
        when(paisRepository.findById(paisId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> localidadeService.buscarEstado(paisId, "per")
        );

        assertNotNull(exception);
        verify(paisRepository).findById(paisId);
        verifyNoInteractions(estadoRepository, cidadeRepository);
    }

    @Test
    void deveRetornarCidadesExistentesPorEstado() {
        Long estadoId = 10L;
        String termoBusca = "rec";
        Pais brasil = criarPais(1L, "Brasil", "BR");
        Estado pernambuco = criarEstado(estadoId, "Pernambuco", "PE", brasil);
        Cidade recife = criarCidade(100L, "Recife", pernambuco);
        when(estadoRepository.findById(estadoId)).thenReturn(Optional.of(pernambuco));
        when(cidadeRepository.findByEstadoAndNomeContainingIgnoreCase(pernambuco, termoBusca))
                .thenReturn(List.of(recife));

        List<CidadeResponseDTO> resultado = localidadeService.buscarCidade(estadoId, termoBusca);

        assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(100L, resultado.getFirst().id()),
                () -> assertEquals("Recife", resultado.getFirst().nome()),
                () -> assertEquals(estadoId, resultado.getFirst().estado())
        );
        verify(estadoRepository).findById(estadoId);
        verify(cidadeRepository).findByEstadoAndNomeContainingIgnoreCase(pernambuco, termoBusca);
        verifyNoInteractions(paisRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCidadesNoCatalogoDoEstado() {
        Long estadoId = 10L;
        String termoBusca = "lugar-inexistente";
        Pais brasil = criarPais(1L, "Brasil", "BR");
        Estado pernambuco = criarEstado(estadoId, "Pernambuco", "PE", brasil);
        when(estadoRepository.findById(estadoId)).thenReturn(Optional.of(pernambuco));
        when(cidadeRepository.findByEstadoAndNomeContainingIgnoreCase(pernambuco, termoBusca)).thenReturn(List.of());

        List<CidadeResponseDTO> resultado = localidadeService.buscarCidade(estadoId, termoBusca);

        assertTrue(resultado.isEmpty());
        verify(estadoRepository).findById(estadoId);
        verify(cidadeRepository).findByEstadoAndNomeContainingIgnoreCase(pernambuco, termoBusca);
        verifyNoInteractions(paisRepository);
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoBuscarCidade() {
        Long estadoId = 99L;
        when(estadoRepository.findById(estadoId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> localidadeService.buscarCidade(estadoId, "rec")
        );

        assertNotNull(exception);
        verify(estadoRepository).findById(estadoId);
        verifyNoInteractions(cidadeRepository, paisRepository);
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
}
