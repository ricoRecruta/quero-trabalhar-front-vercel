package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.hasSize;
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
@Transactional
class UsuarioAdminControllerIntegrationTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();
    private static final String SENHA = "Senha@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limparSliceDeUsuarios() {
        usuarioRepository.deleteAll();
        usuarioRepository.flush();
    }

    @Test
    void deveListarUsuariosAdminEmEstruturaPaginadaComOrdenacaoPadraoPorNomeAsc() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-usuarios-" + indice + "@teste.com");
        Usuario usuarioBeta = persistirUsuario(
                proximoIndice(),
                "Bruno Beta",
                "bruno.beta." + indice + "@teste.com",
                false,
                false
        );
        Usuario usuarioAlpha = persistirUsuario(
                proximoIndice(),
                "Amanda Alpha",
                "amanda.alpha." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-usuarios-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id").value(usuarioAlpha.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(usuarioAlpha.getNome()))
                .andExpect(jsonPath("$.content[1].id").value(usuarioBeta.getId()))
                .andExpect(jsonPath("$.content[1].nome").value(usuarioBeta.getNome()));
    }

    @Test
    void deveFiltrarUsuariosAdminPorTermo() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-termo-" + indice + "@teste.com");
        Usuario usuarioFiltrado = persistirUsuario(
                proximoIndice(),
                "Amanda Alpha",
                "alpha." + indice + "@teste.com",
                false,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Bruno Beta",
                "bravo." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-termo-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "  alpha  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(usuarioFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].email").value(usuarioFiltrado.getEmail()));
    }

    @Test
    void deveFiltrarUsuariosAdminPorNome() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-nome-" + indice + "@teste.com");
        Usuario usuarioFiltrado = persistirUsuario(
                proximoIndice(),
                "Carla Souza",
                "carla.souza." + indice + "@teste.com",
                false,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Marina Costa",
                "marina.costa." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-nome-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("nome", "  Souza  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(usuarioFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(usuarioFiltrado.getNome()));
    }

    @Test
    void deveFiltrarUsuariosAdminPorEmail() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-email-" + indice + "@teste.com");
        Usuario usuarioFiltrado = persistirUsuario(
                proximoIndice(),
                "Diego Lima",
                "diego.lima." + indice + "@teste.com",
                false,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Elisa Rocha",
                "elisa.rocha." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-email-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("email", usuarioFiltrado.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(usuarioFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].email").value(usuarioFiltrado.getEmail()));
    }

    @Test
    void deveFiltrarUsuariosAdminPorCpf() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-cpf-" + indice + "@teste.com");
        Usuario usuarioFiltrado = persistirUsuario(
                proximoIndice(),
                "Fabio Melo",
                "fabio.melo." + indice + "@teste.com",
                false,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Giovana Prado",
                "giovana.prado." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-cpf-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("cpf", usuarioFiltrado.getCpf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(usuarioFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(usuarioFiltrado.getNome()));
    }

    @Test
    void deveFiltrarUsuariosAdminComPerfilCandidatoQuandoParametroForTrue() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-candidato-true-" + indice + "@teste.com");
        Usuario usuarioFiltrado = persistirUsuario(
                proximoIndice(),
                "Helena Candidata",
                "helena.candidata." + indice + "@teste.com",
                true,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Igor Sem Perfil",
                "igor.semperfil." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-candidato-true-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("temPerfilCandidato", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(usuarioFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(usuarioFiltrado.getNome()));
    }

    @Test
    void deveFiltrarUsuariosAdminSemPerfilCandidatoQuandoParametroForFalse() throws Exception {
        int indice = proximoIndice();
        Usuario admin = persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-candidato-false-" + indice + "@teste.com");
        Usuario usuarioSemPerfil = persistirUsuario(
                proximoIndice(),
                "Joao Sem Perfil",
                "joao.semperfil." + indice + "@teste.com",
                false,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Karen Candidata",
                "karen.candidata." + indice + "@teste.com",
                true,
                false
        );

        String token = autenticar("admin-candidato-false-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("temPerfilCandidato", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(usuarioSemPerfil.getId()))
                .andExpect(jsonPath("$.content[1].id").value(admin.getId()));
    }

    @Test
    void deveFiltrarUsuariosAdminComPerfilRecrutadorQuandoParametroForTrue() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-recrutador-true-" + indice + "@teste.com");
        Usuario usuarioFiltrado = persistirUsuario(
                proximoIndice(),
                "Larissa Recrutadora",
                "larissa.recrutadora." + indice + "@teste.com",
                false,
                true
        );
        persistirUsuario(
                proximoIndice(),
                "Marcos Sem Perfil",
                "marcos.semperfil." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-recrutador-true-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("temPerfilRecrutador", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(usuarioFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(usuarioFiltrado.getNome()));
    }

    @Test
    void deveFiltrarUsuariosAdminSemPerfilRecrutadorQuandoParametroForFalse() throws Exception {
        int indice = proximoIndice();
        Usuario admin = persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-recrutador-false-" + indice + "@teste.com");
        Usuario usuarioSemPerfil = persistirUsuario(
                proximoIndice(),
                "Nina Sem Perfil",
                "nina.semperfil." + indice + "@teste.com",
                false,
                false
        );
        persistirUsuario(
                proximoIndice(),
                "Otavio Recrutador",
                "otavio.recrutador." + indice + "@teste.com",
                false,
                true
        );

        String token = autenticar("admin-recrutador-false-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("temPerfilRecrutador", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(usuarioSemPerfil.getId()))
                .andExpect(jsonPath("$.content[1].id").value(admin.getId()));
    }

    @Test
    void deveIgnorarFiltrosNulosOuEmBrancoNaListagemAdminDeUsuarios() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "ZZZ Admin", "admin-branco-" + indice + "@teste.com");
        Usuario usuarioAlpha = persistirUsuario(
                proximoIndice(),
                "Alpha Usuario",
                "alpha.usuario." + indice + "@teste.com",
                false,
                false
        );
        Usuario usuarioBeta = persistirUsuario(
                proximoIndice(),
                "Beta Usuario",
                "beta.usuario." + indice + "@teste.com",
                false,
                false
        );

        String token = autenticar("admin-branco-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "   ")
                        .param("nome", "   ")
                        .param("email", "   ")
                        .param("cpf", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].id").value(usuarioAlpha.getId()))
                .andExpect(jsonPath("$.content[1].id").value(usuarioBeta.getId()));
    }

    private int proximoIndice() {
        return SEQUENCIA.incrementAndGet();
    }

    private Usuario persistirAdminAutenticavel(int indice, String nome, String email) {
        Usuario usuario = criarUsuario(indice, nome, email, false, false);
        usuario.addProfile(Role.ADMIN);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private Usuario persistirUsuario(
            int indice,
            String nome,
            String email,
            boolean temPerfilCandidato,
            boolean temPerfilRecrutador
    ) {
        return usuarioRepository.saveAndFlush(criarUsuario(
                indice,
                nome,
                email,
                temPerfilCandidato,
                temPerfilRecrutador
        ));
    }

    private Usuario criarUsuario(
            int indice,
            String nome,
            String email,
            boolean temPerfilCandidato,
            boolean temPerfilRecrutador
    ) {
        Usuario usuario = new Usuario(
                gerarCpfValido(indice),
                nome,
                String.format("839%08d", indice),
                email,
                passwordEncoder.encode(SENHA)
        );

        if (temPerfilCandidato) {
            usuario.adicionarPerfilCandidato(new PerfilCandidato(usuario));
        }

        if (temPerfilRecrutador) {
            usuario.adicionarPerfilRecrutador(new PerfilRecrutador(null, "Empresa Legada " + indice));
        }

        return usuario;
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

    private byte[] credenciaisJson(String email, String senha) throws Exception {
        return objectMapper.writeValueAsBytes(Map.of(
                "email", email,
                "password", senha
        ));
    }

    private String gerarCpfValido(int indice) {
        String base = String.format("%09d", indice);
        int primeiroDigito = calcularDigitoCpf(base, 10);
        int segundoDigito = calcularDigitoCpf(base + primeiroDigito, 11);
        return base + primeiroDigito + segundoDigito;
    }

    private int calcularDigitoCpf(String base, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;

        for (char caractere : base.toCharArray()) {
            soma += Character.getNumericValue(caractere) * peso;
            peso--;
        }

        int resto = 11 - (soma % 11);
        return resto >= 10 ? 0 : resto;
    }
}
