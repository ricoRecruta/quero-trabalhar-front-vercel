# Referência de endpoints — API Quero Trabalhar

Gerado a partir de [`QueroTrabalhar_API.postman_collection.json`](QueroTrabalhar_API.postman_collection.json).
Essa coleção é a fonte de verdade do contrato que `src/api/` implementa.

## Autenticação

`POST /login` recebe `{ "email", "password" }` e devolve o JWT **no cabeçalho
`Authorization` da resposta**, e não no corpo. As demais chamadas autenticadas
enviam `Authorization: Bearer <token>`.

> Detalhe fácil de errar — veja o tratamento em [`src/api/auth.js`](../../src/api/auth.js).

Nos caminhos abaixo, `{{variavel}}` é um parâmetro do ambiente Postman
([`QueroTrabalhar_Local.postman_environment.json`](QueroTrabalhar_Local.postman_environment.json)).

## 00 - Autenticação

| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/login` | Login - Admin |
| `POST` | `/login` | Login - Candidato Dan |
| `POST` | `/login` | Login - Recrutador RH |
| `POST` | `/login` | Login - Usuário Duplo |

## 01 - Localidades

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/localidades/paises?termo=Bra` | Listar países |
| `GET` | `/api/localidades/estados?paisId={{pais_id}}&termo=Para` | Listar estados por país |
| `GET` | `/api/localidades/cidades?estadoId={{estado_id}}&termo=Jo` | Listar cidades por estado |

## 02 - Tipos de Emprego

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/tipos-de-emprego/aprovados` | Listar tipos aprovados |
| `POST` | `/api/tipos-de-emprego/sugerir` | Sugerir tipo de emprego |
| `GET` | `/api/tipos-de-emprego/{{tipo_id}}` | Buscar tipo por ID |

## 03 - Usuários

| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/api/usuarios/cadastrar` | Cadastrar usuário |
| `GET` | `/api/usuarios/me` | Buscar usuário autenticado |
| `PUT` | `/api/usuarios/me` | Atualizar usuário autenticado |
| `DELETE` | `/api/usuarios/me` | Remover usuário autenticado |
| `POST` | `/api/usuarios/me/perfil-candidato` | Adicionar perfil candidato ao usuário autenticado |
| `DELETE` | `/api/usuarios/me/perfil-candidato` | Remover perfil candidato do usuário autenticado |
| `POST` | `/api/usuarios/me/perfil-recrutador` | Adicionar perfil recrutador ao usuário autenticado |
| `DELETE` | `/api/usuarios/me/perfil-recrutador` | Remover perfil recrutador do usuário autenticado |
| `PUT` | `/api/usuarios/me/senha` | Alterar senha do usuário autenticado |

## 04 - Empresas

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/empresas?page=0&size=10&sort=nome,asc&termo=&paisId={{pais_id}}&estadoId={{estado_id}}&cidadeId={{cidade_id}}` | Listar empresas |
| `POST` | `/api/empresas` | Criar empresa |
| `GET` | `/api/empresas/{{empresa_id}}` | Buscar empresa por ID |
| `GET` | `/api/empresas/{{empresa_id}}/oportunidades?page=0&size=10&sort=id,desc` | Listar oportunidades públicas da empresa |
| `GET` | `/api/empresas/{{empresa_id}}/recrutadores` | Listar recrutadores da empresa |

## 05 - Oportunidades

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/oportunidades?page=0&size=10&sort=id,desc&termo=Dev&tipoDeEmpregoId={{tipo_id}}&empresaId={{empresa_id}}&recrutadorId={{recrutador_id}}&paisId={{pais_id}}&estadoId={{estado_id}}&cidadeId={{cidade_id}}&modalidade=REMOTO` | Listar oportunidades públicas |
| `POST` | `/api/oportunidades` | Criar oportunidade |
| `GET` | `/api/oportunidades/{{vaga_id}}` | Buscar oportunidade por ID |
| `PUT` | `/api/oportunidades/{{vaga_id}}` | Atualizar oportunidade |
| `DELETE` | `/api/oportunidades/{{vaga_id}}` | Remover oportunidade |

## 06 - Recrutadores

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/recrutadores/me` | Buscar meu perfil de recrutador |
| `GET` | `/api/recrutadores/me/empresa` | Buscar empresa vinculada ao recrutador |
| `POST` | `/api/recrutadores/me/empresa/{{empresa_id}}/solicitar-vinculo` | Solicitar vínculo com empresa |
| `GET` | `/api/recrutadores/me/oportunidades?page=0&size=10&sort=id,desc&statusLocalidade=VALIDADA` | Listar minhas oportunidades |

## 07 - Candidatos e Interesses

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/candidatos/me/interesses/vagas?page=0&size=10&sort=id,desc&statusLocalidade=VALIDADA` | Listar vagas de interesse do candidato |
| `POST` | `/api/candidatos/me/interesses/vagas/{{vaga_id}}` | Demonstrar interesse em vaga |
| `DELETE` | `/api/candidatos/me/interesses/vagas/{{vaga_id}}` | Remover interesse em vaga |

## 08 - Experiências Profissionais

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/usuarios/me/perfil-candidato/experiencias` | Listar minhas experiências |
| `POST` | `/api/usuarios/me/perfil-candidato/experiencias` | Criar experiência |
| `PUT` | `/api/usuarios/me/perfil-candidato/experiencias/{{experiencia_id}}` | Atualizar experiência |
| `DELETE` | `/api/usuarios/me/perfil-candidato/experiencias/{{experiencia_id}}` | Remover experiência |

## 09 - Indicações

| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/api/indicacoes` | Criar indicação |
| `GET` | `/api/indicacoes/me/dadas` | Listar indicações dadas |
| `GET` | `/api/indicacoes/me/recebidas` | Listar indicações recebidas |
| `DELETE` | `/api/indicacoes/{{indicacao_id}}` | Remover indicação |

## 10 - Administração

### 10 - Administração › Recrutadores - empresa

| Método | Caminho | Descrição |
| --- | --- | --- |
| `PATCH` | `/api/admin/recrutadores/{{recrutador_id}}/empresa/aprovar` | Aprovar vínculo empresa do recrutador |
| `PATCH` | `/api/admin/recrutadores/{{recrutador_id}}/empresa/recusar` | Recusar vínculo empresa do recrutador |

### 10 - Administração › Usuários

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/admin/usuarios?page=0&size=10&sort=nome,asc&termo=&temPerfilCandidato=true&temPerfilRecrutador=true` | Listar usuários |
| `GET` | `/api/admin/usuarios/{{usuario_id}}` | Buscar usuário por ID |
| `PUT` | `/api/admin/usuarios/{{usuario_id}}` | Atualizar usuário por ID |
| `DELETE` | `/api/admin/usuarios/{{usuario_id}}` | Remover usuário por ID |
| `POST` | `/api/admin/usuarios/{{usuario_id}}/perfil-candidato` | Adicionar perfil candidato por ID |
| `DELETE` | `/api/admin/usuarios/{{usuario_id}}/perfil-candidato` | Remover perfil candidato por ID |
| `POST` | `/api/admin/usuarios/{{usuario_id}}/perfil-recrutador` | Adicionar perfil recrutador por ID |
| `DELETE` | `/api/admin/usuarios/{{usuario_id}}/perfil-recrutador` | Remover perfil recrutador por ID |

### 10 - Administração › Tipos de emprego

| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/api/admin/tipos-emprego` | Criar tipo de emprego admin |
| `PATCH` | `/api/admin/tipos-emprego/aprovar-lote` | Aprovar tipos em lote |
| `GET` | `/api/admin/tipos-emprego/nao-aprovados?page=0&size=10&sort=id,desc&termo=` | Listar tipos não aprovados |
| `GET` | `/api/admin/tipos-emprego/{{tipo_id}}` | Buscar tipo admin por ID |
| `PUT` | `/api/admin/tipos-emprego/{{tipo_id}}` | Atualizar tipo admin por ID |
| `DELETE` | `/api/admin/tipos-emprego/{{tipo_id}}` | Remover tipo admin por ID |
| `PATCH` | `/api/admin/tipos-emprego/{{tipo_id}}/aprovar` | Aprovar tipo por ID |

### 10 - Administração › Experiências

| Método | Caminho | Descrição |
| --- | --- | --- |
| `GET` | `/api/admin/experiencias?page=0&size=10&sort=id,desc` | Listar experiências admin |
| `GET` | `/api/admin/experiencias/{{experiencia_id}}` | Buscar experiência admin por ID |
| `PUT` | `/api/admin/experiencias/{{experiencia_id}}` | Atualizar experiência admin por ID |
| `DELETE` | `/api/admin/experiencias/{{experiencia_id}}` | Remover experiência admin por ID |

## Onde cada grupo é implementado no frontend

| Grupo da API | Módulo |
| --- | --- |
| 00 - Autenticação | [`src/api/auth.js`](../../src/api/auth.js) |
| 01 - Localidades | [`src/api/localidades.js`](../../src/api/localidades.js) |
| 02 - Tipos de Emprego | [`src/api/tiposDeEmprego.js`](../../src/api/tiposDeEmprego.js) |
| 03 - Usuários | [`src/api/usuarios.js`](../../src/api/usuarios.js) |
| 04 - Empresas | [`src/api/empresas.js`](../../src/api/empresas.js) |
| 05 - Oportunidades | [`src/api/oportunidades.js`](../../src/api/oportunidades.js) |
| 06 - Recrutadores | [`src/api/recrutadores.js`](../../src/api/recrutadores.js) |
| 07 - Candidatos e Interesses | [`src/api/candidatos.js`](../../src/api/candidatos.js) |
| 08 - Experiências Profissionais | [`src/api/experiencias.js`](../../src/api/experiencias.js) |
| 09 - Indicações | [`src/api/indicacoes.js`](../../src/api/indicacoes.js) |
| 10 - Administração | [`src/api/admin.js`](../../src/api/admin.js) |
