# Quero Trabalhar Backend

## Descrição geral
O **Quero Trabalhar Backend** é uma API REST desenvolvida em **Java 21** com **Spring Boot** para apoiar o fluxo de empregabilidade entre candidatos, recrutadores, empresas e oportunidades de trabalho.

O foco do MVP atual é oferecer uma base consistente para cadastro, autenticação, gestão de perfis, publicação de oportunidades, indicação entre usuários e consulta pública de empresas e vagas já validadas.

## Objetivo do sistema
Conectar pessoas em busca de emprego a recrutadores e empresas, com contratos HTTP claros, segurança por JWT, documentação OpenAPI e regras de visibilidade que preservam a qualidade dos dados publicados.

## Funcionalidades principais
- Cadastro de usuários e autenticação com JWT.
- Gestão do usuário autenticado via recursos `/me`.
- Ativação e remoção de perfis de candidato e recrutador.
- Cadastro e consulta de empresas.
- Cadastro, atualização, remoção e consulta de oportunidades de emprego.
- Demonstração de interesse em vagas por candidatos.
- Gestão de experiências profissionais do perfil de candidato.
- Criação e consulta de indicações entre usuários.
- Catálogo de tipos de emprego aprovados e sugestão de novos tipos para moderação.
- Catálogo oficial de localidades com autocomplete para países, estados e cidades.
- Fluxos administrativos para moderação de usuários, experiências, tipos de emprego e vínculos entre recrutadores e empresas.

## Arquitetura e padrões adotados
O projeto segue uma arquitetura em camadas, com separação explícita de responsabilidades:

- **Controllers** finos, responsáveis pela borda HTTP.
- **Services** com a regra de negócio.
- **Repositories** com Spring Data JPA e suporte a `Specifications`.
- **Domain/Entities** para o modelo de persistência.
- **DTOs em `record`** para contratos de entrada, saída e filtros.
- **Security** para autenticação/autorização com JWT e regras de acesso.
- **Exceptions** com tratamento global e respostas padronizadas.

Padrões já presentes no MVP:

- validação na borda com Bean Validation;
- paginação com `Page`, `Pageable` e `@PageableDefault`;
- filtros em listagens principais;
- documentação OpenAPI com `@Tag`, `@Operation`, `@ApiResponses` e `@Schema`;
- tratamento global de erros com modelos padronizados.

## Stack tecnológica
- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA / Hibernate
- Spring Validation
- Spring Security
- JWT
- SpringDoc OpenAPI / Swagger UI
- H2 em desenvolvimento e testes
- Driver MySQL em runtime para ambientes com banco externo
- Docker (`Dockerfile`, `docker-compose.yml` e `.dockerignore`)

## Pré-requisitos
- Java 21
- Maven 3.9 ou superior
- Docker e Docker Compose, se optar pela execução em contêiner

> O build usa Maven Enforcer e valida versões mínimas de Java e Maven já na fase `validate`.

## Configuração de ambiente
A aplicação importa opcionalmente um arquivo `.env` na raiz do projeto:

```properties
spring.config.import=optional:file:.env[.properties]
```

Perfil padrão:

- `local` por default, quando `SPRING_PROFILES_ACTIVE` não é informado.

Banco por perfil:

- `local`: H2 em memória com `ddl-auto=update`;
- `test`: H2 em memória com `ddl-auto=create-drop`;
- `prod`: datasource externo via variáveis de ambiente.

Observações do perfil `local`:

- Swagger UI habilitado;
- H2 Console habilitado em `/h2-console`;
- base populada com dados de demonstração na inicialização;
- reprocessamento automático de `LocalidadePendente` habilitado por padrão.

### Variáveis relevantes

| Variável | Uso | Default local / compose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring | `local` |
| `JWT_SECRET` | Segredo do token JWT | fallback de desenvolvimento |
| `JWT_EXPIRATION` | Expiração do token em ms | `86400000` |
| `GOOGLE_MAPS_API_URL` | URL base da integração Google Maps | `https://maps.googleapis.com` |
| `GOOGLE_MAPS_API_PATH` | Caminho da API de geocoding | `/maps/api/geocode/json` |
| `GOOGLE_MAPS_API_KEY` | Chave da API externa usada na resolução de localidade | `dev-google-maps-key` no fallback da aplicação; `docker-compose.yml` também define fallback |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas no CORS | `http://localhost:5173` |
| `LOCALIDADE_PENDENTE_REPROCESSAMENTO_ENABLED` | Habilita o scheduler de reprocessamento | `true` |
| `LOCALIDADE_PENDENTE_REPROCESSAMENTO_FIXED_DELAY_MS` | Intervalo entre execuções do reprocessamento | `600000` |

### Variáveis adicionais para `prod`

No perfil `prod`, além das variáveis acima, o ambiente precisa fornecer:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Opcionalmente:

- `DB_DRIVER` (default: `com.mysql.cj.jdbc.Driver`)

> Sem uma `GOOGLE_MAPS_API_KEY` válida, fluxos de resolução automática por integração externa podem manter a localidade em estado pendente.

## Como executar localmente com Maven
1. Garanta que Java 21 e Maven 3.9+ estejam disponíveis no ambiente.
2. Ajuste o arquivo `.env`, se quiser sobrescrever os defaults.
3. Gere o artefato:

```bash
mvn -DskipTests package
```

4. Execute a aplicação:

```bash
java -jar target/quero-trabalhar-api-0.0.1-SNAPSHOT.jar
```

Aplicação disponível em:

- `http://localhost:8080`

Recursos úteis no perfil `local`:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:querotrabalhar-local`
  - usuário: `sa`
  - senha: em branco

## Como executar com Docker
O repositório já possui:

- `Dockerfile` com build em múltiplas etapas;
- `docker-compose.yml` com o serviço `quero-trabalhar-api`;
- `.dockerignore` para reduzir o contexto de build.

Para subir a API:

```bash
docker compose up --build
```

O `docker-compose.yml` atual:

- sobe o serviço `quero-trabalhar-api`;
- publica a porta `8080`;
- ativa o perfil `local`;
- injeta variáveis de JWT, CORS, Google Maps e reprocessamento com fallbacks de desenvolvimento.

Para encerrar:

```bash
docker compose down
```

> Se necessário, sobrescreva as variáveis no ambiente ou no arquivo `.env` antes de subir os contêineres.

## Como acessar Swagger/OpenAPI
A documentação da API é gerada com **SpringDoc/OpenAPI**.

Disponível apenas no perfil `local`:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

No estado atual do projeto:

- o esquema **Bearer JWT** está configurado na OpenAPI;
- o endpoint `POST /login` foi documentado manualmente;
- endpoints usam `@Tag`, `@Operation` e `@ApiResponses`;
- DTOs e modelos de erro usam `@Schema`;
- Swagger UI fica desabilitado em `test` e `prod`.

## Autenticação JWT
A autenticação é feita por `POST /login`.

Exemplo de payload:

```json
{
  "email": "usuario@exemplo.com",
  "password": "123456"
}
```

Comportamento atual:

- em caso de sucesso, o token é retornado no header `Authorization`;
- o formato do header é `Bearer <token>`;
- endpoints protegidos exigem `Authorization: Bearer <token>`;
- em caso de falha, a API retorna `401` com o modelo `StandardError`.

## Principais endpoints/módulos
Os grupos abaixo representam os módulos principais do MVP e seus endpoints mais relevantes:

| Módulo | Endpoints de referência |
| --- | --- |
| Autenticação e usuários | `POST /login`, `POST /api/usuarios/cadastrar`, `GET/PUT/DELETE /api/usuarios/me` |
| Perfis do usuário | `PUT/DELETE /api/usuarios/me/perfil-candidato`, `PUT/DELETE /api/usuarios/me/perfil-recrutador` |
| Perfil de candidato | `POST/DELETE /api/candidatos/me/interesses/vagas/{vagaId}`, `GET /api/candidatos/me/interesses/vagas` |
| Experiências profissionais | `GET/POST/DELETE /api/usuarios/me/perfil-candidato/experiencias` |
| Perfil de recrutador | `GET /api/recrutadores/me`, `GET /api/recrutadores/me/oportunidades`, `POST /api/recrutadores/me/empresa/{empresaId}/solicitar-vinculo`, `GET /api/recrutadores/me/empresa` |
| Empresas | `POST /api/empresas`, `GET /api/empresas`, `GET /api/empresas/{id}`, `GET /api/empresas/{id}/recrutadores`, `GET /api/empresas/{id}/oportunidades` |
| Oportunidades | `GET /api/oportunidades`, `GET /api/oportunidades/{id}`, `POST/PUT/DELETE /api/oportunidades/{id}` |
| Tipos de emprego | `GET /api/tipos-de-emprego/aprovados`, `GET /api/tipos-de-emprego/{id}`, `POST /api/tipos-de-emprego/sugerir` |
| Indicações | `POST /api/indicacoes`, `GET /api/indicacoes/me/dadas`, `GET /api/indicacoes/me/recebidas`, `DELETE /api/indicacoes/{id}` |
| Localidades | `GET /api/localidades/paises`, `GET /api/localidades/estados`, `GET /api/localidades/cidades` |
| Administração | `GET /api/admin/usuarios`, `GET /api/admin/experiencias`, `GET /api/admin/tipos-emprego/nao-aprovados`, `PATCH /api/admin/recrutadores/{recrutadorId}/empresa/aprovar`, `PATCH /api/admin/recrutadores/{recrutadorId}/empresa/recusar` |

> A tabela acima não esgota todos os endpoints. O contrato completo fica disponível via Swagger no ambiente local.

## Paginação e filtros
As principais listagens do projeto já utilizam:

- `Page`
- `Pageable`
- `@PageableDefault`
- filtros por DTO
- `Specifications`

Endpoints já paginados no MVP:

- `GET /api/oportunidades`
- `GET /api/empresas`
- `GET /api/empresas/{id}/oportunidades`
- `GET /api/recrutadores/me/oportunidades`
- `GET /api/candidatos/me/interesses/vagas`
- `GET /api/admin/usuarios`
- `GET /api/admin/experiencias`
- `GET /api/admin/tipos-emprego/nao-aprovados`

Parâmetros usuais:

- `page`
- `size`
- `sort`

Filtros variam por módulo. Exemplos já implementados:

- `termo`
- `tipoDeEmpregoId`
- `empresaId`
- `recrutadorId`
- `paisId`
- `estadoId`
- `cidadeId`
- `modalidade`
- `statusLocalidade`

Defaults relevantes no código atual:

- empresas públicas: `sort=nome,asc`
- demais listagens principais: `sort=id,desc`

## Módulo de Localidade
O módulo de localidade é uma parte central do MVP e precisa ser entendido em dois níveis:

### 1. Localidade validada
`Localidade` validada é a **fonte oficial** usada pelos recursos públicos.

Isso significa que:

- empresas públicas usam apenas localidade validada;
- oportunidades públicas usam apenas localidade validada;
- filtros públicos por país, estado e cidade operam sobre a base oficial;
- endpoints públicos não expõem detalhes internos de pendência.

### 2. LocalidadePendente
`LocalidadePendente` é um **fallback técnico temporário** e também uma **fila de resolução**.

Ela é usada quando:

- a localidade chega em texto livre;
- a base interna não resolve de forma conclusiva;
- a integração externa não retorna uma validação confiável;
- o sistema precisa manter o recurso salvo, mas ainda sem publicação pública.

Comportamento atual do MVP:

- recursos com localidade pendente **não aparecem** em endpoints públicos;
- endpoints autenticados do dono do recurso podem exibir:
  - `statusLocalidade`
  - `localidadeTextoOriginal`
  - `statusValidacaoLocalidade`
  - `motivoPendenciaLocalidade`
- o catálogo público `/api/localidades/*` expõe apenas a base oficial;
- a API não expõe fluxo público de confirmação manual de sugestão de localidade.

### Reprocessamento em segundo plano
Pendências são reprocessadas automaticamente em background por scheduler.

Variáveis de controle:

- `LOCALIDADE_PENDENTE_REPROCESSAMENTO_ENABLED`
- `LOCALIDADE_PENDENTE_REPROCESSAMENTO_FIXED_DELAY_MS`

No default atual:

- reprocessamento habilitado;
- intervalo de `600000 ms` (10 minutos).

## Tratamento de erros
O projeto possui tratamento global de exceções com `@ControllerAdvice`.

Modelos principais:

- `StandardError`
- `ValidationError`

Status frequentes na API:

- `400 Bad Request`: payload, filtro ou parâmetro inválido;
- `401 Unauthorized`: autenticação ausente, inválida ou expirada;
- `403 Forbidden`: acesso negado a recurso protegido;
- `404 Not Found`: recurso não encontrado;
- `409 Conflict`: conflito de dados;
- `422 Unprocessable Entity`: regra de negócio violada.

Além disso:

- falhas de autenticação JWT retornam resposta padronizada;
- erros de validação por campo retornam detalhes no modelo `ValidationError`;
- os modelos de erro estão documentados na OpenAPI.

## Testes e validações
O repositório possui testes unitários e de integração cobrindo, entre outros pontos:

- services;
- controllers;
- segurança;
- OpenAPI;
- localidade e reprocessamento de pendências.

Comandos úteis:

```bash
mvn validate
```

```bash
mvn test
```

```bash
mvn -DskipTests compile
```

## Roadmap / melhorias futuras
Itens que **não fazem parte do MVP atual**, mas já aparecem como evolução natural do projeto:

- confirmação manual de sugestão de localidade;
- notificação ao dono do recurso quando empresa ou oportunidade ficar oculta por pendência de localidade;
- maior visibilidade operacional sobre pendências e resolução;
- formalização de ownership de empresa além do fluxo atual de vínculo entre recrutador e empresa.

## Licença
Este projeto está licenciado sob a [MIT License](LICENSE).

## Desenvolvedores responsáveis

- **José Lúcio**
  - E-mail: `jose.lourenco@dcx.ufpb.br`
  - LinkedIn: [José Lúcio Lourenço Pereira](https://www.linkedin.com/in/josé-lúcio-lourenço-pereira-b4a5b1298)

- **Jefferson Bezerra**
  - E-mail: `jefferson.bezerra@dcx.ufpb.br`
  - LinkedIn: [Jefferson Bezera](https://www.linkedin.com/in/jefferson-bezerra-123451254/)

