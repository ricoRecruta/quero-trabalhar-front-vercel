package com.QueroTrabalhar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String LOGIN_PATH = "/login";
    private static final String CREDENTIALS_SCHEMA_NAME = "CredentialsDTO";
    private static final String STANDARD_ERROR_SCHEMA_NAME = "StandardError";

    private static final Set<String> PUBLIC_POST_PATHS = Set.of(
            LOGIN_PATH,
            "/api/usuarios/cadastrar"
    );

    private static final Set<String> PUBLIC_GET_PATHS = Set.of(
            "/api/localidades/paises",
            "/api/localidades/estados",
            "/api/localidades/cidades",
            "/api/empresas",
            "/api/empresas/{id}",
            "/api/empresas/{id}/recrutadores",
            "/api/empresas/{id}/oportunidades",
            "/api/oportunidades",
            "/api/oportunidades/{id}",
            "/api/tipos-de-emprego/aprovados",
            "/api/tipos-de-emprego/{id}"
    );

    @Bean
    public OpenAPI queroTrabalharOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quero Trabalhar API")
                        .description("API REST para gestão de empregabilidade, conectando candidatos, recrutadores, empresas, oportunidades de emprego, localidades e indicações.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Bearer Authentication com JWT no header Authorization.")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public OpenApiCustomizer queroTrabalharOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            openApi.getComponents()
                    .addSchemas(CREDENTIALS_SCHEMA_NAME, credentialsSchema())
                    .addSchemas(STANDARD_ERROR_SCHEMA_NAME, standardErrorSchema());

            if (openApi.getPaths() == null) {
                openApi.setPaths(new Paths());
            }

            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                if (isPublicOperation(path, httpMethod)) {
                    operation.setSecurity(List.of());
                }
            }));

            if (openApi.getPaths().get(LOGIN_PATH) == null) {
                openApi.path(LOGIN_PATH, new PathItem().post(loginOperation()));
            }
        };
    }

    private boolean isPublicOperation(String path, PathItem.HttpMethod httpMethod) {
        if (httpMethod == PathItem.HttpMethod.POST) {
            return PUBLIC_POST_PATHS.contains(path);
        }

        if (httpMethod == PathItem.HttpMethod.GET) {
            return PUBLIC_GET_PATHS.contains(path);
        }

        return false;
    }

    private Operation loginOperation() {
        return new Operation()
                .operationId("login")
                .summary("Autenticar usuário")
                .description("Autentica o usuário e retorna o token JWT no header Authorization.")
                .security(List.of())
                .requestBody(new RequestBody()
                        .required(true)
                        .description("Credenciais do usuário.")
                        .content(new Content().addMediaType(
                                MediaType.APPLICATION_JSON_VALUE,
                                new io.swagger.v3.oas.models.media.MediaType().schema(schemaRef(CREDENTIALS_SCHEMA_NAME))
                        )))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Autenticado com sucesso.")
                                .addHeaderObject("Authorization", new Header()
                                        .description("Token JWT no formato Bearer.")
                                        .schema(new StringSchema().example("Bearer eyJhbGciOiJIUzI1NiJ9.exemplo.assinatura"))))
                        .addApiResponse("401", new ApiResponse()
                                .description("E-mail ou senha inválidos.")
                                .content(new Content().addMediaType(
                                        MediaType.APPLICATION_JSON_VALUE,
                                        new io.swagger.v3.oas.models.media.MediaType().schema(schemaRef(STANDARD_ERROR_SCHEMA_NAME))
                                ))));
    }

    private Schema<?> credentialsSchema() {
        return new ObjectSchema()
                .name(CREDENTIALS_SCHEMA_NAME)
                .addRequiredItem("email")
                .addRequiredItem("password")
                .addProperty("email", new StringSchema()
                        .format("email")
                        .example("usuario@exemplo.com"))
                .addProperty("password", new StringSchema()
                        .format("password")
                        .example("123456"));
    }

    private Schema<?> standardErrorSchema() {
        return new ObjectSchema()
                .name(STANDARD_ERROR_SCHEMA_NAME)
                .addRequiredItem("timestamp")
                .addRequiredItem("status")
                .addRequiredItem("error")
                .addRequiredItem("message")
                .addRequiredItem("path")
                .addProperty("timestamp", new IntegerSchema()
                        .format("int64")
                        .example(1746727687000L))
                .addProperty("status", new IntegerSchema()
                        .format("int32")
                        .example(401))
                .addProperty("error", new StringSchema()
                        .example("Não autorizado"))
                .addProperty("message", new StringSchema()
                        .example("E-mail ou senha inválidos."))
                .addProperty("path", new StringSchema()
                        .example("/login"));
    }

    private Schema<?> schemaRef(String schemaName) {
        return new Schema<>().$ref("#/components/schemas/" + schemaName);
    }
}
