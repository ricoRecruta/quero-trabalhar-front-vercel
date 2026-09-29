package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.usuario.AlterarSenhaRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioAtualizacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioResponseDTO;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.DuplicateResourceException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioAtualizacaoSenhaServiceTest {

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
    void deveAtualizarMeuUsuarioSemAlterarSenha() {
        String senhaOriginal = "senha-codificada-original";
        Usuario usuario = criarUsuario(1L, "Ana Júlia", senhaOriginal);
        String cpfOriginal = usuario.getCpf();
        UsuarioAtualizacaoRequestDTO dto = new UsuarioAtualizacaoRequestDTO(
                "Marina Souza",
                "83988887766",
                "marina.souza@teste.com"
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO resposta = usuarioService.atualizarMeuUsuario(dto);

        assertAll(
                () -> assertEquals("Marina Souza", usuario.getNome()),
                () -> assertEquals("83988887766", usuario.getTelefone()),
                () -> assertEquals("marina.souza@teste.com", usuario.getEmail()),
                () -> assertEquals(senhaOriginal, usuario.getSenha()),
                () -> assertEquals(cpfOriginal, usuario.getCpf()),
                () -> assertEquals(1L, resposta.id()),
                () -> assertEquals("Marina Souza", resposta.nome()),
                () -> assertEquals("83988887766", resposta.telefone()),
                () -> assertEquals("marina.souza@teste.com", resposta.email())
        );
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void devePermitirAtualizarMeuUsuarioMantendoMesmoEmail() {
        String emailAtual = "camila.souza@teste.com";
        Usuario usuario = criarUsuario(21L, "Camila Souza", "senha-codificada");
        usuario.setEmail(emailAtual);
        UsuarioAtualizacaoRequestDTO dto = new UsuarioAtualizacaoRequestDTO(
                "Camila Souza Lima",
                "11988887766",
                emailAtual
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO resposta = usuarioService.atualizarMeuUsuario(dto);

        assertAll(
                () -> assertEquals("Camila Souza Lima", usuario.getNome()),
                () -> assertEquals("11988887766", usuario.getTelefone()),
                () -> assertEquals(emailAtual, usuario.getEmail()),
                () -> assertEquals(emailAtual, resposta.email())
        );
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearAtualizacaoMeuUsuarioComEmailDeOutroUsuario() {
        String emailEmUso = "email.em.uso@teste.com";
        Usuario usuarioAutenticado = criarUsuario(22L, "João da Silva", "senha-codificada");
        Usuario outroUsuario = criarUsuario(30L, "Marília Costa", "senha-codificada");
        outroUsuario.setEmail(emailEmUso);
        UsuarioAtualizacaoRequestDTO dto = new UsuarioAtualizacaoRequestDTO(
                "João da Silva",
                "21997776655",
                emailEmUso
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuarioAutenticado);
        when(usuarioRepository.findByEmail(emailEmUso)).thenReturn(Optional.of(outroUsuario));

        assertThrows(DuplicateResourceException.class, () -> usuarioService.atualizarMeuUsuario(dto));

        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(usuarioRepository).findByEmail(emailEmUso);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveAtualizarUsuarioComoAdminSemAlterarSenha() {
        Long idUsuario = 7L;
        String senhaOriginal = "hash-admin-preservado";
        Usuario usuario = criarUsuario(idUsuario, "Carlos Pereira", senhaOriginal);
        String cpfOriginal = usuario.getCpf();
        UsuarioAtualizacaoRequestDTO dto = new UsuarioAtualizacaoRequestDTO(
                "Carlos Eduardo Pereira",
                "11977776666",
                "carlos.pereira@empresa.com"
        );

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO resposta = usuarioService.atualizarUsuarioComoAdmin(idUsuario, dto);

        assertAll(
                () -> assertEquals("Carlos Eduardo Pereira", usuario.getNome()),
                () -> assertEquals("11977776666", usuario.getTelefone()),
                () -> assertEquals("carlos.pereira@empresa.com", usuario.getEmail()),
                () -> assertEquals(senhaOriginal, usuario.getSenha()),
                () -> assertEquals(cpfOriginal, usuario.getCpf()),
                () -> assertEquals(idUsuario, resposta.id()),
                () -> assertEquals("Carlos Eduardo Pereira", resposta.nome()),
                () -> assertEquals("11977776666", resposta.telefone()),
                () -> assertEquals("carlos.pereira@empresa.com", resposta.email())
        );
        verify(usuarioRepository).findById(idUsuario);
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void devePermitirAtualizacaoAdminMantendoMesmoEmail() {
        Long idUsuario = 23L;
        String emailAtual = "admin.mesmo.email@empresa.com";
        Usuario usuario = criarUsuario(idUsuario, "Paulo Roberto", "senha-codificada");
        usuario.setEmail(emailAtual);
        UsuarioAtualizacaoRequestDTO dto = new UsuarioAtualizacaoRequestDTO(
                "Paulo Roberto Júnior",
                "31996665544",
                emailAtual
        );

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO resposta = usuarioService.atualizarUsuarioComoAdmin(idUsuario, dto);

        assertAll(
                () -> assertEquals("Paulo Roberto Júnior", usuario.getNome()),
                () -> assertEquals("31996665544", usuario.getTelefone()),
                () -> assertEquals(emailAtual, usuario.getEmail()),
                () -> assertEquals(emailAtual, resposta.email())
        );
        verify(usuarioRepository).findById(idUsuario);
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearAtualizacaoAdminComEmailDeOutroUsuario() {
        Long idUsuarioAtualizado = 24L;
        String emailEmUso = "repetido@empresa.com";
        Usuario usuarioAtualizado = criarUsuario(idUsuarioAtualizado, "Renata Alves", "senha-codificada");
        Usuario usuarioComMesmoEmail = criarUsuario(31L, "Bruno Mendes", "senha-codificada");
        usuarioComMesmoEmail.setEmail(emailEmUso);
        UsuarioAtualizacaoRequestDTO dto = new UsuarioAtualizacaoRequestDTO(
                "Renata Alves",
                "41995554433",
                emailEmUso
        );

        when(usuarioRepository.findById(idUsuarioAtualizado)).thenReturn(Optional.of(usuarioAtualizado));
        when(usuarioRepository.findByEmail(emailEmUso)).thenReturn(Optional.of(usuarioComMesmoEmail));

        assertThrows(DuplicateResourceException.class, () -> usuarioService.atualizarUsuarioComoAdmin(idUsuarioAtualizado, dto));

        verify(usuarioRepository).findById(idUsuarioAtualizado);
        verify(usuarioRepository).findByEmail(emailEmUso);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveAlterarMinhaSenhaComSenhaAtualCorreta() {
        String senhaAtualCodificada = "senha-atual-codificada";
        Usuario usuario = criarUsuario(3L, "Bianca Lima", senhaAtualCodificada);
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO(
                "SenhaAtual@123",
                "NovaSenha@456",
                "NovaSenha@456"
        );
        String novaSenhaCodificada = "nova-senha-codificada";

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(passwordEncoder.matches(dto.senhaAtual(), senhaAtualCodificada)).thenReturn(true);
        when(passwordEncoder.encode(dto.novaSenha())).thenReturn(novaSenhaCodificada);

        usuarioService.alterarMinhaSenha(dto);

        assertEquals(novaSenhaCodificada, usuario.getSenha());
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(passwordEncoder).matches(dto.senhaAtual(), senhaAtualCodificada);
        verify(passwordEncoder).encode(dto.novaSenha());
        verify(usuarioRepository).save(usuario);
        verifyNoInteractions(perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearAlteracaoDeSenhaQuandoSenhaAtualIncorreta() {
        String senhaAtualCodificada = "senha-atual-codificada";
        Usuario usuario = criarUsuario(4L, "João Pedro", senhaAtualCodificada);
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO(
                "SenhaErrada@123",
                "NovaSenha@456",
                "NovaSenha@456"
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(passwordEncoder.matches(dto.senhaAtual(), senhaAtualCodificada)).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> usuarioService.alterarMinhaSenha(dto));

        assertEquals(senhaAtualCodificada, usuario.getSenha());
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(passwordEncoder).matches(dto.senhaAtual(), senhaAtualCodificada);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBloquearAlteracaoDeSenhaQuandoConfirmacaoDivergir() {
        String senhaAtualCodificada = "senha-atual-codificada";
        Usuario usuario = criarUsuario(5L, "Márcia Alves", senhaAtualCodificada);
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO(
                "SenhaAtual@123",
                "NovaSenha@456",
                "OutraSenha@456"
        );

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(passwordEncoder.matches(dto.senhaAtual(), senhaAtualCodificada)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> usuarioService.alterarMinhaSenha(dto));

        assertEquals(senhaAtualCodificada, usuario.getSenha());
        verify(usuarioAutenticadoService).obterUsuarioAutenticado();
        verify(passwordEncoder).matches(dto.senhaAtual(), senhaAtualCodificada);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBuscarUsuarioAutenticado() {
        Usuario usuario = criarUsuario(11L, "Fernanda Costa", "senha-codificada");

        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario);

        UsuarioResponseDTO resposta = usuarioService.buscarUsuarioAutenticado();

        assertAll(
                () -> assertInstanceOf(UsuarioResponseDTO.class, resposta),
                () -> assertEquals(11L, resposta.id()),
                () -> assertEquals("Fernanda Costa", resposta.nome()),
                () -> assertEquals("83999990011", resposta.telefone()),
                () -> assertEquals("fernanda.costa11@teste.com", resposta.email())
        );
        verify(usuarioAutenticadoService, only()).obterUsuarioAutenticado();
        verifyNoInteractions(usuarioRepository, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveBuscarUsuarioPorIdComoAdmin() {
        Long idUsuario = 15L;
        Usuario usuario = criarUsuario(idUsuario, "Rafael Gomes", "senha-codificada");

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO resposta = usuarioService.buscarUsuarioPorId(idUsuario);

        assertAll(
                () -> assertInstanceOf(UsuarioResponseDTO.class, resposta),
                () -> assertEquals(idUsuario, resposta.id()),
                () -> assertEquals("Rafael Gomes", resposta.nome()),
                () -> assertEquals("83999990015", resposta.telefone()),
                () -> assertEquals("rafael.gomes15@teste.com", resposta.email())
        );
        verify(usuarioRepository).findById(idUsuario);
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    @Test
    void deveLancarObjectNotFoundExceptionQuandoUsuarioPorIdNaoExistir() {
        Long idInexistente = 99L;
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> usuarioService.buscarUsuarioPorId(idInexistente));

        verify(usuarioRepository).findById(idInexistente);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(usuarioAutenticadoService, passwordEncoder, perfilCandidatoRepository, perfilRecrutadorRepository, oportunidadeRepository);
    }

    private Usuario criarUsuario(Long id, String nome, String senha) {
        Usuario usuario = new Usuario(
                "12345678909",
                nome,
                "839999900" + String.format("%02d", id),
                nome.toLowerCase().replace(" ", ".") + id + "@teste.com",
                senha
        );
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }
}
