package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.Role;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
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
class TipoDeEmpregoAdminControllerIntegrationTest {

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

    @Autowired
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @BeforeEach
    void limparBaseDoSlice() {
        usuarioRepository.deleteAll();
        usuarioRepository.flush();
        tipoDeEmpregoRepository.deleteAll();
        tipoDeEmpregoRepository.flush();
    }

    @Test
    void deveListarApenasTiposNaoAprovadosEmEstruturaPaginadaComOrdenacaoPadraoPorIdDesc() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Tipos", "admin-tipos-" + indice + "@teste.com");

        TipoDeEmprego tipoMaisAntigo = persistirTipoPendente(
                "Backend Pendente " + indice,
                "Descricao backend pendente " + indice
        );
        persistirTipoAprovado(
                "Backend Aprovado " + indice,
                "Descricao backend aprovado " + indice
        );
        TipoDeEmprego tipoMaisRecente = persistirTipoPendente(
                "QA Pendente " + indice,
                "Descricao qa pendente " + indice
        );

        String token = autenticar("admin-tipos-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(tipoMaisRecente.getId()))
                .andExpect(jsonPath("$.content[0].titulo").value(tipoMaisRecente.getTitulo()))
                .andExpect(jsonPath("$.content[0].aprovado").value(false))
                .andExpect(jsonPath("$.content[1].id").value(tipoMaisAntigo.getId()))
                .andExpect(jsonPath("$.content[1].titulo").value(tipoMaisAntigo.getTitulo()))
                .andExpect(jsonPath("$.content[1].aprovado").value(false));
    }

    @Test
    void deveFiltrarTiposNaoAprovadosPorTermoNoTitulo() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Termo Titulo", "admin-tipos-titulo-" + indice + "@teste.com");

        TipoDeEmprego tipoFiltrado = persistirTipoPendente(
                "Staff Engineer " + indice,
                "Descricao generica " + indice
        );
        persistirTipoPendente(
                "Product Designer " + indice,
                "Outra descricao " + indice
        );
        persistirTipoAprovado(
                "Staff Engineer Aprovado " + indice,
                "Descricao aprovada " + indice
        );

        String token = autenticar("admin-tipos-titulo-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "  staff  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(tipoFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].titulo").value(tipoFiltrado.getTitulo()))
                .andExpect(jsonPath("$.content[0].aprovado").value(false));
    }

    @Test
    void deveFiltrarTiposNaoAprovadosPorTermoNaDescricao() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Termo Descricao", "admin-tipos-descricao-" + indice + "@teste.com");

        TipoDeEmprego tipoFiltrado = persistirTipoPendente(
                "Backend " + indice,
                "Arquitetura orientada a eventos e mensageria " + indice
        );
        persistirTipoPendente(
                "Frontend " + indice,
                "Interface e experiencia do usuario " + indice
        );
        persistirTipoAprovado(
                "Dados " + indice,
                "Arquitetura orientada a eventos aprovada " + indice
        );

        String token = autenticar("admin-tipos-descricao-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "  mensageria  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(tipoFiltrado.getId()))
                .andExpect(jsonPath("$.content[0].descricao").value(tipoFiltrado.getDescricao()))
                .andExpect(jsonPath("$.content[0].aprovado").value(false));
    }

    @Test
    void deveIgnorarTermoEmBrancoNaListagemDeTiposNaoAprovados() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Branco", "admin-tipos-branco-" + indice + "@teste.com");

        TipoDeEmprego tipoPrimeiro = persistirTipoPendente(
                "Observabilidade " + indice,
                "Dashboards e alertas " + indice
        );
        TipoDeEmprego tipoSegundo = persistirTipoPendente(
                "Plataforma " + indice,
                "Ferramentas internas " + indice
        );
        persistirTipoAprovado(
                "Aprovado " + indice,
                "Nao deve aparecer " + indice
        );

        String token = autenticar("admin-tipos-branco-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(tipoSegundo.getId()))
                .andExpect(jsonPath("$.content[1].id").value(tipoPrimeiro.getId()));
    }

    @Test
    void deveIgnorarQueryParamAprovadoEManterApenasTiposNaoAprovados() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Aprovado Param", "admin-tipos-aprovado-" + indice + "@teste.com");

        TipoDeEmprego tipoPendente = persistirTipoPendente(
                "Pendente " + indice,
                "Descricao pendente " + indice
        );
        persistirTipoAprovado(
                "Aprovado " + indice,
                "Descricao aprovada " + indice
        );

        String token = autenticar("admin-tipos-aprovado-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("aprovado", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(tipoPendente.getId()))
                .andExpect(jsonPath("$.content[0].aprovado").value(false));
    }

    @Test
    void deveNegarAcessoAoEndpointAdminDeTiposNaoAprovadosSemToken() throws Exception {
        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/admin/tipos-emprego/nao-aprovados"));
    }

    @Test
    void deveNegarAcessoAoEndpointAdminDeTiposNaoAprovadosParaUsuarioComum() throws Exception {
        int indice = proximoIndice();
        persistirUsuarioSemAdmin("Usuario Comum", "usuario-comum-tipos-" + indice + "@teste.com");
        String token = autenticar("usuario-comum-tipos-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/tipos-emprego/nao-aprovados")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/admin/tipos-emprego/nao-aprovados"));
    }

    private int proximoIndice() {
        return SEQUENCIA.incrementAndGet();
    }

    private Usuario persistirAdminAutenticavel(int indice, String nome, String email) {
        Usuario usuario = criarUsuario(indice, nome, email);
        usuario.addProfile(Role.ADMIN);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private Usuario persistirUsuarioSemAdmin(String nome, String email) {
        int indice = proximoIndice();
        return usuarioRepository.saveAndFlush(criarUsuario(indice, nome, email));
    }

    private TipoDeEmprego persistirTipoPendente(String titulo, String descricao) {
        return tipoDeEmpregoRepository.saveAndFlush(
                TipoDeEmprego.criarTipoDeEmpregoSugeridoPeloUsuario(titulo, descricao)
        );
    }

    private TipoDeEmprego persistirTipoAprovado(String titulo, String descricao) {
        return tipoDeEmpregoRepository.saveAndFlush(
                TipoDeEmprego.criarTipoDeEmpregoAdmin(titulo, descricao)
        );
    }

    private Usuario criarUsuario(int indice, String nome, String email) {
        return new Usuario(
                gerarCpfValido(indice),
                nome,
                String.format("839%08d", indice),
                email,
                passwordEncoder.encode(SENHA)
        );
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
