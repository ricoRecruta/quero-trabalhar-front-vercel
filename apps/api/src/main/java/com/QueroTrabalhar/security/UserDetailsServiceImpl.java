package com.QueroTrabalhar.security;

import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        // Contas criadas por versões antigas do cadastro não recebiam o
        // papel USER. Corrige o registro ao primeiro login sem invalidar a
        // senha já armazenada.
        if (usuario.getProfiles().isEmpty()) {
            usuario.addProfile(com.QueroTrabalhar.domain.enums.Role.USER);
            usuarioRepository.save(usuario);
        }

        return new UserSecurity(usuario);
    }
}
