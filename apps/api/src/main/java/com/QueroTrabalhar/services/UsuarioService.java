package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorUsuarioRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.AlterarSenhaRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioFilterDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioAtualizacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioResponseDTO;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.repository.specification.UsuarioSpecification;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.DataIntegrityViolationException;
import com.QueroTrabalhar.services.exceptions.DuplicateResourceException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final PerfilRecrutadorRepository perfilRecrutadorRepository;
    private final OportunidadeDeEmpregoRepository oportunidadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PerfilCandidatoRepository perfilCandidatoRepository,
            PerfilRecrutadorRepository perfilRecrutadorRepository,
            OportunidadeDeEmpregoRepository oportunidadeRepository,
            PasswordEncoder passwordEncoder,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.perfilRecrutadorRepository = perfilRecrutadorRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarUsuarios(UsuarioFilterDTO filtro, Pageable pageable) {
        return usuarioRepository.findAll(UsuarioSpecification.comFiltros(filtro), pageable)
                .map(UsuarioResponseDTO::daEntidade);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorId(Long id) {
        return UsuarioResponseDTO.daEntidade(encontrarUsuario(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioAutenticado() {
        return UsuarioResponseDTO.daEntidade(obterUsuarioAutenticado());
    }

    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO usuarioRequest) {
        if (usuarioRepository.existsByCpf(usuarioRequest.cpf())) {
            throw new DataIntegrityViolationException("Já existe um usuário com o CPF: " + usuarioRequest.cpf());
        }

        usuarioRepository.findByEmail(usuarioRequest.email())
                .ifPresent(usuarioExistente -> {
                    throw new DuplicateResourceException("Já existe um usuário com o e-mail informado.");
                });

        Usuario usuario = new Usuario(usuarioRequest);
        usuario.setSenha(passwordEncoder.encode(usuarioRequest.senha()));

        PerfilCandidato perfilPadrao = new PerfilCandidato(usuario);
        usuario.adicionarPerfilCandidato(perfilPadrao);

        return UsuarioResponseDTO.daEntidade(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizarMeuUsuario(UsuarioAtualizacaoRequestDTO dto) {
        Usuario usuarioAutenticado = obterUsuarioAutenticado();
        atualizarDadosCadastrais(usuarioAutenticado, dto);
        return UsuarioResponseDTO.daEntidade(usuarioRepository.save(usuarioAutenticado));
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuarioComoAdmin(Long id, UsuarioAtualizacaoRequestDTO dto) {
        Usuario usuarioExistente = encontrarUsuario(id);
        atualizarDadosCadastrais(usuarioExistente, dto);
        return UsuarioResponseDTO.daEntidade(usuarioRepository.save(usuarioExistente));
    }

    @Transactional
    public void alterarMinhaSenha(AlterarSenhaRequestDTO dto) {
        Usuario usuarioAutenticado = obterUsuarioAutenticado();

        if (!passwordEncoder.matches(dto.senhaAtual(), usuarioAutenticado.getSenha())) {
            throw new BusinessRuleException("A senha atual informada está incorreta.");
        }

        if (!dto.novaSenha().equals(dto.confirmacaoNovaSenha())) {
            throw new BusinessRuleException("A nova senha e a confirmação da nova senha devem ser iguais.");
        }

        usuarioAutenticado.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuarioAutenticado);
    }

    @Transactional
    public void removerMeuPerfilCandidato() {
        processarRemocaoCandidato(obterUsuarioAutenticado());
    }

    @Transactional
    public void removerPerfilCandidatoPorId(Long id) {
        Usuario usuario = encontrarUsuario(id);
        processarRemocaoCandidato(usuario);
    }

    private void processarRemocaoCandidato(Usuario usuario) {
        if (!perfilCandidatoRepository.existsById(usuario.getId())) {
            throw new ObjectNotFoundException("Perfil candidato não encontrado para o usuário ID: " + usuario.getId());
        }

        if (usuario.ehRecrutador()) {
            usuario.removerPerfilCandidato();
            usuarioRepository.save(usuario);
            return;
        }

        throw new DataIntegrityViolationException("O usuário precisa ter pelo menos um perfil ativo.");
    }

    @Transactional
    public void removerMeuPerfilRecrutador() {
        processarRemocaoRecrutador(obterUsuarioAutenticado());
    }

    @Transactional
    public void removerPerfilRecrutadorPorId(Long id) {
        Usuario usuario = encontrarUsuario(id);
        processarRemocaoRecrutador(usuario);
    }

    private void processarRemocaoRecrutador(Usuario usuario) {
        if (!perfilRecrutadorRepository.existsById(usuario.getId())) {
            throw new ObjectNotFoundException("Perfil recrutador não encontrado para o usuário ID: " + usuario.getId());
        }

        if (usuario.ehCandidato()) {
            PerfilRecrutador perfil = usuario.getPerfilRecrutador();

            if (!perfil.getOportunidadesPostadas().isEmpty()) {
                for (OportunidadeDeEmprego vaga : perfil.getOportunidadesPostadas()) {
                    oportunidadeRepository.removerTodosInteressesDaVaga(vaga.getId());
                }
            }

            usuario.removerPerfilRecrutador();
            usuarioRepository.save(usuario);
            return;
        }

        throw new DataIntegrityViolationException("O usuário precisa ter pelo menos um perfil ativo");
    }

    @Transactional
    public void adicionarMeuPerfilCandidato() {
        processarAdicionarCandidato(obterUsuarioAutenticado());
    }

    @Transactional
    public void adicionarPerfilCandidatoPorId(Long id) {
        Usuario usuario = encontrarUsuario(id);
        processarAdicionarCandidato(usuario);
    }

    private void processarAdicionarCandidato(Usuario usuario) {
        if (usuario.ehCandidato()) {
            throw new DataIntegrityViolationException("O usuário já é um candidato");
        }

        PerfilCandidato perfilPadrao = new PerfilCandidato(usuario);
        usuario.adicionarPerfilCandidato(perfilPadrao);

        perfilCandidatoRepository.save(perfilPadrao);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void adicionarMeuPerfilRecrutador(PerfilRecrutadorUsuarioRequestDTO dto) {
        processarAdicionarRecrutador(obterUsuarioAutenticado(), dto.nomeDaEmpresa());
    }

    @Transactional
    public void adicionarPerfilRecrutadorPorId(Long id, PerfilRecrutadorUsuarioRequestDTO dto) {
        Usuario usuario = encontrarUsuario(id);
        processarAdicionarRecrutador(usuario, dto.nomeDaEmpresa());
    }

    private void processarAdicionarRecrutador(Usuario usuario, String nomeDaEmpresa) {
        if (usuario.ehRecrutador()) {
            throw new DataIntegrityViolationException("O usuário já é um recrutador");
        }

        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(usuario, nomeDaEmpresa);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);

        perfilRecrutadorRepository.save(perfilRecrutador);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void meRemover() {
        processarRemocaoUsuario(obterUsuarioAutenticado());
    }

    @Transactional
    public void deletarUsuarioPorId(Long id) {
        Usuario usuario = encontrarUsuario(id);
        processarRemocaoUsuario(usuario);
    }

    private void processarRemocaoUsuario(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }

    private void atualizarDadosCadastrais(Usuario usuario, UsuarioAtualizacaoRequestDTO dto) {
        validarEmailDisponivelParaAtualizacao(usuario, dto.email());
        usuario.setNome(dto.nome());
        usuario.setTelefone(dto.telefone());
        usuario.setEmail(dto.email());
    }

    private void validarEmailDisponivelParaAtualizacao(Usuario usuario, String novoEmail) {
        if (Objects.equals(usuario.getEmail(), novoEmail)) {
            return;
        }

        usuarioRepository.findByEmail(novoEmail)
                .filter(usuarioComMesmoEmail -> !usuarioComMesmoEmail.getId().equals(usuario.getId()))
                .ifPresent(usuarioComMesmoEmail -> {
                    throw new DuplicateResourceException("O e-mail informado já está em uso por outro usuário.");
                });
    }

    private Usuario obterUsuarioAutenticado() {
        return usuarioAutenticadoService.obterUsuarioAutenticado();
    }

    private Usuario encontrarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado. ID: " + id));
    }
}
