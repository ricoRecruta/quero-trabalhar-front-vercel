package com.QueroTrabalhar.config;

import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.Role;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    private static final String PASSWORD = "123456";
    private static final String USER_EMAIL = "usuario.security@teste.com";
    private static final String ADMIN_EMAIL = "admin.security@teste.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        usuarioRepository.flush();

        usuarioRepository.save(criarUsuario(
                "63069141021",
                "Usuario Comum",
                "83999990001",
                USER_EMAIL,
                false
        ));

        usuarioRepository.save(criarUsuario(
                "27128833064",
                "Usuario Admin",
                "83999990002",
                ADMIN_EMAIL,
                true
        ));

        usuarioRepository.flush();
    }

    @Test
    void deveAutenticarComCredenciaisValidas() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciaisJson(USER_EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")));
    }

    @Test
    void deveNegarLoginComCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciaisJson(USER_EMAIL, "senha-invalida")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    void deveNegarAcessoAMeSemToken() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/usuarios/me"));
    }

    @Test
    void devePermitirAcessoAMeComTokenValido() throws Exception {
        String token = autenticar(USER_EMAIL, PASSWORD);

        mockMvc.perform(get("/api/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.nome").value("Usuario Comum"));
    }

    @Test
    void deveNegarAcessoAdminParaUsuarioComum() throws Exception {
        String token = autenticar(USER_EMAIL, PASSWORD);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/admin/usuarios"));
    }

    @Test
    void devePermitirAcessoAdminParaUsuarioAdmin() throws Exception {
        String token = autenticar(ADMIN_EMAIL, PASSWORD);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].email", hasItems(USER_EMAIL, ADMIN_EMAIL)));
    }

    @Test
    void devePermitirEndpointPublicoSemToken() throws Exception {
        mockMvc.perform(get("/api/localidades/paises")
                        .param("termo", "a"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void deveNegarMetodoGetNoLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/login"));
    }

    @Test
    void deveNaoExporSwaggerNoProfileTest() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/swagger-ui/index.html"));
    }

    @Test
    void deveNaoExporH2ConsoleNoProfileTest() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/h2-console"));
    }

    @Test
    void deveManterAdminProtegidoPorPathMatcher() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/admin/usuarios"));
    }

    private Usuario criarUsuario(
            String cpf,
            String nome,
            String telefone,
            String email,
            boolean admin
    ) {
        Usuario usuario = new Usuario(cpf, nome, telefone, email, passwordEncoder.encode(PASSWORD));

        if (admin) {
            usuario.addProfile(Role.ADMIN);
        }

        return usuario;
    }

    private byte[] credenciaisJson(String email, String senha) throws Exception {
        return objectMapper.writeValueAsBytes(Map.of(
                "email", email,
                "password", senha
        ));
    }

    private String autenticar(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciaisJson(email, senha)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andReturn();

        return result.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
