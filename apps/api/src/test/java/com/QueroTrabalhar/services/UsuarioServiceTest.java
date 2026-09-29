package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.usuario.UsuarioFilterDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioRequestDTO;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioResponseDTO;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.DataIntegrityViolationException;
import com.QueroTrabalhar.services.exceptions.DuplicateResourceException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

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
    void deveListarUsuariosPaginadosComFiltros() {
        Usuario primeiroUsuario = criarUsuario(1L, "Amanda Souza", "amanda.souza@teste.com");
        Usuario segundoUsuario = criarUsuario(2L, "Bruno Lima", "bruno.lima@teste.com");
        UsuarioFilterDTO filtro = new UsuarioFilterDTO("  amanda  ", null, null, null, true, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "nome"));

        when(usuarioRepository.findAll(any(Specification.class), same(pageable)))
                .thenReturn(new PageImpl<>(java.util.List.of(primeiroUsuario, segundoUsuario), pageable, 2));

        Page<UsuarioResponseDTO> resposta = usuarioService.listarUsuarios(filtro, pageable);

        assertEquals(2, resposta.getTotalElements());
        assertEquals(2, resposta.getContent().size());
        assertInstanceOf(UsuarioResponseDTO.class, resposta.getContent().get(0));
        assertNotSame(primeiroUsuario, resposta.getContent().get(0));

        UsuarioResponseDTO primeiroUsuarioResponse = resposta.getContent().get(0);
        UsuarioResponseDTO segundoUsuarioResponse = resposta.getContent().get(1);

        assertAll(
                () -> assertEquals(1L, primeiroUsuarioResponse.id()),
                () -> assertEquals("Amanda Souza", primeiroUsuarioResponse.nome()),
                () -> assertEquals("83999990000", primeiroUsuarioResponse.telefone()),
                () -> assertEquals("amanda.souza@teste.com", primeiroUsuarioResponse.email()),
                () -> assertEquals(2L, segundoUsuarioResponse.id()),
                () -> assertEquals("Bruno Lima", segundoUsuarioResponse.nome()),
                () -> assertEquals("bruno.lima@teste.com", segundoUsuarioResponse.email())
        );
        verify(usuarioRepository).findAll(any(Specification.class), same(pageable));
        verifyNoInteractions(
                perfilCandidatoRepository,
                perfilRecrutadorRepository,
                oportunidadeRepository,
                passwordEncoder,
                usuarioAutenticadoService
        );
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "12345678909",
                "Fernanda Costa",
                "83999998888",
                "fernanda.costa@teste.com",
                "Senha@123"
        );
        String senhaCodificada = "senha-codificada";

        when(usuarioRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.senha())).thenReturn(senhaCodificada);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioSalvo = invocation.getArgument(0);
            ReflectionTestUtils.setField(usuarioSalvo, "id", 10L);
            return usuarioSalvo;
        });

        // Act
        UsuarioResponseDTO resposta = usuarioService.cadastrarUsuario(request);

        // Assert
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

        assertAll(
                () -> assertNotNull(resposta),
                () -> assertEquals(10L, resposta.id()),
                () -> assertEquals(request.nome(), resposta.nome()),
                () -> assertEquals(request.telefone(), resposta.telefone()),
                () -> assertEquals(request.email(), resposta.email())
        );

        verify(passwordEncoder).encode(request.senha());
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertAll(
                () -> assertEquals(request.cpf(), usuarioSalvo.getCpf()),
                () -> assertEquals(request.nome(), usuarioSalvo.getNome()),
                () -> assertEquals(request.telefone(), usuarioSalvo.getTelefone()),
                () -> assertEquals(request.email(), usuarioSalvo.getEmail()),
                () -> assertEquals(senhaCodificada, usuarioSalvo.getSenha()),
                () -> assertTrue(usuarioSalvo.ehCandidato()),
                () -> assertNotNull(usuarioSalvo.getPerfilCandidato())
        );
        verifyNoInteractions(usuarioAutenticadoService, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearCadastroQuandoCpfJaExistir() {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "12345678909",
                "Fernanda Costa",
                "83999998888",
                "fernanda.costa@teste.com",
                "Senha@123"
        );
        when(usuarioRepository.existsByCpf(request.cpf())).thenReturn(true);

        // Act
        assertThrows(DataIntegrityViolationException.class, () -> usuarioService.cadastrarUsuario(request));

        // Assert
        verify(usuarioRepository).existsByCpf(request.cpf());
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearCadastroQuandoEmailJaExistir() {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "12345678909",
                "Fernanda Costa",
                "83999998888",
                "fernanda.costa@teste.com",
                "Senha@123"
        );
        Usuario usuarioExistente = criarUsuario(20L, "Usuario Existente", request.email());

        when(usuarioRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(usuarioRepository.findByEmail(request.email())).thenReturn(Optional.of(usuarioExistente));

        // Act
        assertThrows(DuplicateResourceException.class, () -> usuarioService.cadastrarUsuario(request));

        // Assert
        verify(usuarioRepository).existsByCpf(request.cpf());
        verify(usuarioRepository).findByEmail(request.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveDeletarUsuarioPorIdComSucesso() {
        // Arrange
        Long usuarioId = 30L;
        Usuario usuario = criarUsuario(usuarioId, "Marina Souza", "marina.souza@teste.com");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.deletarUsuarioPorId(usuarioId);

        // Assert
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioRepository).delete(same(usuario));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveLancarObjectNotFoundExceptionAoDeletarUsuarioPorIdInexistente() {
        // Arrange
        Long usuarioId = 99L;
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act
        assertThrows(ObjectNotFoundException.class, () -> usuarioService.deletarUsuarioPorId(usuarioId));

        // Assert
        verify(usuarioRepository).findById(usuarioId);
        verify(usuarioRepository, never()).delete(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveRemoverUsuarioAutenticadoComSucesso() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuario(40L, "Carlos Lima", "carlos.lima@teste.com");
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act
        usuarioService.meRemover();

        // Assert
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(usuarioRepository).delete(same(usuarioAutenticado));
        verifyNoInteractions(passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveAdicionarMeuPerfilCandidatoComSucesso() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuario(50L, "Ana Lima", "ana.lima@teste.com");
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act
        usuarioService.adicionarMeuPerfilCandidato();

        // Assert
        ArgumentCaptor<PerfilCandidato> perfilCaptor = ArgumentCaptor.forClass(PerfilCandidato.class);

        assertAll(
                () -> assertTrue(usuarioAutenticado.ehCandidato()),
                () -> assertNotNull(usuarioAutenticado.getPerfilCandidato())
        );

        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(perfilCandidatoRepository).save(perfilCaptor.capture());
        verify(usuarioRepository).save(same(usuarioAutenticado));

        PerfilCandidato perfilSalvo = perfilCaptor.getValue();
        assertAll(
                () -> assertSame(usuarioAutenticado, perfilSalvo.getUsuario()),
                () -> assertSame(perfilSalvo, usuarioAutenticado.getPerfilCandidato())
        );
        verifyNoInteractions(passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveAdicionarPerfilCandidatoPorIdComSucesso() {
        // Arrange
        Long usuarioId = 51L;
        Usuario usuario = criarUsuario(usuarioId, "Bruno Rocha", "bruno.rocha@teste.com");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.adicionarPerfilCandidatoPorId(usuarioId);

        // Assert
        ArgumentCaptor<PerfilCandidato> perfilCaptor = ArgumentCaptor.forClass(PerfilCandidato.class);

        assertAll(
                () -> assertTrue(usuario.ehCandidato()),
                () -> assertNotNull(usuario.getPerfilCandidato())
        );

        verify(usuarioRepository).findById(usuarioId);
        verify(perfilCandidatoRepository).save(perfilCaptor.capture());
        verify(usuarioRepository).save(same(usuario));

        PerfilCandidato perfilSalvo = perfilCaptor.getValue();
        assertAll(
                () -> assertSame(usuario, perfilSalvo.getUsuario()),
                () -> assertSame(perfilSalvo, usuario.getPerfilCandidato())
        );
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearAdicionarMeuPerfilCandidatoQuandoUsuarioJaForCandidato() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuarioComPerfilCandidato(52L, "Carla Souza", "carla.souza@teste.com");
        PerfilCandidato perfilExistente = usuarioAutenticado.getPerfilCandidato();
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);

        // Act
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioService.adicionarMeuPerfilCandidato()
        );

        // Assert
        assertNotNull(exception);
        assertSame(perfilExistente, usuarioAutenticado.getPerfilCandidato());
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(perfilCandidatoRepository, never()).save(any(PerfilCandidato.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearAdicionarPerfilCandidatoPorIdQuandoUsuarioJaForCandidato() {
        // Arrange
        Long usuarioId = 53L;
        Usuario usuario = criarUsuarioComPerfilCandidato(usuarioId, "Daniel Alves", "daniel.alves@teste.com");
        PerfilCandidato perfilExistente = usuario.getPerfilCandidato();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Act
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioService.adicionarPerfilCandidatoPorId(usuarioId)
        );

        // Assert
        assertNotNull(exception);
        assertSame(perfilExistente, usuario.getPerfilCandidato());
        verify(usuarioRepository).findById(usuarioId);
        verify(perfilCandidatoRepository, never()).save(any(PerfilCandidato.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveRemoverMeuPerfilCandidatoComSucesso() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuarioComPerfisCandidatoERecrutador(
                54L,
                "Eduarda Lima",
                "eduarda.lima@teste.com",
                "Empresa Sul"
        );
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);
        when(perfilCandidatoRepository.existsById(usuarioAutenticado.getId())).thenReturn(true);

        // Act
        usuarioService.removerMeuPerfilCandidato();

        // Assert
        assertAll(
                () -> assertFalse(usuarioAutenticado.ehCandidato()),
                () -> assertTrue(usuarioAutenticado.ehRecrutador()),
                () -> assertNotNull(usuarioAutenticado.getPerfilRecrutador())
        );

        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(perfilCandidatoRepository).existsById(usuarioAutenticado.getId());
        verify(usuarioRepository).save(same(usuarioAutenticado));
        verifyNoInteractions(passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveRemoverPerfilCandidatoPorIdComSucesso() {
        // Arrange
        Long usuarioId = 55L;
        Usuario usuario = criarUsuarioComPerfisCandidatoERecrutador(
                usuarioId,
                "Fabio Melo",
                "fabio.melo@teste.com",
                "Empresa Norte"
        );
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilCandidatoRepository.existsById(usuarioId)).thenReturn(true);

        // Act
        usuarioService.removerPerfilCandidatoPorId(usuarioId);

        // Assert
        assertAll(
                () -> assertFalse(usuario.ehCandidato()),
                () -> assertTrue(usuario.ehRecrutador()),
                () -> assertNotNull(usuario.getPerfilRecrutador())
        );

        verify(usuarioRepository).findById(usuarioId);
        verify(perfilCandidatoRepository).existsById(usuarioId);
        verify(usuarioRepository).save(same(usuario));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearRemoverMeuPerfilCandidatoQuandoUsuarioNaoForCandidato() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuarioComPerfilRecrutador(
                56L,
                "Gabriela Costa",
                "gabriela.costa@teste.com",
                "Empresa Leste"
        );
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);
        when(perfilCandidatoRepository.existsById(usuarioAutenticado.getId())).thenReturn(false);

        // Act
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> usuarioService.removerMeuPerfilCandidato()
        );

        // Assert
        assertNotNull(exception);
        assertFalse(usuarioAutenticado.ehCandidato());
        assertTrue(usuarioAutenticado.ehRecrutador());
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(perfilCandidatoRepository).existsById(usuarioAutenticado.getId());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearRemoverPerfilCandidatoPorIdQuandoUsuarioNaoForCandidato() {
        // Arrange
        Long usuarioId = 57L;
        Usuario usuario = criarUsuarioComPerfilRecrutador(
                usuarioId,
                "Henrique Alves",
                "henrique.alves@teste.com",
                "Empresa Oeste"
        );
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilCandidatoRepository.existsById(usuarioId)).thenReturn(false);

        // Act
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> usuarioService.removerPerfilCandidatoPorId(usuarioId)
        );

        // Assert
        assertNotNull(exception);
        assertFalse(usuario.ehCandidato());
        assertTrue(usuario.ehRecrutador());
        verify(usuarioRepository).findById(usuarioId);
        verify(perfilCandidatoRepository).existsById(usuarioId);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveRemoverMeuPerfilRecrutadorComSucesso() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuarioComPerfisCandidatoERecrutador(
                58L,
                "Isabela Rocha",
                "isabela.rocha@teste.com",
                "Empresa Centro"
        );
        adicionarOportunidadeAoPerfil(usuarioAutenticado.getPerfilRecrutador(), 801L, "Vaga Backend");
        adicionarOportunidadeAoPerfil(usuarioAutenticado.getPerfilRecrutador(), 802L, "Vaga QA");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);
        when(perfilRecrutadorRepository.existsById(usuarioAutenticado.getId())).thenReturn(true);

        // Act
        usuarioService.removerMeuPerfilRecrutador();

        // Assert
        assertAll(
                () -> assertFalse(usuarioAutenticado.ehRecrutador()),
                () -> assertTrue(usuarioAutenticado.ehCandidato()),
                () -> assertNotNull(usuarioAutenticado.getPerfilCandidato())
        );

        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(perfilRecrutadorRepository).existsById(usuarioAutenticado.getId());
        verify(oportunidadeRepository).removerTodosInteressesDaVaga(801L);
        verify(oportunidadeRepository).removerTodosInteressesDaVaga(802L);
        verify(usuarioRepository).save(same(usuarioAutenticado));
        verifyNoInteractions(passwordEncoder, perfilCandidatoRepository);
    }

    @Test
    void deveRemoverPerfilRecrutadorPorIdComSucesso() {
        // Arrange
        Long usuarioId = 59L;
        Usuario usuario = criarUsuarioComPerfisCandidatoERecrutador(
                usuarioId,
                "Joana Nunes",
                "joana.nunes@teste.com",
                "Empresa Vale"
        );
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilRecrutadorRepository.existsById(usuarioId)).thenReturn(true);

        // Act
        usuarioService.removerPerfilRecrutadorPorId(usuarioId);

        // Assert
        assertAll(
                () -> assertFalse(usuario.ehRecrutador()),
                () -> assertTrue(usuario.ehCandidato()),
                () -> assertNotNull(usuario.getPerfilCandidato())
        );

        verify(usuarioRepository).findById(usuarioId);
        verify(perfilRecrutadorRepository).existsById(usuarioId);
        verify(usuarioRepository).save(same(usuario));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearRemoverMeuPerfilRecrutadorQuandoUsuarioNaoForRecrutador() {
        // Arrange
        Usuario usuarioAutenticado = criarUsuarioComPerfilCandidato(60L, "Karen Prado", "karen.prado@teste.com");
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);
        when(perfilRecrutadorRepository.existsById(usuarioAutenticado.getId())).thenReturn(false);

        // Act
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> usuarioService.removerMeuPerfilRecrutador()
        );

        // Assert
        assertNotNull(exception);
        assertTrue(usuarioAutenticado.ehCandidato());
        assertFalse(usuarioAutenticado.ehRecrutador());
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(perfilRecrutadorRepository).existsById(usuarioAutenticado.getId());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder, perfilCandidatoRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearRemoverPerfilRecrutadorPorIdQuandoUsuarioNaoForRecrutador() {
        // Arrange
        Long usuarioId = 61L;
        Usuario usuario = criarUsuarioComPerfilCandidato(usuarioId, "Lucas Matos", "lucas.matos@teste.com");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilRecrutadorRepository.existsById(usuarioId)).thenReturn(false);

        // Act
        ObjectNotFoundException exception = assertThrows(
                ObjectNotFoundException.class,
                () -> usuarioService.removerPerfilRecrutadorPorId(usuarioId)
        );

        // Assert
        assertNotNull(exception);
        assertTrue(usuario.ehCandidato());
        assertFalse(usuario.ehRecrutador());
        verify(usuarioRepository).findById(usuarioId);
        verify(perfilRecrutadorRepository).existsById(usuarioId);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, oportunidadeRepository);
    }

    private Usuario criarUsuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario("12345678909", nome, "83999990000", email, "senha-codificada");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Usuario criarUsuarioComPerfilCandidato(Long id, String nome, String email) {
        Usuario usuario = criarUsuario(id, nome, email);
        usuario.adicionarPerfilCandidato(new PerfilCandidato(usuario));
        return usuario;
    }

    private Usuario criarUsuarioComPerfilRecrutador(Long id, String nome, String email, String empresa) {
        Usuario usuario = criarUsuario(id, nome, email);
        usuario.adicionarPerfilRecrutador(new PerfilRecrutador(usuario, empresa));
        return usuario;
    }

    private Usuario criarUsuarioComPerfisCandidatoERecrutador(Long id, String nome, String email, String empresa) {
        Usuario usuario = criarUsuarioComPerfilCandidato(id, nome, email);
        usuario.adicionarPerfilRecrutador(new PerfilRecrutador(usuario, empresa));
        return usuario;
    }

    private void adicionarOportunidadeAoPerfil(PerfilRecrutador perfilRecrutador, Long vagaId, String descricao) {
        OportunidadeDeEmprego vaga = new OportunidadeDeEmprego(descricao, null, null, null, perfilRecrutador);
        ReflectionTestUtils.setField(vaga, "id", vagaId);
        perfilRecrutador.adicionarOportunidadePostada(vaga);
    }
}
