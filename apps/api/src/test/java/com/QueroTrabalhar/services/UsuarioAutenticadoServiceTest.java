package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioAutenticadoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @AfterEach
    void limparSecurityContextHolder() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveObterUsuarioAutenticadoComSucesso() {
        // Arrange
        Usuario usuario = criarUsuario(1L, "Marina Costa", "marina.costa@teste.com");
        autenticar("marina.costa@teste.com");
        when(usuarioRepository.findByEmail("marina.costa@teste.com")).thenReturn(Optional.of(usuario));

        // Act
        Usuario resposta = usuarioAutenticadoService.obterUsuarioAutenticado();

        // Assert
        assertSame(usuario, resposta);
        verify(usuarioRepository).findByEmail("marina.costa@teste.com");
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverAutenticacao() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act / Assert
        assertThrows(
                BusinessRuleException.class,
                () -> usuarioAutenticadoService.obterUsuarioAutenticado()
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveLancarExcecaoQuandoPrincipalNaoForEsperado() {
        // Arrange
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new AnonymousAuthenticationToken(
                "chave-teste",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        ));
        SecurityContextHolder.setContext(context);

        // Act / Assert
        assertThrows(
                BusinessRuleException.class,
                () -> usuarioAutenticadoService.obterUsuarioAutenticado()
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoNaoExistirNoBanco() {
        // Arrange
        autenticar("usuario.inexistente@teste.com");
        when(usuarioRepository.findByEmail("usuario.inexistente@teste.com")).thenReturn(Optional.empty());

        // Act / Assert
        assertThrows(
                ObjectNotFoundException.class,
                () -> usuarioAutenticadoService.obterUsuarioAutenticado()
        );

        verify(usuarioRepository).findByEmail("usuario.inexistente@teste.com");
    }

    @Test
    void deveObterPerfilCandidatoAutenticadoComSucesso() {
        // Arrange
        Usuario usuario = criarUsuario(2L, "Joao Ribeiro", "joao.ribeiro@teste.com");
        PerfilCandidato perfilCandidato = criarPerfilCandidato(usuario, 2L);
        autenticar("joao.ribeiro@teste.com");
        when(usuarioRepository.findByEmail("joao.ribeiro@teste.com")).thenReturn(Optional.of(usuario));

        // Act
        PerfilCandidato resposta = usuarioAutenticadoService.obterPerfilCandidatoAutenticado();

        // Assert
        assertSame(perfilCandidato, resposta);
        verify(usuarioRepository).findByEmail("joao.ribeiro@teste.com");
    }

    @Test
    void deveBloquearObterPerfilCandidatoQuandoUsuarioNaoForCandidato() {
        // Arrange
        Usuario usuario = criarUsuario(3L, "Ana Clara", "ana.clara@teste.com");
        autenticar("ana.clara@teste.com");
        when(usuarioRepository.findByEmail("ana.clara@teste.com")).thenReturn(Optional.of(usuario));

        // Act / Assert
        assertThrows(
                BusinessRuleException.class,
                () -> usuarioAutenticadoService.obterPerfilCandidatoAutenticado()
        );

        verify(usuarioRepository).findByEmail("ana.clara@teste.com");
    }

    @Test
    void deveObterPerfilRecrutadorAutenticadoComSucesso() {
        // Arrange
        Usuario usuario = criarUsuario(4L, "Fernanda Rocha", "fernanda.rocha@teste.com");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(usuario, 4L, "Empresa Atlas");
        autenticar("fernanda.rocha@teste.com");
        when(usuarioRepository.findByEmail("fernanda.rocha@teste.com")).thenReturn(Optional.of(usuario));

        // Act
        PerfilRecrutador resposta = usuarioAutenticadoService.obterPerfilRecrutadorAutenticado();

        // Assert
        assertSame(perfilRecrutador, resposta);
        verify(usuarioRepository).findByEmail("fernanda.rocha@teste.com");
    }

    @Test
    void deveBloquearObterPerfilRecrutadorQuandoUsuarioNaoForRecrutador() {
        // Arrange
        Usuario usuario = criarUsuario(5L, "Lucas Martins", "lucas.martins@teste.com");
        autenticar("lucas.martins@teste.com");
        when(usuarioRepository.findByEmail("lucas.martins@teste.com")).thenReturn(Optional.of(usuario));

        // Act / Assert
        assertThrows(
                BusinessRuleException.class,
                () -> usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()
        );

        verify(usuarioRepository).findByEmail("lucas.martins@teste.com");
    }

    private void autenticar(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(email, "senha", Collections.emptyList())
        );
        SecurityContextHolder.setContext(context);
    }

    private Usuario criarUsuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario("12345678909", nome, "83999999999", email, "Senha@123");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private PerfilCandidato criarPerfilCandidato(Usuario usuario, Long id) {
        PerfilCandidato perfilCandidato = new PerfilCandidato(usuario);
        ReflectionTestUtils.setField(perfilCandidato, "id", id);
        usuario.adicionarPerfilCandidato(perfilCandidato);
        return perfilCandidato;
    }

    private PerfilRecrutador criarPerfilRecrutador(Usuario usuario, Long id, String empresa) {
        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(usuario, empresa);
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);
        return perfilRecrutador;
    }
}
