package com.QueroTrabalhar.security;

import com.QueroTrabalhar.domain.entity.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Usuario getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserSecurity userSecurity) {
            return userSecurity.getUsuario();
        }

        throw new ClassCastException("O principal no SecurityContext não é um UserSecurity.");
    }
}
