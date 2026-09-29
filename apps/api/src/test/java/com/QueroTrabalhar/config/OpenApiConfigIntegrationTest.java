package com.QueroTrabalhar.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OpenApiConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExporConfiguracaoBaseDaOpenApiNoProfileLocal() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.title").value("Quero Trabalhar API"))
                .andExpect(jsonPath("$.info.description").value("API REST para gestão de empregabilidade, conectando candidatos, recrutadores, empresas, oportunidades de emprego, localidades e indicações."))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/login'].post.summary").value("Autenticar usuário"))
                .andExpect(jsonPath("$.paths['/login'].post.description").value("Autentica o usuário e retorna o token JWT no header Authorization."))
                .andExpect(jsonPath("$.paths['/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/login'].post.requestBody.required").value(true))
                .andExpect(jsonPath("$.paths['/login'].post.requestBody.content['application/json'].schema['$ref']").value("#/components/schemas/CredentialsDTO"))
                .andExpect(jsonPath("$.paths['/login'].post.responses['200'].description").value("Autenticado com sucesso."))
                .andExpect(jsonPath("$.paths['/login'].post.responses['200'].headers.Authorization.description").value("Token JWT no formato Bearer."))
                .andExpect(jsonPath("$.paths['/login'].post.responses['401'].description").value("E-mail ou senha inválidos."))
                .andExpect(jsonPath("$.paths['/api/localidades/paises'].get.security").isEmpty())
                .andExpect(jsonPath("$.components.schemas.CredentialsDTO.properties.email.format").value("email"))
                .andExpect(jsonPath("$.components.schemas.StandardError.properties.message.example").value("E-mail ou senha inválidos."));
    }

    @Test
    void deveDocumentarUsuariosAutenticacaoEErrosNaOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/usuarios/cadastrar'].post.summary").value("Cadastrar usuário"))
                .andExpect(jsonPath("$.paths['/api/usuarios/cadastrar'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/usuarios/cadastrar'].post.responses['409'].description").value("Já existe um usuário com o e-mail informado."))
                .andExpect(jsonPath("$.paths['/api/usuarios/me'].get.responses['401'].description").value("Não autenticado."))
                .andExpect(jsonPath("$.paths['/api/usuarios/me/senha'].put.responses['422'].description").value("Regra de negócio violada ao alterar a senha."))
                .andExpect(jsonPath("$.paths['/api/admin/usuarios'].get.summary").value("Listar usuários"))
                .andExpect(jsonPath("$.paths['/api/admin/usuarios'].get.description").value("Lista usuários com paginação e filtros. Aceita os filtros termo, nome, email, cpf, temPerfilCandidato e temPerfilRecrutador. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=nome,asc."))
                .andExpect(jsonPath("$.paths['/api/admin/usuarios/{id}'].get.responses['403'].description").value("Sem permissão para acessar este recurso."))
                .andExpect(jsonPath("$.components.schemas.UsuarioRequestDTO.properties.senha.format").value("password"))
                .andExpect(jsonPath("$.paths['/api/admin/usuarios'].get.parameters[?(@.name=='temPerfilCandidato')].description").value(hasItem("Filtra usuários que possuem ou não perfil de candidato.")))
                .andExpect(jsonPath("$.paths['/api/admin/usuarios'].get.parameters[?(@.name=='temPerfilRecrutador')].description").value(hasItem("Filtra usuários que possuem ou não perfil de recrutador.")))
                .andExpect(jsonPath("$.components.schemas.ValidationError.properties.erros.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.FieldMessage.properties.fieldName.description").value("Nome do campo com erro."));
    }

    @Test
    void deveDocumentarEmpresasEOportunidadesNaOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/empresas'].get.summary").value("Listar empresas públicas"))
                .andExpect(jsonPath("$.paths['/api/empresas'].get.description").value("Lista empresas públicas com paginação e filtros. Aceita os filtros termo, paisId, estadoId e cidadeId. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=nome,asc. Os filtros de localidade consideram apenas empresas com localidade validada; empresas com localidade pendente ou sem localidade validada não aparecem neste endpoint."))
                .andExpect(jsonPath("$.paths['/api/empresas'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/empresas'].post.responses['401'].description").value("Não autenticado."))
                .andExpect(jsonPath("$.paths['/api/empresas'].post.responses['422'].description").value("Regra de negócio violada ao cadastrar a empresa."))
                .andExpect(jsonPath("$.paths['/api/empresas/{id}'].get.responses['404'].description").value("Empresa não encontrada ou indisponível nos endpoints públicos."))
                .andExpect(jsonPath("$.paths['/api/empresas/{id}/oportunidades'].get.description").value("Lista as oportunidades públicas de uma empresa com paginação e filtros. Aceita os filtros termo, tipoDeEmpregoId, recrutadorId, paisId, estadoId, cidadeId e modalidade. O escopo da empresa é definido pelo parâmetro de caminho id. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. Os filtros de localidade consideram apenas oportunidades com localidade validada; oportunidades com localidade pendente ou sem localidade validada não aparecem neste endpoint."))
                .andExpect(jsonPath("$.paths['/api/empresas/{id}/oportunidades'].get.parameters[?(@.name=='cidadeId')].description").value(hasItem("Filtra oportunidades públicas da empresa por cidade. Considera apenas recursos com localidade validada.")))
                .andExpect(jsonPath("$.paths['/api/oportunidades'].get.summary").value("Listar oportunidades públicas"))
                .andExpect(jsonPath("$.paths['/api/oportunidades'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/oportunidades'].get.parameters[?(@.name=='modalidade')].description").value(hasItem("Filtra oportunidades públicas pela modalidade.")))
                .andExpect(jsonPath("$.paths['/api/oportunidades/{id}'].get.responses['404'].description").value("Oportunidade não encontrada ou indisponível nos endpoints públicos."))
                .andExpect(jsonPath("$.paths['/api/oportunidades'].post.responses['401'].description").value("Não autenticado."))
                .andExpect(jsonPath("$.paths['/api/oportunidades'].post.responses['422'].description").value("Regra de negócio violada ao cadastrar a oportunidade."))
                .andExpect(jsonPath("$.paths['/api/oportunidades/{id}'].put.description").value("Atualiza uma oportunidade do recrutador autenticado. Nesta subfase, o contexto original de publicação é preservado: o payload não converte a oportunidade entre publicação pessoal e publicação em nome de empresa. A localidade pode ser atualizada por IDs estruturados ou por texto livre; se ficar pendente, a resposta interna retorna os campos de status para o dono do recurso e a oportunidade deixa de aparecer nos endpoints públicos até possuir localidade validada."))
                .andExpect(jsonPath("$.paths['/api/oportunidades/{id}'].delete.responses['422'].description").value("Regra de negócio violada ao remover a oportunidade."))
                .andExpect(jsonPath("$.components.schemas.EmpresaRequestDTO.properties.localidadeTexto.description").value("Texto livre da localidade. Use quando não houver IDs estruturados. Se a resolução automática não validar a localidade, a empresa será criada com localidade pendente no fluxo interno e não aparecerá nos endpoints públicos. A confirmação manual da sugestão ainda não está implementada no MVP."))
                .andExpect(jsonPath("$.components.schemas.EmpresaResponseDTO.properties.statusLocalidade.description").value("Status interno da localidade da empresa. Campo de apoio ao dono do recurso e não utilizado nos endpoints públicos."))
                .andExpect(jsonPath("$.components.schemas.EmpresaResponseDTO.properties.statusValidacaoLocalidade.description").value("Status detalhado da validação interna da localidade pendente. Campo interno do fluxo autenticado. No MVP atual, estados de confirmação ou recusa manual permanecem reservados para evolução futura e não indicam funcionalidade pública já disponível."))
                .andExpect(jsonPath("$.components.schemas.OportunidadeDeEmpregoResponseDTO.properties.statusValidacaoLocalidade.description").value("Status detalhado da validação interna da localidade pendente. Campo interno do dono do recurso. No MVP atual, estados de confirmação ou recusa manual permanecem reservados para evolução futura e não indicam funcionalidade pública já disponível."))
                .andExpect(jsonPath("$.components.schemas.RecrutadorDaEmpresaResponseDTO.properties.statusVinculoEmpresa.description").value("Status atual do vínculo do recrutador com a empresa."));
    }

    @Test
    void deveDocumentarLocalidadesTiposDeEmpregoEIndicacoesNaOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/localidades/paises'].get.summary").value("Listar países oficiais"))
                .andExpect(jsonPath("$.paths['/api/localidades/paises'].get.description").value("Consulta o catálogo oficial de países disponíveis na base para autocomplete. Usa o parâmetro termo e retorna lista vazia quando a busca tiver menos de 2 caracteres."))
                .andExpect(jsonPath("$.paths['/api/localidades/paises'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/localidades/paises'].get.parameters[?(@.name=='termo')].description").value(hasItem("Trecho do nome do país usado no autocomplete oficial. Quando tiver menos de 2 caracteres, o endpoint retorna lista vazia.")))
                .andExpect(jsonPath("$.paths['/api/localidades/estados'].get.parameters[?(@.name=='paisId')].description").value(hasItem("Identificador do país já existente na base oficial para restringir o catálogo de estados.")))
                .andExpect(jsonPath("$.paths['/api/localidades/cidades'].get.responses['400'].description").value("Parâmetros inválidos ou estado não encontrado na base oficial."))
                .andExpect(jsonPath("$.components.schemas.PaisResponseDTO.properties.sigla.description").value("Sigla do país na base oficial."))
                .andExpect(jsonPath("$.components.schemas.CidadeResponseDTO.properties.estado.description").value("Identificador do estado associado à cidade, mantendo o nome de campo JSON atual."))
                .andExpect(jsonPath("$.paths['/api/tipos-de-emprego/aprovados'].get.summary").value("Listar tipos de emprego aprovados"))
                .andExpect(jsonPath("$.paths['/api/tipos-de-emprego/aprovados'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/tipos-de-emprego/{id}'].get.responses['404'].description").value("Tipo de emprego aprovado não encontrado."))
                .andExpect(jsonPath("$.paths['/api/tipos-de-emprego/sugerir'].post.description").value("Cria uma sugestão de tipo de emprego ainda não aprovado para a fila de moderação administrativa. No comportamento atual da API, este endpoint exige autenticação."))
                .andExpect(jsonPath("$.paths['/api/tipos-de-emprego/sugerir'].post.responses['401'].description").value("Não autenticado."))
                .andExpect(jsonPath("$.paths['/api/admin/tipos-emprego/nao-aprovados'].get.description").value("Lista a fila administrativa de moderação dos tipos de emprego ainda não aprovados. Aceita o filtro termo. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. O backend aplica obrigatoriamente o filtro aprovado=false e ignora qualquer tentativa do cliente de controlar esse estado."))
                .andExpect(jsonPath("$.paths['/api/admin/tipos-emprego/nao-aprovados'].get.parameters[?(@.name=='termo')].description").value(hasItem("Busca textual aplicada ao título e à descrição dos tipos pendentes.")))
                .andExpect(jsonPath("$.paths['/api/admin/tipos-emprego/nao-aprovados'].get.responses['403'].description").value("Sem permissão para acessar este recurso administrativo."))
                .andExpect(jsonPath("$.paths['/api/admin/tipos-emprego/aprovar-lote'].patch.requestBody.description").value("Array JSON simples com os IDs numéricos das sugestões pendentes a aprovar em lote."))
                .andExpect(jsonPath("$.paths['/api/admin/tipos-emprego/aprovar-lote'].patch.requestBody.content['application/json'].schema.type").value("array"))
                .andExpect(jsonPath("$.paths['/api/admin/tipos-emprego/aprovar-lote'].patch.requestBody.content['application/json'].schema.items.type").value("integer"))
                .andExpect(jsonPath("$.components.schemas.TipoDeEmpregoResponseDTO.properties.aprovado.description").value("Indica se o tipo de emprego já faz parte do catálogo aprovado."))
                .andExpect(jsonPath("$.paths['/api/indicacoes'].post.summary").value("Criar indicação"))
                .andExpect(jsonPath("$.paths['/api/indicacoes'].post.responses['422'].description").value("Regra de negócio violada ao criar a indicação."))
                .andExpect(jsonPath("$.paths['/api/indicacoes/me/dadas'].get.description").value("Lista as indicações criadas pelo usuário autenticado. Opera sobre o recurso /me e mantém o contrato atual de lista simples, sem paginação."))
                .andExpect(jsonPath("$.paths['/api/indicacoes/me/recebidas'].get.responses['401'].description").value("Não autenticado."))
                .andExpect(jsonPath("$.paths['/api/indicacoes/{id}'].delete.responses['422'].description").value("Regra de negócio violada ao remover a indicação."))
                .andExpect(jsonPath("$.components.schemas.IndicacaoRequestDTO.properties.usuarioIndicadoId.description").value("Identificador do usuário que receberá a indicação."))
                .andExpect(jsonPath("$.components.schemas.PerfilRecrutadorUsuarioRequestDTO.description").value("Dados para adicionar ou atualizar o perfil de recrutador de um usuário, tanto em fluxo administrativo quanto no fluxo do próprio usuário autenticado."))
                .andExpect(jsonPath("$.components.schemas.IndicacaoResponseDTO.properties.autorNome.description").value("Nome do usuário autor da indicação."));
    }
}
