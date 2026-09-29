package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.indicacao.IndicacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.indicacao.IndicacaoResponseDTO;
import com.QueroTrabalhar.domain.entity.Indicacao;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.IndicacaoRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicacaoServiceTest {

    @Mock
    private IndicacaoRepository indicacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private IndicacaoService indicacaoService;

    @Test
    void deveCriarIndicacaoComUsuarioAutenticadoComoAutor() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");
        Usuario usuarioIndicado = criarUsuario(2L, "María Fernanda", "maria@teste.com");
        IndicacaoRequestDTO dto = new IndicacaoRequestDTO(
                usuarioIndicado.getId(),
                "  Excelente profissional em Java 21.  "
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(usuarioRepository.findById(usuarioIndicado.getId())).thenReturn(Optional.of(usuarioIndicado));
        when(indicacaoRepository.save(any(Indicacao.class))).thenAnswer(invocation -> {
            Indicacao indicacaoSalva = invocation.getArgument(0);
            ReflectionTestUtils.setField(indicacaoSalva, "id", 10L);
            return indicacaoSalva;
        });

        // Act
        IndicacaoResponseDTO resposta = indicacaoService.criarIndicacao(dto);

        // Assert
        ArgumentCaptor<Indicacao> indicacaoCaptor = ArgumentCaptor.forClass(Indicacao.class);
        verify(indicacaoRepository).save(indicacaoCaptor.capture());
        Indicacao indicacaoSalva = indicacaoCaptor.getValue();

        assertAll(
                () -> assertEquals(10L, resposta.id()),
                () -> assertEquals(autorAutenticado.getId(), resposta.autorId()),
                () -> assertEquals("João da Silva", resposta.autorNome()),
                () -> assertEquals(usuarioIndicado.getId(), resposta.usuarioIndicadoId()),
                () -> assertEquals("María Fernanda", resposta.usuarioIndicadoNome()),
                () -> assertEquals("Excelente profissional em Java 21.", resposta.mensagem()),
                () -> assertSame(autorAutenticado, indicacaoSalva.getAutor()),
                () -> assertSame(usuarioIndicado, indicacaoSalva.getUsuarioIndicado()),
                () -> assertEquals("Excelente profissional em Java 21.", indicacaoSalva.getMensagem())
        );
    }

    @Test
    void deveBloquearAutoindicacao() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");
        IndicacaoRequestDTO dto = new IndicacaoRequestDTO(
                autorAutenticado.getId(),
                "Você deveria se contratar."
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(usuarioRepository.findById(autorAutenticado.getId())).thenReturn(Optional.of(autorAutenticado));

        // Act / Assert
        assertThrows(BusinessRuleException.class, () -> indicacaoService.criarIndicacao(dto));

        verify(indicacaoRepository, never()).save(any(Indicacao.class));
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoUsuarioIndicadoNaoExistir() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");
        IndicacaoRequestDTO dto = new IndicacaoRequestDTO(99L, "Pessoa muito dedicada.");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThrows(ObjectNotFoundException.class, () -> indicacaoService.criarIndicacao(dto));

        verify(indicacaoRepository, never()).save(any(Indicacao.class));
    }

    @Test
    void deveListarIndicacoesDadasPeloUsuarioAutenticado() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");
        Usuario usuarioIndicado1 = criarUsuario(2L, "Ana Cláudia", "ana@teste.com");
        Usuario usuarioIndicado2 = criarUsuario(3L, "Érico Souza", "erico@teste.com");
        Indicacao indicacao1 = criarIndicacao(10L, autorAutenticado, usuarioIndicado1, "Perfil técnico sólido.");
        Indicacao indicacao2 = criarIndicacao(11L, autorAutenticado, usuarioIndicado2, "Ótima comunicação.");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(indicacaoRepository.findByAutorId(autorAutenticado.getId())).thenReturn(List.of(indicacao1, indicacao2));

        // Act
        List<IndicacaoResponseDTO> resposta = indicacaoService.listarIndicacoesDadasPeloUsuarioAutenticado();

        // Assert
        assertEquals(2, resposta.size());
        assertAll(
                () -> assertEquals(10L, resposta.get(0).id()),
                () -> assertEquals(1L, resposta.get(0).autorId()),
                () -> assertEquals("João da Silva", resposta.get(0).autorNome()),
                () -> assertEquals(2L, resposta.get(0).usuarioIndicadoId()),
                () -> assertEquals("Ana Cláudia", resposta.get(0).usuarioIndicadoNome()),
                () -> assertEquals("Perfil técnico sólido.", resposta.get(0).mensagem()),
                () -> assertEquals(11L, resposta.get(1).id()),
                () -> assertEquals(3L, resposta.get(1).usuarioIndicadoId()),
                () -> assertEquals("Érico Souza", resposta.get(1).usuarioIndicadoNome()),
                () -> assertEquals("Ótima comunicação.", resposta.get(1).mensagem())
        );
        verify(indicacaoRepository).findByAutorId(autorAutenticado.getId());
    }

    @Test
    void deveListarIndicacoesRecebidasPeloUsuarioAutenticado() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuario(2L, "Ana Cláudia", "ana@teste.com");
        Usuario autor1 = criarUsuario(1L, "João da Silva", "joao@teste.com");
        Usuario autor2 = criarUsuario(3L, "Marcos Vinícius", "marcos@teste.com");
        Indicacao indicacao1 = criarIndicacao(20L, autor1, usuarioAutenticado, "Excelente colaboração.");
        Indicacao indicacao2 = criarIndicacao(21L, autor2, usuarioAutenticado, "Entrega com qualidade.");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);
        when(indicacaoRepository.findByUsuarioIndicadoId(usuarioAutenticado.getId()))
                .thenReturn(List.of(indicacao1, indicacao2));

        // Act
        List<IndicacaoResponseDTO> resposta = indicacaoService.listarIndicacoesRecebidasPeloUsuarioAutenticado();

        // Assert
        assertEquals(2, resposta.size());
        assertAll(
                () -> assertEquals(20L, resposta.get(0).id()),
                () -> assertEquals(1L, resposta.get(0).autorId()),
                () -> assertEquals("João da Silva", resposta.get(0).autorNome()),
                () -> assertEquals(2L, resposta.get(0).usuarioIndicadoId()),
                () -> assertEquals("Ana Cláudia", resposta.get(0).usuarioIndicadoNome()),
                () -> assertEquals("Excelente colaboração.", resposta.get(0).mensagem()),
                () -> assertEquals(21L, resposta.get(1).id()),
                () -> assertEquals(3L, resposta.get(1).autorId()),
                () -> assertEquals("Marcos Vinícius", resposta.get(1).autorNome()),
                () -> assertEquals("Entrega com qualidade.", resposta.get(1).mensagem())
        );
        verify(indicacaoRepository).findByUsuarioIndicadoId(usuarioAutenticado.getId());
    }

    @Test
    void deveExcluirIndicacaoCriadaPeloUsuarioAutenticado() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");
        Usuario usuarioIndicado = criarUsuario(2L, "Ana Cláudia", "ana@teste.com");
        Indicacao indicacao = criarIndicacao(30L, autorAutenticado, usuarioIndicado, "Forte capacidade analítica.");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(indicacaoRepository.findById(30L)).thenReturn(Optional.of(indicacao));

        // Act
        indicacaoService.excluirIndicacaoDoUsuarioAutenticado(30L);

        // Assert
        verify(indicacaoRepository).delete(indicacao);
    }

    @Test
    void deveBloquearExclusaoDeIndicacaoCriadaPorOutroUsuario() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");
        Usuario outroAutor = criarUsuario(3L, "Paulo Henrique", "paulo@teste.com");
        Usuario usuarioIndicado = criarUsuario(2L, "Ana Cláudia", "ana@teste.com");
        Indicacao indicacao = criarIndicacao(31L, outroAutor, usuarioIndicado, "Muito organizado.");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(indicacaoRepository.findById(31L)).thenReturn(Optional.of(indicacao));

        // Act / Assert
        assertThrows(
                BusinessRuleException.class,
                () -> indicacaoService.excluirIndicacaoDoUsuarioAutenticado(31L)
        );

        verify(indicacaoRepository, never()).delete(any(Indicacao.class));
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoIndicacaoNaoExistirAoExcluir() {
        // Arrange
        Usuario autorAutenticado = criarUsuario(1L, "João da Silva", "joao@teste.com");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(autorAutenticado);
        when(indicacaoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThrows(
                ObjectNotFoundException.class,
                () -> indicacaoService.excluirIndicacaoDoUsuarioAutenticado(99L)
        );

        verify(indicacaoRepository, never()).delete(any(Indicacao.class));
    }

    private Usuario criarUsuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario(
                "12345678909",
                nome,
                "83999999999",
                email,
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Indicacao criarIndicacao(Long id, Usuario autor, Usuario usuarioIndicado, String mensagem) {
        Indicacao indicacao = new Indicacao(autor, usuarioIndicado, mensagem);
        ReflectionTestUtils.setField(indicacao, "id", id);
        autor.adicionarIndicacaoDada(indicacao);
        usuarioIndicado.adicionarIndicacaoRecebida(indicacao);
        return indicacao;
    }
}
