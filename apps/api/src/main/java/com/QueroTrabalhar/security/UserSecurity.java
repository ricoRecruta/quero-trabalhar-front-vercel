package com.QueroTrabalhar.security;

import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserSecurity implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Usuario user;

    public UserSecurity(Long id, String email, String password, Set<Role> roles, Usuario user) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = roles.stream()
                .map(x -> new SimpleGrantedAuthority(x.getAuthority()))
                .collect(Collectors.toSet());
        this.user = user;
    }

    public UserSecurity(Usuario user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getSenha();
        this.authorities = user.getProfiles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
