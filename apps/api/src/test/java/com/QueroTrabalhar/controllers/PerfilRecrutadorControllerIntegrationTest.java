package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
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
class PerfilRecrutadorControllerIntegrationTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();
    private static final String SENHA = "Senha@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

    @Autowired
    private LocalidadePendenteRepository localidadePendenteRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @BeforeEach
    void limparSliceDoRecrutador() {
        for (OportunidadeDeEmprego oportunidade : oportunidadeDeEmpregoRepository.findAll()) {
            oportunidadeDeEmpregoRepository.removerTodosInteressesDaVaga(oportunidade.getId());
        }
        oportunidadeDeEmpregoRepository.deleteAll();
        oportunidadeDeEmpregoRepository.flush();
        localidadePendenteRepository.deleteAll();
        localidadePendenteRepository.flush();
        empresaRepository.deleteAll();
        empresaRepository.flush();
        usuarioRepository.deleteAll();
        usuarioRepository.flush();
    }

    @Test
    void deveListarMinhasOportunidadesEmEstruturaPaginadaComOrdenacaoPadraoPorIdDesc() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Lima");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend", indice);

        OportunidadeDeEmprego primeiraOportunidade = persistirOportunidadeValidada(
                "Java 21",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        OportunidadeDeEmprego segundaOportunidade = persistirOportunidadeValidada(
                "Spring Boot",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(segundaOportunidade.getId()))
                .andExpect(jsonPath("$.content[1].id").value(primeiraOportunidade.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").value("VALIDADA"));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorTermoIgnorandoEspacosExternos() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Joana Lima");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend termo", indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "Spring Boot com Java 21",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        persistirOportunidadeValidada(
                "Kotlin com mensageria",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "  spring boot  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].descricao").value(oportunidadeFiltrada.getDescricao()));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorTipoDeEmpregoId() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Tipo");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoAlvo = persistirTipoDeEmprego("Backend alvo", indice);
        TipoDeEmprego outroTipo = persistirTipoDeEmprego("Frontend outro", proximoIndice());

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API Java",
                Modalidade.REMOTO,
                tipoAlvo,
                recrutador,
                null,
                localidade
        );
        persistirOportunidadeValidada(
                "SPA Angular",
                Modalidade.REMOTO,
                outroTipo,
                recrutador,
                null,
                localidade
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("tipoDeEmpregoId", tipoAlvo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].tipoDeEmpregoId").value(tipoAlvo.getId()));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorEmpresaId() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Empresa");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend empresa", indice);
        Empresa empresaAlvo = persistirEmpresa("Empresa Filtro", indice);
        Empresa outraEmpresa = persistirEmpresa("Empresa Fora", proximoIndice());

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API corporativa",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                empresaAlvo,
                localidade
        );
        persistirOportunidadeValidada(
                "API interna",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                outraEmpresa,
                localidade
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("empresaId", empresaAlvo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].empresaId").value(empresaAlvo.getId()));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorModalidade() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Modalidade");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend modalidade", indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API remota",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        persistirOportunidadeValidada(
                "API presencial",
                Modalidade.PRESENCIAL,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("modalidade", Modalidade.REMOTO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].modalidade").value(Modalidade.REMOTO.name()));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorPaisEstadoECidadeConsiderandoValidadas() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Localidade");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend localidade", indice);
        LocalidadePersistida localidadeAlvo = persistirLocalidadeCompleta(indice);
        LocalidadePersistida outraLocalidade = persistirLocalidadeCompleta(proximoIndice());

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API cidade alvo",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                localidadeAlvo
        );
        persistirOportunidadeValidada(
                "API outra cidade",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                outraLocalidade
        );
        persistirOportunidadePendente(
                "API pendente sem localidade validada",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                "Regiao pendente " + indice
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("paisId", localidadeAlvo.pais().getId().toString())
                        .param("estadoId", localidadeAlvo.estado().getId().toString())
                        .param("cidadeId", localidadeAlvo.cidade().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].paisId").value(localidadeAlvo.pais().getId()))
                .andExpect(jsonPath("$.content[0].estadoId").value(localidadeAlvo.estado().getId()))
                .andExpect(jsonPath("$.content[0].cidadeId").value(localidadeAlvo.cidade().getId()));
    }

    @Test
    void deveRetornarOportunidadesPendentesQuandoStatusLocalidadeNaoForInformado() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Pendente");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend pendente", indice);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeValidada = persistirOportunidadeValidada(
                "API validada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        OportunidadeDeEmprego oportunidadePendente = persistirOportunidadePendente(
                "API pendente",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                "Vale Imaginario " + indice
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadePendente.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").value("PENDENTE"))
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").value("Vale Imaginario " + indice))
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").value("PENDENTE_VALIDACAO"))
                .andExpect(jsonPath("$.content[1].id").value(oportunidadeValidada.getId()))
                .andExpect(jsonPath("$.content[1].statusLocalidade").value("VALIDADA"));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorStatusLocalidadeValidada() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Validada");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend validada", indice);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeValidada = persistirOportunidadeValidada(
                "API validada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        persistirOportunidadePendente(
                "API pendente",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                "Regiao pendente " + indice
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("statusLocalidade", "VALIDADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeValidada.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").value("VALIDADA"));
    }

    @Test
    void deveFiltrarMinhasOportunidadesPorStatusLocalidadePendente() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutador = persistirRecrutadorAutenticavel(indice, "Marina Somente Pendente");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend somente pendente", indice);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        persistirOportunidadeValidada(
                "API validada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        OportunidadeDeEmprego oportunidadePendente = persistirOportunidadePendente(
                "API pendente",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                "Serra do Sol " + indice
        );
        persistirOportunidadeSemLocalidadeValidada(
                "API sem localidade validada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("statusLocalidade", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadePendente.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").value("PENDENTE"))
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").value("Serra do Sol " + indice));
    }

    @Test
    void naoDeveRetornarOportunidadesDeOutroRecrutadorMesmoComRecrutadorIdNaQuery() throws Exception {
        int indice = proximoIndice();
        PerfilRecrutador recrutadorAutenticado = persistirRecrutadorAutenticavel(indice, "Marina Escopo");
        PerfilRecrutador outroRecrutador = persistirRecrutadorAutenticavel(proximoIndice(), "Diego Escopo");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend escopo", indice);

        OportunidadeDeEmprego oportunidadeDoAutenticado = persistirOportunidadeValidada(
                "Descricao compartilhada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutadorAutenticado,
                null,
                localidade
        );
        persistirOportunidadeValidada(
                "Descricao compartilhada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                outroRecrutador,
                null,
                localidade
        );

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("recrutadorId", outroRecrutador.getId().toString())
                        .param("termo", "compartilhada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeDoAutenticado.getId()))
                .andExpect(jsonPath("$.content[0].recrutadorId").value(recrutadorAutenticado.getId()));
    }

    @Test
    void deveRetornarBadRequestQuandoStatusLocalidadeForInvalido() throws Exception {
        int indice = proximoIndice();
        persistirRecrutadorAutenticavel(indice, "Marina Enum");
        String token = autenticar("recrutador-me-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/recrutadores/me/oportunidades")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("statusLocalidade", "INVALIDA"))
                .andExpect(status().isBadRequest());
    }

    private int proximoIndice() {
        return SEQUENCIA.incrementAndGet();
    }

    private PerfilRecrutador persistirRecrutadorAutenticavel(int indice, String nome) {
        Usuario usuario = new Usuario(
                gerarCpfValido(indice),
                nome,
                String.format("839%08d", indice),
                "recrutador-me-" + indice + "@teste.com",
                passwordEncoder.encode(SENHA)
        );
        usuario.adicionarPerfilRecrutador(new PerfilRecrutador(null, "Empresa Legada " + indice));
        return usuarioRepository.saveAndFlush(usuario).getPerfilRecrutador();
    }

    private TipoDeEmprego persistirTipoDeEmprego(String tituloBase, int indice) {
        return tipoDeEmpregoRepository.saveAndFlush(
                TipoDeEmprego.criarTipoDeEmpregoAdmin(
                        tituloBase + " " + indice,
                        tituloBase + " descricao " + indice
                )
        );
    }

    private Empresa persistirEmpresa(String nomeBase, int indice) {
        return empresaRepository.saveAndFlush(
                new Empresa(nomeBase + " " + indice, "Descricao " + indice, null, null, null, null)
        );
    }

    private LocalidadePersistida persistirLocalidadeCompleta(int indice) {
        Pais pais = paisRepository.findFirstBySiglaIgnoreCase("BR")
                .orElseThrow(() -> new IllegalStateException("Pais BR nao encontrado no seed de teste."));
        Estado estado = estadoRepository.saveAndFlush(
                new Estado("Estado Recrutador " + indice, String.format("R%03d", indice), pais)
        );
        Cidade cidade = cidadeRepository.saveAndFlush(new Cidade("Cidade Recrutador " + indice, estado));
        return new LocalidadePersistida(pais, estado, cidade);
    }

    private OportunidadeDeEmprego persistirOportunidadeValidada(
            String descricao,
            Modalidade modalidade,
            TipoDeEmprego tipoDeEmprego,
            PerfilRecrutador recrutador,
            Empresa empresa,
            LocalidadePersistida localidade
    ) {
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                descricao,
                tipoDeEmprego,
                modalidade,
                new Localidade(localidade.pais(), localidade.estado(), localidade.cidade()),
                recrutador,
                empresa
        );
        return oportunidadeDeEmpregoRepository.saveAndFlush(oportunidade);
    }

    private OportunidadeDeEmprego persistirOportunidadePendente(
            String descricao,
            Modalidade modalidade,
            TipoDeEmprego tipoDeEmprego,
            PerfilRecrutador recrutador,
            Empresa empresa,
            String textoOriginal
    ) {
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                descricao,
                tipoDeEmprego,
                modalidade,
                null,
                recrutador,
                empresa
        );
        OportunidadeDeEmprego oportunidadeSalva = oportunidadeDeEmpregoRepository.saveAndFlush(oportunidade);
        localidadePendenteRepository.saveAndFlush(
                LocalidadePendente.criarPendenteInformadaPeloUsuario(
                        textoOriginal,
                        "Aguardando validacao",
                        TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                        oportunidadeSalva.getId(),
                        CampoLocalidadePendente.LOCALIDADE
                )
        );
        return oportunidadeSalva;
    }

    private OportunidadeDeEmprego persistirOportunidadeSemLocalidadeValidada(
            String descricao,
            Modalidade modalidade,
            TipoDeEmprego tipoDeEmprego,
            PerfilRecrutador recrutador,
            Empresa empresa
    ) {
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                descricao,
                tipoDeEmprego,
                modalidade,
                null,
                recrutador,
                empresa
        );
        return oportunidadeDeEmpregoRepository.saveAndFlush(oportunidade);
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

    private record LocalidadePersistida(Pais pais, Estado estado, Cidade cidade) {
    }
}
