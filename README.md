# Quero Trabalhar

Monorepo da plataforma Quero Trabalhar. O frontend React/Vite e a API Java/Spring Boot são versionados, desenvolvidos e publicados juntos.

## Estrutura

```text
apps/
├── web/  # React 19 + Vite
└── api/  # Java 21 + Spring Boot
docs/     # documentação e coleção Postman
```

O backend em `apps/api` vem da implementação completa da branch `jefferson-experimental` do repositório `a4s-ufpb/quero-trabalhar`, que corresponde ao contrato consumido pelo frontend.

## Desenvolvimento local

### Com Docker

O caminho mais simples sobe as duas aplicações e mantém frontend e API na mesma origem:

```bash
docker compose up --build
```

- Aplicação: <http://localhost:5173>
- API direta: <http://localhost:8080>
- Swagger local: <http://localhost:8080/swagger-ui/index.html>

O perfil local usa H2 em memória e cria dados de demonstração. As contas de demonstração usam a senha `123456`:

- `admin@gmail.com`
- `dan@gmail.com`
- `rh@tech.com`
- `jane@gmail.com`

### Sem Docker

Pré-requisitos: Node.js 20+, Java 21 e Maven 3.9+.

```bash
npm install
npm run dev:api
```

Em outro terminal:

```bash
npm run dev:web
```

O Vite encaminha `/login` e `/api/*` para `http://localhost:8080`.

## Comandos

| Comando | Função |
| --- | --- |
| `npm run dev` | Inicia o frontend |
| `npm run dev:api` | Inicia a API com Maven |
| `npm run build` | Gera o frontend de produção |
| `npm run build:api` | Empacota a API |
| `npm run lint` | Verifica o frontend |
| `npm run test:api` | Executa os testes da API |

## Publicação na Vercel

O arquivo `vercel.json` usa Vercel Services para publicar tudo em um único projeto e domínio:

- `/` é atendido pelo serviço Vite em `apps/web`;
- `/login` e `/api/*` são enviados ao serviço Spring Boot em `apps/api`;
- a API é construída pelo `apps/api/Dockerfile.vercel` e escuta a porta fornecida pela Vercel.

### 1. Banco de dados

Containers da Vercel não têm disco persistente. Conecte um banco MySQL ou PostgreSQL gerenciado e cadastre estas variáveis no projeto:

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `DB_URL` | sim | URL JDBC, começando com `jdbc:mysql://` ou `jdbc:postgresql://` |
| `DB_USERNAME` | sim | usuário do banco |
| `DB_PASSWORD` | sim | senha do banco |
| `JWT_SECRET` | sim | segredo Base64 forte para assinar tokens |
| `JWT_EXPIRATION` | não | validade do token em ms; padrão `86400000` |
| `GOOGLE_MAPS_API_KEY` | não | chave usada para resolver localidades |
| `JPA_DDL_AUTO` | não | padrão `update`; após criar o esquema, prefira `validate` |
| `LOCALIDADE_PENDENTE_REPROCESSAMENTO_ENABLED` | não | mantenha `false` em ambiente com autoscaling |

Exemplos de `DB_URL`:

```properties
DB_URL=jdbc:postgresql://host:5432/quero_trabalhar?sslmode=require
DB_URL=jdbc:mysql://host:3306/quero_trabalhar?useSSL=true
```

Não use H2 em produção: os serviços podem escalar para zero ou criar mais de uma instância.

### 2. Configurar o projeto

1. Importe este repositório na Vercel.
2. Em **Build and Deployment**, selecione o framework **Services**.
3. Cadastre as variáveis de produção e preview.
4. Faça o deploy a partir da raiz do repositório.

Também é possível testar a composição local da Vercel com uma versão recente da CLI:

```bash
vercel dev -L
```

O frontend usa caminhos relativos para a API, por isso previews e domínios personalizados funcionam sem trocar `VITE_API_BASE_URL` e sem CORS entre os dois serviços.

## Observações de produção

- O scheduler de reprocessamento de localidades fica desligado por padrão na Vercel, evitando execução duplicada quando houver várias instâncias.
- Swagger e H2 Console ficam desabilitados no perfil `prod`.
- `JPA_DDL_AUTO=update` facilita o primeiro deploy do MVP; migrações versionadas devem substituir essa opção antes de evoluções destrutivas do banco.

## Licença

MIT.
