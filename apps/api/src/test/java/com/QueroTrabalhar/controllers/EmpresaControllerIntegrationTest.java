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
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmpresaControllerIntegrationTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @Autowired
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalidadePendenteRepository localidadePendenteRepository;

    @BeforeEach
    void limparSliceDeEmpresa() {
        for (OportunidadeDeEmprego oportunidade : oportunidadeDeEmpregoRepository.findAll()) {
            oportunidadeDeEmpregoRepository.removerTodosInteressesDaVaga(oportunidade.getId());
        }
        oportunidadeDeEmpregoRepository.deleteAll();
        oportunidadeDeEmpregoRepository.flush();
        empresaRepository.deleteAll();
        empresaRepository.flush();
        localidadePendenteRepository.deleteAll();
        localidadePendenteRepository.flush();
    }

    @Test
    void naoDeveListarEmpresasComLocalidadePendenteOuSemLocalidadeValidadaNaListagemPublica() throws Exception {
        int indice = proximoIndice();
        Empresa empresaValidada = persistirEmpresaValidada("Empresa Alpha " + indice);
        persistirEmpresaPendente("Empresa Oculta " + indice, "Regiao nao mapeada " + indice);
        persistirEmpresaSemLocalidadeValidada("Empresa Sem Localidade " + indice);

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(empresaValidada.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(empresaValidada.getNome()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveListarEmpresasPublicasComEstruturaPaginadaEOrdenacaoPadraoPorNomeAsc() throws Exception {
        int indice = proximoIndice();
        Empresa empresaBeta = persistirEmpresaValidada("Empresa Beta " + indice);
        Empresa empresaAlpha = persistirEmpresaValidada("Empresa Alpha " + indice);

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(empresaAlpha.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(empresaAlpha.getNome()))
                .andExpect(jsonPath("$.content[1].id").value(empresaBeta.getId()))
                .andExpect(jsonPath("$.content[1].nome").value(empresaBeta.getNome()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveFiltrarEmpresasPorTermoIgnorandoEspacosExternos() throws Exception {
        int indice = proximoIndice();
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);
        Empresa empresaFiltrada = persistirEmpresaValidada(
                "Plataforma Alpha " + indice,
                new Localidade(localidade.pais(), localidade.estado(), localidade.cidade())
        );
        persistirEmpresaValidada(
                "Consultoria Beta " + indice,
                new Localidade(localidade.pais(), localidade.estado(), localidade.cidade())
        );

        mockMvc.perform(get("/api/empresas")
                        .param("termo", "  alpha  "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(empresaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].nome").value(empresaFiltrada.getNome()));
    }

    @Test
    void deveFiltrarEmpresasPorPaisEstadoECidade() throws Exception {
        int indiceBase = proximoIndice();
        LocalidadePersistida localidadeAlvo = persistirLocalidadeCompleta(indiceBase);
        LocalidadePersistida outraLocalidade = persistirLocalidadeCompleta(proximoIndice());
        Empresa empresaFiltrada = persistirEmpresaValidada(
                "Empresa Localizada " + indiceBase,
                new Localidade(localidadeAlvo.pais(), localidadeAlvo.estado(), localidadeAlvo.cidade())
        );
        persistirEmpresaValidada(
                "Empresa Fora do Filtro " + indiceBase,
                new Localidade(outraLocalidade.pais(), outraLocalidade.estado(), outraLocalidade.cidade())
        );

        mockMvc.perform(get("/api/empresas")
                        .param("paisId", localidadeAlvo.pais().getId().toString())
                        .param("estadoId", localidadeAlvo.estado().getId().toString())
                        .param("cidadeId", localidadeAlvo.cidade().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(empresaFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].paisId").value(localidadeAlvo.pais().getId()))
                .andExpect(jsonPath("$.content[0].estadoId").value(localidadeAlvo.estado().getId()))
                .andExpect(jsonPath("$.content[0].cidadeId").value(localidadeAlvo.cidade().getId()));
    }

    @Test
    void deveIgnorarParametrosOpcionaisNulosOuEmBrancoNaListagemPublica() throws Exception {
        int indice = proximoIndice();
        Empresa empresaAlpha = persistirEmpresaValidada("Empresa Alpha Branco " + indice);
        Empresa empresaBeta = persistirEmpresaValidada("Empresa Beta Branco " + indice);

        mockMvc.perform(get("/api/empresas")
                        .param("termo", "   "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(empresaAlpha.getId()))
                .andExpect(jsonPath("$.content[1].id").value(empresaBeta.getId()));
    }

    @Test
    void deveBuscarEmpresaPublicaSemCamposTecnicosDePendencia() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Publica " + indice);

        mockMvc.perform(get("/api/empresas/{id}", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(empresa.getId()))
                .andExpect(jsonPath("$.nome").value(empresa.getNome()))
                .andExpect(jsonPath("$.statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void naoDeveRetornarEmpresaComLocalidadePendenteNoDetalhePublico() throws Exception {
        int indice = proximoIndice();
        Empresa empresaPendente = persistirEmpresaPendente(
                "Empresa Oculta Detalhe " + indice,
                "Regiao nao mapeada detalhe " + indice
        );

        mockMvc.perform(get("/api/empresas/{id}", empresaPendente.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoDevePermitirNavegacaoPublicaDeRecrutadoresParaEmpresaComLocalidadePendente() throws Exception {
        int indice = proximoIndice();
        Empresa empresaPendente = persistirEmpresaPendente(
                "Empresa Oculta Recrutadores " + indice,
                "Regiao nao mapeada recrutadores " + indice
        );

        mockMvc.perform(get("/api/empresas/{id}/recrutadores", empresaPendente.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarOportunidadesPublicasDaEmpresaComEstruturaPaginadaEOrdenacaoPadraoPorIdDesc() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Ordenacao " + indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend empresa", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego primeiraOportunidade = persistirOportunidadeValidada(
                "API publica",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );
        OportunidadeDeEmprego segundaOportunidade = persistirOportunidadeValidada(
                "API publica com Spring Boot",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(segundaOportunidade.getId()))
                .andExpect(jsonPath("$.content[1].id").value(primeiraOportunidade.getId()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveFiltrarOportunidadesDaEmpresaPorTermoIgnorandoEspacosExternos() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Termo " + indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend termo", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "Java 21 com Spring Boot",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );
        persistirOportunidadeValidada(
                "Analise de dados",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId())
                        .param("termo", "  spring boot  "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].descricao").value(oportunidadeFiltrada.getDescricao()));
    }

    @Test
    void deveFiltrarOportunidadesDaEmpresaPorTipoDeEmpregoId() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Tipo " + indice);
        TipoDeEmprego tipoDeEmpregoAlvo = persistirTipoDeEmprego("Backend alvo", indice);
        TipoDeEmprego outroTipoDeEmprego = persistirTipoDeEmprego("Frontend alvo", proximoIndice());
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API Java",
                Modalidade.REMOTO,
                tipoDeEmpregoAlvo,
                recrutador,
                empresa,
                localidade
        );
        persistirOportunidadeValidada(
                "SPA Angular",
                Modalidade.REMOTO,
                outroTipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId())
                        .param("tipoDeEmpregoId", tipoDeEmpregoAlvo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].tipoDeEmpregoId").value(tipoDeEmpregoAlvo.getId()));
    }

    @Test
    void deveFiltrarOportunidadesDaEmpresaPorRecrutadorId() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Recrutador " + indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend recrutador", indice);
        PerfilRecrutador recrutadorAlvo = persistirRecrutador(indice, "Marina Lima");
        PerfilRecrutador outroRecrutador = persistirRecrutador(proximoIndice(), "Carlos Melo");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API com ownership claro",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutadorAlvo,
                empresa,
                localidade
        );
        persistirOportunidadeValidada(
                "Aplicacao legado",
                Modalidade.REMOTO,
                tipoDeEmprego,
                outroRecrutador,
                empresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId())
                        .param("recrutadorId", recrutadorAlvo.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].recrutadorId").value(recrutadorAlvo.getId()));
    }

    @Test
    void deveFiltrarOportunidadesDaEmpresaPorPaisEstadoECidade() throws Exception {
        int indiceBase = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Localidade " + indiceBase);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend localidade", indiceBase);
        PerfilRecrutador recrutador = persistirRecrutador(indiceBase, "Marina Lima");
        LocalidadePersistida localidadeAlvo = persistirLocalidadeCompleta(indiceBase);
        LocalidadePersistida outraLocalidade = persistirLocalidadeCompleta(proximoIndice());

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API em cidade alvo",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidadeAlvo
        );
        persistirOportunidadeValidada(
                "API em outra cidade",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                empresa,
                outraLocalidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId())
                        .param("paisId", localidadeAlvo.pais().getId().toString())
                        .param("estadoId", localidadeAlvo.estado().getId().toString())
                        .param("cidadeId", localidadeAlvo.cidade().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].paisId").value(localidadeAlvo.pais().getId()))
                .andExpect(jsonPath("$.content[0].estadoId").value(localidadeAlvo.estado().getId()))
                .andExpect(jsonPath("$.content[0].cidadeId").value(localidadeAlvo.cidade().getId()));
    }

    @Test
    void deveFiltrarOportunidadesDaEmpresaPorModalidade() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Modalidade " + indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend modalidade", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeFiltrada = persistirOportunidadeValidada(
                "API remota",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );
        persistirOportunidadeValidada(
                "API presencial",
                Modalidade.PRESENCIAL,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId())
                        .param("modalidade", Modalidade.REMOTO.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeFiltrada.getId()))
                .andExpect(jsonPath("$.content[0].modalidade").value(Modalidade.REMOTO.name()));
    }

    @Test
    void naoDeveListarOportunidadesDeOutraEmpresaNoEndpointDaEmpresa() throws Exception {
        int indice = proximoIndice();
        Empresa empresaAlvo = persistirEmpresaValidada("Empresa Alvo " + indice);
        Empresa outraEmpresa = persistirEmpresaValidada("Empresa Fora " + proximoIndice());
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend empresa", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeDaEmpresaAlvo = persistirOportunidadeValidada(
                "Descricao compartilhada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresaAlvo,
                localidade
        );
        persistirOportunidadeValidada(
                "Descricao compartilhada",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                outraEmpresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresaAlvo.getId())
                        .param("termo", "compartilhada"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeDaEmpresaAlvo.getId()))
                .andExpect(jsonPath("$.content[0].empresaId").value(empresaAlvo.getId()));
    }

    @Test
    void deveIgnorarEmpresaIdDeClienteLegadoEManterEscopoDaEmpresaDoPath() throws Exception {
        int indice = proximoIndice();
        Empresa empresaDoPath = persistirEmpresaValidada("Empresa Path " + indice);
        Empresa empresaDoParametro = persistirEmpresaValidada("Empresa Parametro " + proximoIndice());
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend legado", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego oportunidadeDaEmpresaDoPath = persistirOportunidadeValidada(
                "Descricao legado",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresaDoPath,
                localidade
        );
        persistirOportunidadeValidada(
                "Descricao legado",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresaDoParametro,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresaDoPath.getId())
                        .param("empresaId", empresaDoParametro.getId().toString())
                        .param("termo", "legado"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeDaEmpresaDoPath.getId()))
                .andExpect(jsonPath("$.content[0].empresaId").value(empresaDoPath.getId()));
    }

    @Test
    void naoDeveListarOportunidadesComLocalidadePendenteOuSemLocalidadeValidadaNaListagemPublicaDaEmpresa()
            throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Pendencia " + indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend empresa", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");

        OportunidadeDeEmprego oportunidadeValidada = persistirOportunidadeValidada(
                "API publica",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresa
        );
        persistirOportunidadePendente(
                "Operacao em regiao nao mapeada",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                empresa,
                "Regiao nao mapeada " + indice
        );
        persistirOportunidadeSemLocalidadeValidada(
                "Oportunidade sem localidade validada",
                Modalidade.PRESENCIAL,
                tipoDeEmprego,
                recrutador,
                empresa
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(oportunidadeValidada.getId()))
                .andExpect(jsonPath("$.content[0].descricao").value(oportunidadeValidada.getDescricao()))
                .andExpect(jsonPath("$.content[0].statusLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].localidadeTextoOriginal").doesNotExist())
                .andExpect(jsonPath("$.content[0].statusValidacaoLocalidade").doesNotExist())
                .andExpect(jsonPath("$.content[0].motivoPendenciaLocalidade").doesNotExist());
    }

    @Test
    void deveIgnorarParametrosOpcionaisNulosOuEmBrancoNaListagemPublicaDeOportunidadesDaEmpresa() throws Exception {
        int indice = proximoIndice();
        Empresa empresa = persistirEmpresaValidada("Empresa Vagas Branco " + indice);
        TipoDeEmprego tipoDeEmprego = persistirTipoDeEmprego("Backend branco", indice);
        PerfilRecrutador recrutador = persistirRecrutador(indice, "Marina Lima");
        LocalidadePersistida localidade = persistirLocalidadeCompleta(indice);

        OportunidadeDeEmprego primeiraOportunidade = persistirOportunidadeValidada(
                "API alpha",
                Modalidade.REMOTO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );
        OportunidadeDeEmprego segundaOportunidade = persistirOportunidadeValidada(
                "API beta",
                Modalidade.HIBRIDO,
                tipoDeEmprego,
                recrutador,
                empresa,
                localidade
        );

        mockMvc.perform(get("/api/empresas/{id}/oportunidades", empresa.getId())
                        .param("termo", "   "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(segundaOportunidade.getId()))
                .andExpect(jsonPath("$.content[1].id").value(primeiraOportunidade.getId()));
    }

    private int proximoIndice() {
        return SEQUENCIA.incrementAndGet();
    }

    private TipoDeEmprego persistirTipoDeEmprego(String tituloBase, int indice) {
        return tipoDeEmpregoRepository.saveAndFlush(
                TipoDeEmprego.criarTipoDeEmpregoAdmin(
                        tituloBase + " " + indice,
                        tituloBase + " descricao " + indice
                )
        );
    }

    private PerfilRecrutador persistirRecrutador(int indice, String nome) {
        Usuario usuario = new Usuario(
                gerarCpfValido(indice),
                nome,
                String.format("839%08d", indice),
                "empresa-controller" + indice + "@teste.com",
                "Senha@123"
        );
        usuario.adicionarPerfilRecrutador(new PerfilRecrutador(null, "Empresa Legada " + indice));
        return usuarioRepository.saveAndFlush(usuario).getPerfilRecrutador();
    }

    private LocalidadePersistida persistirLocalidadeCompleta(int indice) {
        Pais pais = paisRepository.findFirstBySiglaIgnoreCase("BR")
                .orElseThrow(() -> new IllegalStateException("Pais BR nao encontrado no seed de teste."));
        Estado estado = estadoRepository.saveAndFlush(
                new Estado("Estado Empresa " + indice, String.format("E%03d", indice), pais)
        );
        Cidade cidade = cidadeRepository.saveAndFlush(new Cidade("Cidade Empresa " + indice, estado));
        return new LocalidadePersistida(pais, estado, cidade);
    }

    private Empresa persistirEmpresaValidada(String nome) {
        Pais pais = paisRepository.findFirstBySiglaIgnoreCase("BR")
                .orElseThrow(() -> new IllegalStateException("Pais BR nao encontrado no seed de teste."));

        Empresa empresa = new Empresa(nome, "Descricao publica", null, null, null, new Localidade(pais));
        return empresaRepository.saveAndFlush(empresa);
    }

    private Empresa persistirEmpresaValidada(String nome, Localidade localidade) {
        Empresa empresa = new Empresa(nome, "Descricao publica", null, null, null, localidade);
        return empresaRepository.saveAndFlush(empresa);
    }

    private Empresa persistirEmpresaPendente(String nome, String localidadeTextoOriginal) {
        Empresa empresa = new Empresa(nome, "Descricao pendente", null, null, null, null);
        Empresa empresaSalva = empresaRepository.saveAndFlush(empresa);
        localidadePendenteRepository.saveAndFlush(
                LocalidadePendente.criarPendenteInformadaPeloUsuario(
                        localidadeTextoOriginal,
                        "Aguardando validacao",
                        TipoRecursoLocalidadePendente.EMPRESA,
                        empresaSalva.getId(),
                        CampoLocalidadePendente.LOCALIDADE
                )
        );
        return empresaSalva;
    }

    private Empresa persistirEmpresaSemLocalidadeValidada(String nome) {
        return empresaRepository.saveAndFlush(new Empresa(nome, "Descricao sem localidade", null, null, null, null));
    }

    private OportunidadeDeEmprego persistirOportunidadeValidada(
            String descricao,
            Modalidade modalidade,
            TipoDeEmprego tipoDeEmprego,
            PerfilRecrutador recrutador,
            Empresa empresa
    ) {
        Pais pais = paisRepository.findFirstBySiglaIgnoreCase("BR")
                .orElseThrow(() -> new IllegalStateException("Pais BR nao encontrado no seed de teste."));

        return persistirOportunidadeValidada(
                descricao,
                modalidade,
                tipoDeEmprego,
                recrutador,
                empresa,
                new Localidade(pais)
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
        return persistirOportunidadeValidada(
                descricao,
                modalidade,
                tipoDeEmprego,
                recrutador,
                empresa,
                new Localidade(localidade.pais(), localidade.estado(), localidade.cidade())
        );
    }

    private OportunidadeDeEmprego persistirOportunidadeValidada(
            String descricao,
            Modalidade modalidade,
            TipoDeEmprego tipoDeEmprego,
            PerfilRecrutador recrutador,
            Empresa empresa,
            Localidade localidade
    ) {
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                descricao,
                tipoDeEmprego,
                modalidade,
                localidade,
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
