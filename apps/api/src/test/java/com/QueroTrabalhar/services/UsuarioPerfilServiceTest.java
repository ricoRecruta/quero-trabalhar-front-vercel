package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorUsuarioRequestDTO;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPerfilServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilCandidatoRepository perfilCandidatoRepository;

    @Mock
    private PerfilRecrutadorRepository perfilRecrutadorRepository;

    @Mock
    private OportunidadeDeEmpregoRepository oportunidadeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveAdicionarMeuPerfilRecrutadorComNomeDaEmpresaDoDTO() throws Exception {
        // Arrange
        Usuario usuario = criarUsuario(1L, "Marina Souza", "marina.souza@email.com");
        PerfilRecrutadorUsuarioRequestDTO dto = new PerfilRecrutadorUsuarioRequestDTO("Empresa Ágil");
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);

        // Act
        usuarioService.adicionarMeuPerfilRecrutador(dto);

        // Assert
        assertTrue(usuario.ehRecrutador());
        assertNotNull(usuario.getPerfilRecrutador());
        assertEquals("Empresa Ágil", usuario.getPerfilRecrutador().getEmpresa());

        ArgumentCaptor<PerfilRecrutador> perfilCaptor = ArgumentCaptor.forClass(PerfilRecrutador.class);
        verify(perfilRecrutadorRepository).save(perfilCaptor.capture());
        assertSame(usuario, perfilCaptor.getValue().getUsuario());
        assertEquals("Empresa Ágil", perfilCaptor.getValue().getEmpresa());

        verify(usuarioRepository).save(same(usuario));
    }

    @Test
    void deveAdicionarPerfilRecrutadorPorIdComNomeDaEmpresaDoDTO() throws Exception {
        // Arrange
        Long usuarioId = 2L;
        Usuario usuario = criarUsuario(usuarioId, "João da Silva", "joao.silva@email.com");
        PerfilRecrutadorUsuarioRequestDTO dto = new PerfilRecrutadorUsuarioRequestDTO("Tecnologia São José");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.adicionarPerfilRecrutadorPorId(usuarioId, dto);

        // Assert
        assertTrue(usuario.ehRecrutador());
        assertNotNull(usuario.getPerfilRecrutador());
        assertEquals("Tecnologia São José", usuario.getPerfilRecrutador().getEmpresa());

        ArgumentCaptor<PerfilRecrutador> perfilCaptor = ArgumentCaptor.forClass(PerfilRecrutador.class);
        verify(perfilRecrutadorRepository).save(perfilCaptor.capture());
        assertSame(usuario, perfilCaptor.getValue().getUsuario());
        assertEquals("Tecnologia São José", perfilCaptor.getValue().getEmpresa());

        verify(usuarioRepository).save(same(usuario));
    }

    @Test
    void deveBloquearAdicionarMeuPerfilRecrutadorQuandoUsuarioJaForRecrutador() throws Exception {
        // Arrange
        Usuario usuario = criarUsuario(3L, "Lívia Rocha", "livia.rocha@email.com");
        PerfilRecrutador perfilExistente = new PerfilRecrutador(usuario, "Empresa Atual");
        usuario.adicionarPerfilRecrutador(perfilExistente);
        PerfilRecrutadorUsuarioRequestDTO dto = new PerfilRecrutadorUsuarioRequestDTO("Nova Empresa");
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);

        // Act
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioService.adicionarMeuPerfilRecrutador(dto)
        );

        // Assert
        assertNotNull(exception);
        assertSame(perfilExistente, usuario.getPerfilRecrutador());
        assertEquals("Empresa Atual", usuario.getPerfilRecrutador().getEmpresa());
        verify(perfilRecrutadorRepository, never()).save(org.mockito.ArgumentMatchers.any(PerfilRecrutador.class));
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    @Test
    void deveBloquearAdicionarPerfilRecrutadorPorIdQuandoUsuarioJaForRecrutador() throws Exception {
        // Arrange
        Long usuarioId = 4L;
        Usuario usuario = criarUsuario(usuarioId, "Carlos Eduardo", "carlos.eduardo@email.com");
        PerfilRecrutador perfilExistente = new PerfilRecrutador(usuario, "Empresa Consolidada");
        usuario.adicionarPerfilRecrutador(perfilExistente);
        PerfilRecrutadorUsuarioRequestDTO dto = new PerfilRecrutadorUsuarioRequestDTO("Empresa Nova");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioService.adicionarPerfilRecrutadorPorId(usuarioId, dto)
        );

        // Assert
        assertNotNull(exception);
        assertSame(perfilExistente, usuario.getPerfilRecrutador());
        assertEquals("Empresa Consolidada", usuario.getPerfilRecrutador().getEmpresa());
        verify(perfilRecrutadorRepository, never()).save(org.mockito.ArgumentMatchers.any(PerfilRecrutador.class));
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    private Usuario criarUsuario(Long id, String nome, String email) throws Exception {
        Usuario usuario = new Usuario("12345678909", nome, "83912345678", email, "senhaForte123");
        injetarId(usuario, id);
        return usuario;
    }

    private void injetarId(Usuario usuario, Long id) throws Exception {
        Field field = Usuario.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(usuario, id);
    }
}
