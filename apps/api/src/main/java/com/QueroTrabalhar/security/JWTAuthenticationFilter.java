package com.QueroTrabalhar.security;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.domain.dtos.CredentialsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JWTUtil jwtUtil;


    public JWTAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        super(defaultFilterProcessesUrl);
        setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        CredentialsDTO credentials = new ObjectMapper().readValue(request.getInputStream(), CredentialsDTO.class);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(credentials.email(), credentials.password());

        return getAuthenticationManager().authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {

        String userName = authResult.getName();
        String token = jwtUtil.generateToken(userName);

        response.setHeader("access-control-expose-headers", "Authorization");
        response.setHeader("Authorization", "Bearer " + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpServletResponse.SC_UNAUTHORIZED,
                "Não autorizado",
                "E-mail ou senha inválidos.",
                request.getRequestURI()
        );

        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}
