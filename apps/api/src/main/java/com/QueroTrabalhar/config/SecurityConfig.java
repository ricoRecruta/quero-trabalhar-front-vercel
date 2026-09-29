package com.QueroTrabalhar.config;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.security.JWTAuthenticationFilter;
import com.QueroTrabalhar.security.JWTAuthorizationFilter;
import com.QueroTrabalhar.security.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String[] DEV_ONLY_PUBLIC_MATCHES = {
            "/h2-console/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private static final String LOGIN_PATH = "/login";
    private static final String ADMIN_MATCHER = "/api/admin/**";

    private static final String[] PUBLIC_POST_MATCHES = {
            "/api/usuarios/cadastrar"
    };

    private static final String[] PUBLIC_GET_MATCHES = {
            "/api/localidades/paises",
            "/api/localidades/estados",
            "/api/localidades/cidades",
            "/api/empresas",
            "/api/oportunidades",
            "/api/tipos-de-emprego/aprovados"
    };

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins,
            JWTUtil jwtUtil,
            UserDetailsService userDetailsService,
            ObjectMapper objectMapper
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    @Profile("local")
    public SecurityFilterChain localSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager
    ) throws Exception {
        return buildJwtSecurityFilterChain(http, authenticationManager, true);
    }

    @Bean
    @Profile({"test", "prod"})
    public SecurityFilterChain testAndProdSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager
    ) throws Exception {
        return buildJwtSecurityFilterChain(http, authenticationManager, false);
    }

    private SecurityFilterChain buildJwtSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            boolean allowDeveloperTooling
    ) throws Exception {
        if (allowDeveloperTooling) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }

        JWTAuthenticationFilter authenticationFilter =
                new JWTAuthenticationFilter(LOGIN_PATH, authenticationManager, jwtUtil);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(
                new RegexRequestMatcher("^/login$", HttpMethod.POST.name())
        );

        JWTAuthorizationFilter authorizationFilter =
                new JWTAuthorizationFilter(jwtUtil, userDetailsService);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json;charset=UTF-8");

                            StandardError error = new StandardError(
                                    System.currentTimeMillis(),
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    "Não autorizado",
                                    "Token ausente, inválido ou expirado.",
                                    request.getRequestURI()
                            );

                            objectMapper.writeValue(response.getWriter(), error);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json;charset=UTF-8");

                            StandardError error = new StandardError(
                                    System.currentTimeMillis(),
                                    HttpServletResponse.SC_FORBIDDEN,
                                    "Acesso negado",
                                    "Você não possui permissão para acessar este recurso.",
                                    request.getRequestURI()
                            );

                            objectMapper.writeValue(response.getWriter(), error);
                        })
                )
                .authorizeHttpRequests(auth -> {
                    if (allowDeveloperTooling) {
                        auth.requestMatchers(DEV_ONLY_PUBLIC_MATCHES).permitAll();
                    }

                    auth.requestMatchers(HttpMethod.POST, LOGIN_PATH).permitAll()
                            .requestMatchers(HttpMethod.POST, PUBLIC_POST_MATCHES).permitAll()
                            .requestMatchers(HttpMethod.GET, PUBLIC_GET_MATCHES).permitAll()
                            .requestMatchers(
                                    new RegexRequestMatcher("^/api/empresas/\\d+$", HttpMethod.GET.name()),
                                    new RegexRequestMatcher("^/api/empresas/\\d+/recrutadores$", HttpMethod.GET.name()),
                                    new RegexRequestMatcher("^/api/empresas/\\d+/oportunidades$", HttpMethod.GET.name()),
                                    new RegexRequestMatcher("^/api/oportunidades/\\d+$", HttpMethod.GET.name()),
                                    new RegexRequestMatcher("^/api/tipos-de-emprego/\\d+$", HttpMethod.GET.name())
                            ).permitAll()
                            .requestMatchers(ADMIN_MATCHER).hasRole("ADMIN")
                            .anyRequest().authenticated();
                })
                .addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "POST",
                "GET",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        configuration.setExposedHeaders(List.of(
                "Authorization"
        ));

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
