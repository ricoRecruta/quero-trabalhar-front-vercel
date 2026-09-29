# Testes - Quero Trabalhar Backend

## Objetivo

Este documento descreve a suíte de testes automatizados do backend do Quero Trabalhar. O foco atual da suíte é validar regras de negócio, segurança, controllers HTTP, configuração, localidade, paginação e filtros, domínio e parte do tratamento de erros.

## Stack de testes

- **JUnit 5**: base da suíte.
- **Mockito**: predominante nos testes unitários de service, com `@ExtendWith(MockitoExtension.class)`.
- **Spring Boot Test**: usado nos testes de integração com contexto Spring.
- **MockMvc**: usado para validar contrato HTTP, segurança e documentação OpenAPI.
- **H2 Database**: usado em memória nos profiles `test` e `local`.
- **Maven / Surefire**: execução via ciclo `test` do Maven, com relatórios em `target/surefire-reports`.
- **Spring Security Test**: a dependência está declarada no `pom.xml`, mas a suíte atual não usa `@WithMockUser` nem `SecurityMockMvcRequestPostProcessors`; a validação de segurança acontece principalmente com autenticação real via `POST /login` e JWT.
- **AssertJ**: disponível transitivamente por `spring-boot-starter-test`, mas não há uso explícito identificado em `src/test/java`.
- **Hamcrest**: aparece em parte das asserções HTTP, por exemplo `hasItem`, `hasItems`, `hasSize` e `startsWith`.

## Como executar

Pré-requisitos:

- Java 21
- Maven 3.9 ou superior

O próprio build valida esses requisitos pelo `maven-enforcer-plugin`.

Exemplos de execução:

```bash
# suíte completa
mvn test

# uma classe de service
mvn -Dtest=UsuarioServiceTest test

# um controller de integração
mvn -Dtest=EmpresaControllerIntegrationTest test

# contrato OpenAPI
mvn -Dtest=OpenApiConfigIntegrationTest test
```

Observações:

- Não é necessário informar profile manualmente para as classes atuais, porque os testes já usam `@ActiveProfiles`.
- A classe `OpenApiConfigIntegrationTest` executa com o profile `local`, porque `application-test.yaml` desabilita `springdoc.api-docs` e `swagger-ui`.

## Estrutura dos testes

```text
src/test/java/com/QueroTrabalhar
|-- config
|-- controllers
|-- domain
|-- repository
|-- services
|   `-- localidade
`-- QueroTrabalharApplicationTests.java
```

Resumo por área:

- `config`: segurança e documentação OpenAPI.
- `controllers`: testes HTTP de integração com `@SpringBootTest`, `@AutoConfigureMockMvc`, profile `test` e, na maior parte, `@Transactional`.
- `repository`: teste JPA focado no repositório de pendências de localidade com `@DataJpaTest`.
- `services`: testes unitários e de regras de negócio, majoritariamente com Mockito.
- `domain`: invariantes de entidade e montagem de DTOs.
- `QueroTrabalharApplicationTests`: smoke test de subida do contexto Spring.

## Quantidade atual

Os números abaixo refletem a última execução disponível em `target/surefire-reports`, que está alinhada com as 34 classes de teste presentes hoje em `src/test/java`.

| Recorte | Classes | Testes |
| --- | ---: | ---: |
| Configuração | 2 | 15 |
| Controllers | 7 | 77 |
| Services | 20 | 183 |
| Repository | 1 | 4 |
| Domínio e DTOs | 3 | 18 |
| Bootstrap | 1 | 1 |
| **Total** | **34** | **298** |

Status da última execução registrada pelo Surefire:

- `298` testes
- `0` falhas
- `0` erros
- `0` ignorados

## Tipos de teste

- **Unitários de service**: validam fluxo, mapeamento, filtros, permissões e exceções sem subir o contexto completo.
- **Integração de controller**: validam endpoints reais com `MockMvc`, serialização JSON, paginação, filtros, escopo de visibilidade e segurança.
- **Segurança**: validam login, acesso autenticado, autorização por papel e exposição de endpoints públicos e administrativos.
- **Domínio**: validam invariantes de entidade e comportamento de DTOs relacionados a localidade pendente e validada.
- **Repository**: validam consultas JPA específicas para `LocalidadePendente`.
- **Configuração**: validam OpenAPI e parte da configuração de segurança por profile.
- **Bootstrap**: valida que o contexto principal sobe com o profile de teste.

## Tabelas por classe e cenário

### Configuração e bootstrap

| Classe | Tipo | Testes | Cenários principais |
| --- | --- | ---: | --- |
| `OpenApiConfigIntegrationTest` | integração / configuração | 4 | `info` da API, schema de segurança Bearer, schemas de erro, descrições de endpoints, paginação e filtros documentados |
| `SecurityConfigIntegrationTest` | integração / segurança | 11 | login com e sem sucesso, acesso a `/me`, proteção de `/api/admin/**`, endpoint público sem token, bloqueio de Swagger e H2 no profile `test` |
| `QueroTrabalharApplicationTests` | bootstrap | 1 | subida do contexto Spring |

### Controllers

| Classe | Tipo | Testes | Cenários principais |
| --- | --- | ---: | --- |
| `EmpresaControllerIntegrationTest` | controller / integração | 18 | listagem pública, detalhe público, paginação, filtros por texto e localidade, escopo por empresa e ocultação de empresas e vagas com localidade pendente |
| `ExperienciaProfissionalAdminControllerIntegrationTest` | controller / integração | 12 | listagem administrativa, filtros por termo, tipo, datas e `emAndamento`, além de 401 e 403 |
| `OportunidadeDeEmpregoControllerIntegrationTest` | controller / integração | 6 | listagem pública, filtros combinados, ordenação padrão, ocultação de vagas com localidade pendente e detalhe público |
| `PerfilCandidatoControllerIntegrationTest` | controller / integração | 13 | minhas vagas de interesse, filtros por texto, tipo, empresa, recrutador, modalidade, localidade e `statusLocalidade`, além de `400` para valor inválido |
| `PerfilRecrutadorControllerIntegrationTest` | controller / integração | 11 | minhas oportunidades, filtros por texto, tipo, empresa, modalidade, localidade e `statusLocalidade`, além de proteção de escopo do recrutador |
| `TipoDeEmpregoAdminControllerIntegrationTest` | controller / integração | 7 | fila administrativa de tipos não aprovados, filtro textual e proteção por autenticação e autorização |
| `UsuarioAdminControllerIntegrationTest` | controller / integração | 10 | paginação administrativa de usuários e filtros por termo, nome, e-mail, CPF e existência de perfis |

### Services

| Classe | Tipo | Testes | Cenários principais |
| --- | --- | ---: | --- |
| `EmpresaFeatureServiceTest` | unitário / service | 15 | criação de empresa, validação de localidade por IDs e texto livre, pendência de localidade, consulta pública e listagem de oportunidades da empresa |
| `ExperienciaProfissionalServiceTest` | unitário / service | 12 | CRUD de experiência do candidato, consultas administrativas e bloqueio de acesso a recurso de outro usuário |
| `IndicacaoServiceTest` | unitário / service | 8 | criação, autoindicação inválida, listagem das indicações dadas e recebidas, exclusão e recurso inexistente |
| `InteresseEmEmpregoServiceTest` | unitário / service | 4 | listar, salvar, remover e contrato atual para exclusão de interesse inexistente |
| `LocalidadeServiceTest` | unitário / service | 7 | catálogos de países, estados e cidades, incluindo listas vazias e parâmetros inválidos |
| `OportunidadeEmpresaFeatureServiceTest` | unitário / service | 8 | publicação pessoal ou em nome da empresa, regras de vínculo e preservação do contexto da vaga no update |
| `OportunidadeLocalidadePendenteServiceTest` | unitário / service | 9 | criação e atualização de vagas com localidade por IDs ou texto livre, priorização de IDs e geração de pendência |
| `OportunidadeRecrutadorMeServiceTest` | unitário / service | 11 | paginação do recrutador, mistura de vagas pessoais e em empresa, detalhe, remoção, DTOs e busca em lote de pendências |
| `PerfilCandidatoInteresseServiceTest` | unitário / service | 8 | demonstrar interesse, impedir duplicidade, remover interesse, listar minhas vagas e buscar pendências em lote |
| `PerfilRecrutadorEmpresaFeatureServiceTest` | unitário / service | 7 | solicitar vínculo, impedir duplicidade, aprovar, recusar e bloquear transições inválidas |
| `PerfilRecrutadorMeServiceTest` | unitário / service | 5 | retorno do perfil autenticado, delegação para `UsuarioAutenticadoService` e consulta da empresa vinculada |
| `TipoDeEmpregoServiceTest` | unitário / service | 18 | criação administrativa, sugestão, aprovação, aprovação em lote, catálogo aprovado, fila pendente, delete e exceções de integridade |
| `UsuarioAtualizacaoSenhaServiceTest` | unitário / service | 12 | atualização do usuário comum e admin, conflitos de e-mail, troca de senha e busca por ID |
| `UsuarioAutenticadoServiceTest` | unitário / service | 8 | obtenção do usuário autenticado, perfil candidato, perfil recrutador e falhas de autenticação ou papel |
| `UsuarioPerfilServiceTest` | unitário / service | 4 | criação de perfil de recrutador no fluxo `/me` e no fluxo administrativo, incluindo bloqueio de duplicidade |
| `UsuarioServiceTest` | unitário / service | 19 | cadastro, listagem paginada, exclusão, inclusão e remoção de perfis candidato e recrutador |
| `LocalidadePendenteReprocessamentoSchedulerTest` | unitário / scheduler | 3 | scheduler habilitado, desabilitado e captura de exceção sem propagar |
| `LocalidadePendenteReprocessamentoServiceTest` | unitário / service | 6 | reprocessamento de pendência para empresa e vaga, manutenção de pendência ambígua e tratamento de tipo não suportado |
| `LocalidadeResolucaoServiceTest` | unitário / service | 15 | resolução por base interna, fallback para Google, tentativas extras, ambiguidade e entrada inválida |
| `ResultadoResolucaoLocalidadeTest` | unitário / domínio de apoio | 4 | criação de resultado resolvido ou pendente e bloqueio de estados inválidos |

### Repository, domínio e DTOs

| Classe | Tipo | Testes | Cenários principais |
| --- | --- | ---: | --- |
| `LocalidadePendenteRepositoryTest` | repositório / JPA | 4 | consulta da pendência mais recente, compatibilidade de método legado, busca em lote e ordenação das pendências abertas |
| `EmpresaResponseDTOTest` | domínio / DTO | 2 | priorização da localidade validada e fallback explícito para pendência |
| `OportunidadeDeEmpregoResponseDTOTest` | domínio / DTO | 2 | priorização da localidade validada e fallback explícito para pendência |
| `LocalidadePendenteTest` | domínio / entidade | 14 | criação, normalização, validações de texto, status, origem e dono genérico |

## Contrato de erros validado

O handler central de exceções da aplicação é `ResourceExceptionHandler`, que mapeia:

- `ObjectNotFoundException` para `404`
- `DuplicateResourceException` para `409`
- `BusinessRuleException` para `422`
- `DataIntegrityViolationException` para `400`
- `MethodArgumentNotValidException` para `400`
- `ConstraintViolationException` para `400`
- `IllegalArgumentException` para `400`
- `ResourceInUseException` para `409`

Modelo padrão de erro:

- `timestamp`
- `status`
- `error`
- `message`
- `path`

Modelo de validação:

- mesmos campos de `StandardError`
- lista `erros` com mensagens por campo

O que a suíte valida hoje com boa evidência:

| Recorte | Evidência atual |
| --- | --- |
| `401 Unauthorized` | validado por `SecurityConfigIntegrationTest` e por controllers administrativos, incluindo campos `status`, `error`, `message` e `path` |
| `403 Forbidden` | validado por `SecurityConfigIntegrationTest`, `ExperienciaProfissionalAdminControllerIntegrationTest` e `TipoDeEmpregoAdminControllerIntegrationTest` |
| `400 Bad Request` | validado por cenários de `statusLocalidade` inválido nos endpoints `/me` e por regras de entrada em services; a validação do corpo HTTP ainda é parcial |
| `404 Not Found` | validado principalmente para recursos públicos indisponíveis por localidade pendente e para recursos inexistentes no nível de service |
| `409` e `422` | fortemente validados no nível de service e documentados na OpenAPI; a cobertura HTTP completa desses códigos ainda pode crescer |
| schema OpenAPI de erros | validado por `OpenApiConfigIntegrationTest` para `StandardError`, `ValidationError` e `FieldMessage` |

Limite importante: a suíte atual valida muito bem o **status** e os **fluxos de negócio** das exceções, mas a verificação do corpo HTTP detalhado ainda está mais forte em segurança do que nos demais endpoints.

## Regras de negócio cobertas

As regras abaixo aparecem de forma recorrente na suíte atual:

- recursos públicos não devem expor empresas ou oportunidades com localidade pendente ou sem localidade validada;
- endpoints públicos não devem expor campos técnicos internos de pendência de localidade;
- filtros textuais devem ignorar espaços externos e filtros opcionais em branco;
- paginação e ordenação padrão devem permanecer estáveis nos endpoints principais;
- o escopo do recurso deve prevalecer sobre parâmetros legados do cliente, como `empresaId` na query;
- recrutadores só podem publicar em nome de empresa quando o vínculo estiver aprovado;
- candidato e recrutador só podem manipular recursos próprios nos endpoints `/me`;
- fluxo de indicação impede autoindicação e exclusão de indicação alheia;
- tipos de emprego possuem separação entre catálogo aprovado e fila administrativa de moderação;
- troca de senha exige senha atual correta e confirmação consistente;
- resolução de localidade tenta base interna antes do fallback externo e gera pendência quando o resultado é ambíguo ou insuficiente;
- reprocessamento de pendências mantém o lote estável quando o recurso continua ambíguo, falha ou não é suportado.

## Convenções adotadas

- nomes de teste em português, no padrão `deve...` e `naoDeve...`;
- foco em comportamento observável, não em detalhe de implementação;
- services usam majoritariamente `Arrange / Act / Assert`;
- controllers montam massa de dados mínima por helpers locais, em vez de fixtures globais;
- testes HTTP de listagem costumam validar paginação, ordenação, `totalElements` e estrutura de `content`;
- segurança de integração usa login real e header `Authorization: Bearer ...`, sem atalho com usuário mockado;
- o profile padrão das integrações é `test`, com H2 em memória e `ddl-auto: create-drop`;
- o teste de OpenAPI usa profile `local`, com `springdoc` habilitado;
- `PageImpl` é usado em parte dos testes unitários para simular paginação em memória;
- nos artefatos inspecionados deste repositório, não foi identificada recorrência de warning de Mockito/ByteBuddy no JDK 21 nem warning de serialização de `PageImpl` no último conjunto de relatórios do Surefire.

## Próximos pontos de melhoria

- ampliar testes HTTP de erro para `409`, `422`, `404` e `400` com validação completa do corpo retornado;
- adicionar cenários dedicados para `MethodArgumentNotValidException` e conteúdo da lista `erros` do `ValidationError`;
- expandir integração de controllers de escrita (`POST`, `PUT`, `PATCH`, `DELETE`), hoje menos cobertos do que os endpoints de consulta e listagem;
- adicionar relatório de cobertura automatizado, por exemplo com JaCoCo, para acompanhar evolução da suíte;
- criar testes mais explícitos para configurações transversais como CORS, serialização e eventuais regressões de profile;
- monitorar warnings de infraestrutura em CI para registrar no documento somente quando houver recorrência real.
