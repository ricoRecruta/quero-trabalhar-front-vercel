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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OportunidadeDeEmpregoControllerIntegrationTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

    @Autowired
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @Autowired
    private LocalidadePendenteRepository localidadePendenteRepository;

    @BeforeEach
    void limparSliceDeOportunidade() {
        for (OportunidadeDeEmprego oportunidade : oportunidadeDeEmpregoRepository.findAll()) {
            oportunidadeDeEmpregoRepository.removerTodosInteressesDaVaga(oportunidade.getId());
        }
        oportunidadeDeEmpregoRepository.deleteAll();
        oportunidadeDeEmpregoRepository.flush();
        localidadePendenteRepository.deleteAll();
        localidadePendenteRepository.flush();
    }

    @Test
    void deveListarOportunidadesComOrdenacaoPadraoPorIdDesc() throws Exception {
        int indice = proximoIndice();
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima", "Empresa Legada A");

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

        mockMvc.perform(get("/api/oportunidades"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(segundaOportunidade.getId()))
                .andExpect(jsonPath("$.content[1].id").value(primeiraOportunidade.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveFiltrarOportunidadesComFiltrosCombinadosDisponiveisNoDominio() throws Exception {
        int indiceBase = proximoIndice();
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indiceBase);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend Java", indiceBase);
        PerfilRecrutador recrutadorAlvo = persistirRecrutador(indiceBase, "Marina Lima", "Empresa Legada A");
        PerfilRecrutador outroRecrutador = persistirRecrutador(
                proximoIndice(),
                "Carlos Melo",
                "Empresa Legada B"
        );
        Empresa empresaAlvo = persistirEmpresa("Empresa Filtro", indiceBase);
        Empresa outraEmpresa = persistirEmpresa("Empresa Fora do Filtro", proximoIndice());

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "Spring Boot com Java 21",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutadorAlvo,
                empresaAlvo,
                localidade
        );
        persistirOportunidadeValidada(
                "Spring Boot legado",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutadorAlvo,
                outraEmpresa,
                localidade
        );
        persistirOportunidadeValidada(
                "Kotlin e arquitetura",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                outroRecrutador,
                empresaAlvo,
                localidade
        );

        mockMvc.perform(get("/api/oportunidades")
                        .param("termo", "  Spring Boot  ")
                        .param("tipoDeEmpregoId", tipoDeEmprego.getId().toString())
                        .param("empresaId", empresaAlvo.getId().toString())
                        .param("recrutadorId", recrutadorAlvo.getId().toString())
                        .param("paisId", localidade.pais().getId().toString())
                        .param("estadoId", localidade.estado().getId().toString())
                        .param("cidadeId", localidade.cidade().getId().toString())
                        .param("modalidade", Modalidade.REMOTO.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].empresaId").value(empresaAlvo.getId()))
                .andExpect(jsonPath("$.content[0].recrutadorId").value(recrutadorAlvo.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void naoDeveListarOportunidadesComLocalidadePendenteNaListagemPublica() throws Exception {
        int indice = proximoIndice();
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Frontend", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima", "Empresa Legada A");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeValidada = persistirOportunidadeValidada(
                "Angular presencial",
                Modalidade.PRESENCIAL,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        persistirOportunidadePendente(
                "React remoto",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                "Regiao Metropolitana",
                null
        );

        mockMvc.perform(get("/api/oportunidades"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeValidada.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveIgnorarParametroStatusLocalidadeLegadoSemExporOportunidadesPendentes() throws Exception {
        int indice = proximoIndice();
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Dados", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Joana Lima", "Empresa Legada B");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeValidada = persistirOportunidadeValidada(
                "Engenharia de dados",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );
        persistirOportunidadePendente(
                "Analise em campo",
                Modalidade.PRESENCIAL,
                tipoDeEmprego,
                recrutador,
                "Interior expandido",
                null
        );

        mockMvc.perform(get("/api/oportunidades")
                        .param("statusLocalidade", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeValidada.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveBuscarOportunidadePublicaSemCamposTecnicosDePendencia() throws Exception {
        int indice = proximoIndice();
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend publico", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima", "Empresa Legada A");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        OportunidadeDeEmprego oportunidade = persistirOportunidadeValidada(
                "API publica com Spring Boot",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                null,
                localidade
        );

        mockMvc.perform(get("/api/oportunidades/{id}", oportunidade.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(oportunidade.getId()))
                .andExpect(jsonPath("$.descricao").value(oportunidade.getDescricao()))
                .andExpect(jsonPath("$.statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void naoDeveRetornarOportunidadeComLocalidadePendenteNoDetalhePublico() throws Exception {
        int indice = proximoIndice();
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend oculto", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Joana Lima", "Empresa Legada B");
        OportunidadeDeEmprego oportunidadePendente = persistirOportunidadePendente(
                "Plataforma em regiao nao mapeada",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                "Regiao nao mapeada " + indice,
                null
        );

        mockMvc.perform(get("/api/oportunidades/{id}", oportunidadePendente.getId()))
                .andExpect(status().isNotFound());
    }

    private int proximoIndice() {
        return SEQUENCIA.incrementAndGet();
    }

    private LocalidadePersistida persistirLocalidadeCompleta(int indice) {
        Pais pais = paisRepository.findFirstBySiglaIgnoreCase("BR")
                .orElseThrow(() -> new IllegalStateException("Pais BR nao encontrado no seed de teste."));
        Estado estado = estadoRepository.saveAndFlush(
                new Estado("Estado Teste " + indice, String.format("T%03d", indice), pais)
        );
        Cidade cidade = cidadeRepository.saveAndFlush(new Cidade("Cidade Teste " + indice, estado));
        return new LocalidadePersistida(pais, estado, cidade);
    }

    private TipoDeEmprego persistirTipoDeEmprego(String tituloBase, int indice) {
        return tipoDeEmpregoRepository.saveAndFlush(
                TipoDeEmprego.criarTipoDeEmpregoAdmin(
                        tituloBase + " " + indice,
                        tituloBase + " descricao " + indice
                )
        );
    }

    private PerfilRecrutador persistirRecrutador(int indice, String nome, String empresaLegada) {
        Usuario usuario = new Usuario(
                gerarCpfValido(indice),
                nome,
                String.format("839%08d", indice),
                "recrutador" + indice + "@teste.com",
                "Senha@123"
        );
        usuario.adicionarPerfilRecrutador(new PerfilRecrutador(null, empresaLegada));
        return usuarioRepository.saveAndFlush(usuario).getPerfilRecrutador();
    }

    private Empresa persistirEmpresa(String nomeBase, int indice) {
        return empresaRepository.saveAndFlush(
                new Empresa(nomeBase + " " + indice, "Descricao " + indice, null, null, null, null)
        );
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
            String textoOriginal,
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

    private record LocalidadePersistida(Pais pais, Estado estado, Cidade cidade) {
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
