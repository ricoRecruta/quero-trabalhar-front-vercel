package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Usuario obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessRuleException("Usuário autenticado não encontrado no contexto de segurança.");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ObjectNotFoundException("Usuário autenticado não encontrado."));
    }

    @Transactional(readOnly = true)
    public PerfilRecrutador obterPerfilRecrutadorAutenticado() {
        Usuario usuarioAutenticado = obterUsuarioAutenticado();

        if (!usuarioAutenticado.ehRecrutador() || usuarioAutenticado.getPerfilRecrutador() == null) {
            throw new BusinessRuleException(
                    "O usuário autenticado precisa possuir um perfil de recrutador ativo para acessar este recurso."
            );
        }

        return usuarioAutenticado.getPerfilRecrutador();
    }

    @Transactional(readOnly = true)
    public PerfilCandidato obterPerfilCandidatoAutenticado() {
        Usuario usuarioAutenticado = obterUsuarioAutenticado();

        if (!usuarioAutenticado.ehCandidato() || usuarioAutenticado.getPerfilCandidato() == null) {
            throw new BusinessRuleException(
                    "O usuário autenticado precisa possuir um perfil de candidato ativo para acessar este recurso."
            );
        }

        return usuarioAutenticado.getPerfilCandidato();
    }
}
