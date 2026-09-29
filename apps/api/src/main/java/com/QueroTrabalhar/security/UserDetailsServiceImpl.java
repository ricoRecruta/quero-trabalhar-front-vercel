package com.QueroTrabalhar.security;

import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.Role;
import com.QueroTrabalhar.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final Set<String> adminEmails;

    public UserDetailsServiceImpl(
            UsuarioRepository usuarioRepository,
            @Value("${app.security.admin-emails:}") String adminEmails
    ) {
        this.usuarioRepository = usuarioRepository;
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(UserDetailsServiceImpl::normalizarEmail)
                .filter(email -> !email.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        // Contas criadas por versões antigas do cadastro não recebiam o
        // papel USER. Corrige o registro ao primeiro login sem invalidar a
        // senha já armazenada.
        boolean alterado = false;

        if (usuario.getProfiles().isEmpty()) {
            usuario.addProfile(Role.USER);
            alterado = true;
        }

        // O ambiente de producao nao executa o seed local. Esta lista permite
        // promover, de forma controlada pela configuracao da Vercel, uma conta
        // real ja cadastrada sem criar senha administrativa no codigo-fonte.
        if (adminEmails.contains(normalizarEmail(usuario.getEmail()))
                && !usuario.getProfiles().contains(Role.ADMIN)) {
            usuario.addProfile(Role.ADMIN);
            alterado = true;
        }

        if (alterado) {
            usuarioRepository.save(usuario);
        }

        return new UserSecurity(usuario);
    }

    private static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
