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

O `vercel.json` usa Vercel Services para publicar os dois aplicativos no mesmo domínio:

- o serviço `web` compila o Vite em `apps/web` e atende `/`;
- o serviço `api` executa o container Spring Boot de `apps/api`;
- `/api/*` e `/login` são encaminhados publicamente para `api`;
- todos os outros caminhos são encaminhados para `web`;
- não há bindings internos, pois o navegador chama a API pelas rotas públicas relativas.

### 1. Banco de dados

Containers da Vercel não têm disco persistente. A configuração recomendada é criar um Neon PostgreSQL em **Storage** no painel da Vercel e conectá-lo ao projeto. A integração injeta automaticamente `PGHOST`, `PGDATABASE`, `PGUSER` e `PGPASSWORD`, que o backend consome diretamente.

Para outro provedor MySQL ou PostgreSQL, cadastre as variáveis `DB_*` abaixo. Elas têm precedência sobre as variáveis do Neon:

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

As três variáveis `DB_*` são opcionais quando o Neon está conectado. Exemplos de `DB_URL` para outro provedor:

```properties
DB_URL=jdbc:postgresql://host:5432/quero_trabalhar?sslmode=require
DB_URL=jdbc:mysql://host:3306/quero_trabalhar?useSSL=true
```

Não use H2 em produção: os serviços podem escalar para zero ou criar mais de uma instância.

### 2. Configurar o projeto

1. Importe este repositório na Vercel.
2. Selecione o modo **Services** quando a Vercel solicitar a importação multi-serviço.
3. Conecte um Neon PostgreSQL em **Storage** ou cadastre `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para outro banco.
4. Cadastre `JWT_SECRET` em Production e Preview.
5. Faça o deploy a partir da raiz; cada serviço será construído independentemente.

O frontend usa caminhos relativos para a API, por isso previews e domínios personalizados funcionam sem trocar `VITE_API_BASE_URL` e sem CORS.

## Observações de produção

- O scheduler de reprocessamento de localidades fica desligado por padrão na Vercel, evitando execução duplicada quando houver várias instâncias.
- Swagger e H2 Console ficam desabilitados no perfil `prod`.
- `JPA_DDL_AUTO=update` facilita o primeiro deploy do MVP; migrações versionadas devem substituir essa opção antes de evoluções destrutivas do banco.

## Licença

MIT.
