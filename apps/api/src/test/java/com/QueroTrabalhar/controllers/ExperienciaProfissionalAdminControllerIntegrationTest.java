package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.domain.entity.ExperienciaProfissional;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.Role;
import com.QueroTrabalhar.repository.ExperienciaProfissionalRepository;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
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
class ExperienciaProfissionalAdminControllerIntegrationTest {

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

    @Autowired
    private ExperienciaProfissionalRepository experienciaProfissionalRepository;

    @BeforeEach
    void limparBaseDoSlice() {
        experienciaProfissionalRepository.deleteAll();
        experienciaProfissionalRepository.flush();
        usuarioRepository.deleteAll();
        usuarioRepository.flush();
        tipoDeEmpregoRepository.deleteAll();
        tipoDeEmpregoRepository.flush();
    }

    @Test
    void deveListarExperienciasAdminEmEstruturaPaginadaComOrdenacaoPadraoPorIdDesc() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Experiencias", "admin-experiencias-" + indice + "@teste.com");

        TipoDeEmprego tipoBackend = persistirTipoDeEmprego("Backend " + indice);
        TipoDeEmprego tipoQa = persistirTipoDeEmprego("QA " + indice);

        Usuario candidatoA = persistirCandidato("Amanda Alpha", "amanda.alpha." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Bruno Beta", "bruno.beta." + indice + "@teste.com");

        ExperienciaProfissional experienciaMaisAntiga = persistirExperiencia(
                candidatoA,
                tipoBackend,
                "API interna",
                LocalDate.of(2021, 1, 10),
                LocalDate.of(2022, 2, 20)
        );
        ExperienciaProfissional experienciaMaisRecente = persistirExperiencia(
                candidatoB,
                tipoQa,
                "Automacao de testes",
                LocalDate.of(2023, 3, 15),
                null
        );

        String token = autenticar("admin-experiencias-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(experienciaMaisRecente.getId()))
                .andExpect(jsonPath("$.content[0].descricao").value(experienciaMaisRecente.getDescricao()))
                .andExpect(jsonPath("$.content[1].id").value(experienciaMaisAntiga.getId()))
                .andExpect(jsonPath("$.content[1].descricao").value(experienciaMaisAntiga.getDescricao()));
    }

    @Test
    void deveFiltrarExperienciasAdminPorTermo() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Termo", "admin-experiencias-termo-" + indice + "@teste.com");

        TipoDeEmprego tipoFiltrado = persistirTipoDeEmprego("Staff Engineer " + indice);
        TipoDeEmprego tipoNaoFiltrado = persistirTipoDeEmprego("Designer " + indice);

        Usuario candidatoA = persistirCandidato("Carla Termo", "carla.termo." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Diego Termo", "diego.termo." + indice + "@teste.com");

        ExperienciaProfissional experienciaFiltrada = persistirExperiencia(
                candidatoA,
                tipoFiltrado,
                "Lideranca tecnica de plataforma",
                LocalDate.of(2022, 1, 1),
                null
        );
        persistirExperiencia(
                candidatoB,
                tipoNaoFiltrado,
                "Discovery de produto",
                LocalDate.of(2022, 1, 1),
                null
        );

        String token = autenticar("admin-experiencias-termo-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "  staff  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(experienciaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].tipoDeEmprego").value(tipoFiltrado.getId()));
    }

    @Test
    void deveFiltrarExperienciasAdminPorTipoDeEmpregoId() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Tipo", "admin-experiencias-tipo-" + indice + "@teste.com");

        TipoDeEmprego tipoFiltrado = persistirTipoDeEmprego("Backend Especialista " + indice);
        TipoDeEmprego tipoNaoFiltrado = persistirTipoDeEmprego("Mobile " + indice);

        Usuario candidatoA = persistirCandidato("Elisa Tipo", "elisa.tipo." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Fabio Tipo", "fabio.tipo." + indice + "@teste.com");

        ExperienciaProfissional experienciaFiltrada = persistirExperiencia(
                candidatoA,
                tipoFiltrado,
                "Evolucao de APIs",
                LocalDate.of(2021, 4, 1),
                null
        );
        persistirExperiencia(
                candidatoB,
                tipoNaoFiltrado,
                "Aplicativo nativo",
                LocalDate.of(2021, 4, 1),
                null
        );

        String token = autenticar("admin-experiencias-tipo-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("tipoDeEmpregoId", tipoFiltrado.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].tipoDeEmprego").value(tipoFiltrado.getId()));
    }

    @Test
    void deveFiltrarExperienciasAdminPorEmAndamentoTrue() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Andamento True", "admin-experiencias-andamento-true-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("Dados " + indice);
        Usuario candidatoA = persistirCandidato("Gabriela Andamento", "gabriela.andamento." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Henrique Andamento", "henrique.andamento." + indice + "@teste.com");

        ExperienciaProfissional experienciaEmAndamento = persistirExperiencia(
                candidatoA,
                tipo,
                "Plataforma de dados",
                LocalDate.of(2023, 1, 1),
                null
        );
        persistirExperiencia(
                candidatoB,
                tipo,
                "Projeto encerrado",
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2022, 12, 31)
        );

        String token = autenticar("admin-experiencias-andamento-true-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("emAndamento", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaEmAndamento.getId()))
                .andExpect(jsonPath("$.content[0].dataFim").value(nullValue()));
    }

    @Test
    void deveFiltrarExperienciasAdminPorEmAndamentoFalse() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Andamento False", "admin-experiencias-andamento-false-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("Produto " + indice);
        Usuario candidatoA = persistirCandidato("Isabela Encerrada", "isabela.encerrada." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Joao Ativo", "joao.ativo." + indice + "@teste.com");

        ExperienciaProfissional experienciaEncerrada = persistirExperiencia(
                candidatoA,
                tipo,
                "Roadmap e backlog",
                LocalDate.of(2020, 5, 1),
                LocalDate.of(2021, 8, 31)
        );
        persistirExperiencia(
                candidatoB,
                tipo,
                "Produto em curso",
                LocalDate.of(2022, 9, 1),
                null
        );

        String token = autenticar("admin-experiencias-andamento-false-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("emAndamento", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaEncerrada.getId()))
                .andExpect(jsonPath("$.content[0].dataFim").value("2021-08-31"));
    }

    @Test
    void deveFiltrarExperienciasAdminPorDataInicioDe() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Data Inicio De", "admin-experiencias-data-inicio-de-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("Infra " + indice);
        Usuario candidatoA = persistirCandidato("Karen Data", "karen.data." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Leo Data", "leo.data." + indice + "@teste.com");

        persistirExperiencia(
                candidatoA,
                tipo,
                "Ambiente legado",
                LocalDate.of(2021, 2, 1),
                LocalDate.of(2021, 12, 1)
        );
        ExperienciaProfissional experienciaFiltrada = persistirExperiencia(
                candidatoB,
                tipo,
                "Cloud platform",
                LocalDate.of(2023, 6, 1),
                null
        );

        String token = autenticar("admin-experiencias-data-inicio-de-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("dataInicioDe", "2022-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].dataInicio").value("2023-06-01"));
    }

    @Test
    void deveFiltrarExperienciasAdminPorDataInicioAte() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Data Inicio Ate", "admin-experiencias-data-inicio-ate-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("Seguranca " + indice);
        Usuario candidatoA = persistirCandidato("Marina Inicio", "marina.inicio." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Nando Inicio", "nando.inicio." + indice + "@teste.com");

        ExperienciaProfissional experienciaFiltrada = persistirExperiencia(
                candidatoA,
                tipo,
                "Hardening",
                LocalDate.of(2020, 3, 1),
                LocalDate.of(2021, 7, 1)
        );
        persistirExperiencia(
                candidatoB,
                tipo,
                "Arquitetura zero trust",
                LocalDate.of(2024, 1, 1),
                null
        );

        String token = autenticar("admin-experiencias-data-inicio-ate-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("dataInicioAte", "2021-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].dataInicio").value("2020-03-01"));
    }

    @Test
    void deveFiltrarExperienciasAdminPorDataFimDe() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Data Fim De", "admin-experiencias-data-fim-de-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("SRE " + indice);
        Usuario candidatoA = persistirCandidato("Olivia Fim", "olivia.fim." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Paulo Fim", "paulo.fim." + indice + "@teste.com");

        persistirExperiencia(
                candidatoA,
                tipo,
                "On-call",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2021, 6, 30)
        );
        ExperienciaProfissional experienciaFiltrada = persistirExperiencia(
                candidatoB,
                tipo,
                "Confiabilidade",
                LocalDate.of(2021, 7, 1),
                LocalDate.of(2023, 4, 30)
        );

        String token = autenticar("admin-experiencias-data-fim-de-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("dataFimDe", "2022-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].dataFim").value("2023-04-30"));
    }

    @Test
    void deveFiltrarExperienciasAdminPorDataFimAte() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Data Fim Ate", "admin-experiencias-data-fim-ate-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("Suporte " + indice);
        Usuario candidatoA = persistirCandidato("Quenia Fim", "quenia.fim." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Rafael Fim", "rafael.fim." + indice + "@teste.com");

        ExperienciaProfissional experienciaFiltrada = persistirExperiencia(
                candidatoA,
                tipo,
                "Atendimento N2",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 5, 15)
        );
        persistirExperiencia(
                candidatoB,
                tipo,
                "Operacao 24x7",
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2023, 11, 30)
        );

        String token = autenticar("admin-experiencias-data-fim-ate-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("dataFimAte", "2021-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(experienciaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].dataFim").value("2020-05-15"));
    }

    @Test
    void deveIgnorarFiltrosNulosOuEmBrancoNaListagemAdminDeExperiencias() throws Exception {
        int indice = proximoIndice();
        persistirAdminAutenticavel(indice, "Admin Branco", "admin-experiencias-branco-" + indice + "@teste.com");

        TipoDeEmprego tipo = persistirTipoDeEmprego("Observabilidade " + indice);
        Usuario candidatoA = persistirCandidato("Sofia Branco", "sofia.branco." + indice + "@teste.com");
        Usuario candidatoB = persistirCandidato("Tiago Branco", "tiago.branco." + indice + "@teste.com");

        ExperienciaProfissional experienciaPrimeira = persistirExperiencia(
                candidatoA,
                tipo,
                "Dashboards e alertas",
                LocalDate.of(2021, 8, 1),
                null
        );
        ExperienciaProfissional experienciaSegunda = persistirExperiencia(
                candidatoB,
                tipo,
                "Tracing distribuido",
                LocalDate.of(2022, 9, 1),
                LocalDate.of(2024, 1, 10)
        );

        String token = autenticar("admin-experiencias-branco-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(experienciaSegunda.getId()))
                .andExpect(jsonPath("$.content[1].id").value(experienciaPrimeira.getId()));
    }

    @Test
    void deveNegarAcessoAoEndpointAdminDeExperienciasSemToken() throws Exception {
        mockMvc.perform(get("/api/admin/experiencias"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/admin/experiencias"));
    }

    @Test
    void deveNegarAcessoAoEndpointAdminDeExperienciasParaUsuarioComum() throws Exception {
        int indice = proximoIndice();
        persistirUsuarioSemAdmin("Usuario Comum", "usuario-comum-experiencias-" + indice + "@teste.com");
        String token = autenticar("usuario-comum-experiencias-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/admin/experiencias")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/admin/experiencias"));
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

    private Usuario persistirCandidato(String nome, String email) {
        int indice = proximoIndice();
        Usuario usuario = criarUsuario(indice, nome, email);
        usuario.adicionarPerfilCandidato(new PerfilCandidato(usuario));
        return usuarioRepository.saveAndFlush(usuario);
    }

    private TipoDeEmprego persistirTipoDeEmprego(String titulo) {
        return tipoDeEmpregoRepository.saveAndFlush(
                TipoDeEmprego.criarTipoDeEmpregoAdmin(titulo, "Descricao " + titulo)
        );
    }

    private ExperienciaProfissional persistirExperiencia(
            Usuario usuario,
            TipoDeEmprego tipoDeEmprego,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        ExperienciaProfissional experiencia = new ExperienciaProfissional(
                usuario.getPerfilCandidato(),
                tipoDeEmprego,
                descricao,
                dataInicio,
                dataFim
        );
        return experienciaProfissionalRepository.saveAndFlush(experiencia);
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
