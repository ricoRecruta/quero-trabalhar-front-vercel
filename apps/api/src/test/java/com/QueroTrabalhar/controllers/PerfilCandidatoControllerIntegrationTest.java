package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
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
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
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
class PerfilCandidatoControllerIntegrationTest {

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
    private PerfilCandidatoRepository perfilCandidatoRepository;

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
    void limparSliceDoCandidato() {
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
    void deveListarMinhasVagasDeInteresseEmEstruturaPaginadaComOrdenacaoPadraoPorIdDesc() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Lima");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Lima", "Empresa Legada A");
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
        registrarInteresse(candidato, primeiraOportunidade);
        registrarInteresse(candidato, segundaOportunidade);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
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
    void deveFiltrarMinhasVagasDeInteressePorTermoIgnorandoEspacosExternos() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Termo");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Lima", "Empresa Legada A");
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
        OportunidadeDeEmprego oportunidadeFora = persistirOportunidadeValidada(
                "Kotlin com mensageria",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        registrarInteresse(candidato, oportunidadeFiltrada);
        registrarInteresse(candidato, oportunidadeFora);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "  spring boot  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].descricao").value(oportunidadeFiltrada.getDescricao()));
    }

    @Test
    void deveFiltrarMinhasVagasDeInteressePorTipoDeEmpregoId() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Tipo");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Tipo", "Empresa Legada A");
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
        OportunidadeDeEmprego oportunidadeFora = persistirOportunidadeValidada(
                "SPA Angular",
                Modalidade.REMOTO,
                outroTipo,
                recrutador,
                null,
                localidade
        );
        registrarInteresse(candidato, oportunidadeFiltrada);
        registrarInteresse(candidato, oportunidadeFora);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("tipoDeEmpregoId", tipoAlvo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].tipoDeEmpregoId").value(tipoAlvo.getId()));
    }

    @Test
    void deveFiltrarMinhasVagasDeInteressePorEmpresaId() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Empresa");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Empresa", "Empresa Legada A");
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
        OportunidadeDeEmprego oportunidadeFora = persistirOportunidadeValidada(
                "API interna",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                outraEmpresa,
                localidade
        );
        registrarInteresse(candidato, oportunidadeFiltrada);
        registrarInteresse(candidato, oportunidadeFora);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("empresaId", empresaAlvo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].empresaId").value(empresaAlvo.getId()));
    }

    @Test
    void deveFiltrarMinhasVagasDeInteressePorRecrutadorId() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Recrutador");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutadorAlvo = persistirRecrutador(indice, "Marina Lima", "Empresa Legada A");
        PerfilRecrutador outroRecrutador = persistirRecrutador(proximoIndice(), "Carlos Melo", "Empresa Legada B");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend recrutador", indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API Java com mesmo termo",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutadorAlvo,
                null,
                localidade
        );
        OportunidadeDeEmprego oportunidadeFora = persistirOportunidadeValidada(
                "API Java com mesmo termo",
                Modalidade.REMOTO,
                tipoDeEmprego,
                outroRecrutador,
                null,
                localidade
        );
        registrarInteresse(candidato, oportunidadeFiltrada);
        registrarInteresse(candidato, oportunidadeFora);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("recrutadorId", recrutadorAlvo.getId().toString())
                        .param("termo", "mesmo termo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].recrutadorId").value(recrutadorAlvo.getId()));
    }

    @Test
    void deveFiltrarMinhasVagasDeInteressePorModalidade() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Modalidade");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Modalidade", "Empresa Legada A");
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
        OportunidadeDeEmprego oportunidadeFora = persistirOportunidadeValidada(
                "API presencial",
                Modalidade.PRESENCIAL,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        registrarInteresse(candidato, oportunidadeFiltrada);
        registrarInteresse(candidato, oportunidadeFora);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("modalidade", Modalidade.REMOTO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].modalidade").value(Modalidade.REMOTO.name()));
    }

    @Test
    void deveFiltrarMinhasVagasDeInteressePorPaisEstadoECidadeConsiderandoValidadas() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Localidade");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Localidade", "Empresa Legada A");
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
        OportunidadeDeEmprego oportunidadeFora = persistirOportunidadeValidada(
                "API outra cidade",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                outraLocalidade
        );
        OportunidadeDeEmprego oportunidadePendente = persistirOportunidadePendente(
                "API pendente sem localidade validada",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                "Regiao pendente " + indice
        );
        registrarInteresse(candidato, oportunidadeFiltrada);
        registrarInteresse(candidato, oportunidadeFora);
        registrarInteresse(candidato, oportunidadePendente);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
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
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Pendente");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Pendente", "Empresa Legada A");
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
        registrarInteresse(candidato, oportunidadeValidada);
        registrarInteresse(candidato, oportunidadePendente);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
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
    void deveFiltrarMinhasVagasDeInteressePorStatusLocalidadeValidada() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Validada");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Validada", "Empresa Legada A");
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
        OportunidadeDeEmprego oportunidadePendente = persistirOportunidadePendente(
                "API pendente",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                "Regiao pendente " + indice
        );
        registrarInteresse(candidato, oportunidadeValidada);
        registrarInteresse(candidato, oportunidadePendente);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("statusLocalidade", "VALIDADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeValidada.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").value("VALIDADA"));
    }

    @Test
    void deveFiltrarMinhasVagasDeInteressePorStatusLocalidadePendente() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Somente Pendente");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Somente Pendente", "Empresa Legada A");
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend somente pendente", indice);
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
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                "Serra do Sol " + indice
        );
        OportunidadeDeEmprego oportunidadeSemPendencia = persistirOportunidadeSemLocalidadeValidada(
                "API sem localidade validada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null
        );
        registrarInteresse(candidato, oportunidadeValidada);
        registrarInteresse(candidato, oportunidadePendente);
        registrarInteresse(candidato, oportunidadeSemPendencia);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("statusLocalidade", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadePendente.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").value("PENDENTE"))
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").value("Serra do Sol " + indice));
    }

    @Test
    void naoDeveRetornarOportunidadesSemInteresseDoCandidatoAutenticado() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidato = persistirCandidatoAutenticavel(indice, "Joana Escopo");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Escopo", "Empresa Legada A");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend escopo", indice);

        OportunidadeDeEmprego oportunidadeComInteresse = persistirOportunidadeValidada(
                "Descricao compartilhada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        OportunidadeDeEmprego oportunidadeSemInteresse = persistirOportunidadeValidada(
                "Descricao compartilhada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        registrarInteresse(candidato, oportunidadeComInteresse);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "compartilhada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeComInteresse.getId()));
    }

    @Test
    void naoDeveRetornarInteressesDeOutroCandidato() throws Exception {
        int indice = proximoIndice();
        PerfilCandidato candidatoAutenticado = persistirCandidatoAutenticavel(indice, "Joana Dona");
        PerfilCandidato outroCandidato = persistirCandidatoAutenticavel(proximoIndice(), "Diego Outro");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);
        PerfilRecrutador recrutador = persistirRecrutador(proximoIndice(), "Marina Outro", "Empresa Legada A");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend outro candidato", indice);

        OportunidadeDeEmprego oportunidadeDoAutenticado = persistirOportunidadeValidada(
                "Descricao comum",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        OportunidadeDeEmprego oportunidadeDoOutro = persistirOportunidadeValidada(
                "Descricao comum",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        registrarInteresse(candidatoAutenticado, oportunidadeDoAutenticado);
        registrarInteresse(outroCandidato, oportunidadeDoOutro);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("termo", "descricao comum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeDoAutenticado.getId()));
    }

    @Test
    void deveRetornarBadRequestQuandoStatusLocalidadeForInvalido() throws Exception {
        int indice = proximoIndice();
        persistirCandidatoAutenticavel(indice, "Joana Enum");
        String token = autenticar("candidato-me-" + indice + "@teste.com", SENHA);

        mockMvc.perform(get("/api/candidatos/me/interesses/vagas")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .param("statusLocalidade", "INVALIDA"))
                .andExpect(status().isBadRequest());
    }

    private int proximoIndice() {
        return SEQUENCIA.incrementAndGet();
    }

    private PerfilCandidato persistirCandidatoAutenticavel(int indice, String nome) {
        Usuario usuario = new Usuario(
                gerarCpfValido(indice),
                nome,
                String.format("839%08d", indice),
                "candidato-me-" + indice + "@teste.com",
                passwordEncoder.encode(SENHA)
        );
        usuario.adicionarPerfilCandidato(new PerfilCandidato(usuario));
        return usuarioRepository.saveAndFlush(usuario).getPerfilCandidato();
    }

    private PerfilRecrutador persistirRecrutador(int indice, String nome, String empresaLegada) {
        Usuario usuario = new Usuario(
                gerarCpfValido(indice + 5000),
                nome,
                String.format("838%08d", indice),
                "recrutador-candidato-" + indice + "@teste.com",
                passwordEncoder.encode(SENHA)
        );
        usuario.adicionarPerfilRecrutador(new PerfilRecrutador(null, empresaLegada));
        return usuarioRepository.saveAndFlush(usuario).getPerfilRecrutador();
    }

    private void registrarInteresse(PerfilCandidato candidato, OportunidadeDeEmprego oportunidade) {
        candidato.demonstrarInteresse(oportunidade);
        perfilCandidatoRepository.saveAndFlush(candidato);
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
                new Estado("Estado Candidato " + indice, String.format("C%03d", indice), pais)
        );
        Cidade cidade = cidadeRepository.saveAndFlush(new Cidade("Cidade Candidato " + indice, estado));
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
