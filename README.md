# Quero Trabalhar — Frontend

Interface web em React + Vite do Quero Trabalhar, sistema que conecta pessoas
em busca de emprego a recrutadores.

Este repositório contém **apenas o frontend**. A API fica em
[a4s-ufpb/quero-trabalhar](https://github.com/a4s-ufpb/quero-trabalhar).

## Sumário

- [Como rodar](#como-rodar)
- [Rodando com Docker](#rodando-com-docker)
- [Como o front conversa com a API](#como-o-front-conversa-com-a-api)
- [Camada de API](#camada-de-api)
- [Estrutura](#estrutura)
- [Documentação](#documentação)

## Como rodar

Pré-requisitos: Node 20+ e a API rodando em `http://localhost:8080`.

```bash
npm install
cp .env.example .env
npm run dev
```

A aplicação sobe em <http://localhost:5173>.

Scripts disponíveis:

| Comando | O que faz |
| --- | --- |
| `npm run dev` | Servidor de desenvolvimento com hot reload |
| `npm run build` | Gera o bundle de produção em `dist/` |
| `npm run preview` | Serve o bundle já buildado |
| `npm run lint` | Roda o ESLint |

## Rodando com Docker

Cada aplicação tem seu próprio container. Eles se enxergam por uma rede Docker
compartilhada, criada uma única vez:

```bash
docker network create quero-trabalhar-net
```

Depois, em cada repositório:

```bash
docker compose up --build
```

O frontend fica em <http://localhost:5173> e a API em <http://localhost:8080>.

### Subindo os dois de uma vez

Se os repositórios estiverem lado a lado no disco, dá para subir tudo a partir
daqui, sem criar a rede manualmente:

```bash
docker compose -f docker-compose.full.yml up --build
```

Se o backend estiver em outro caminho, aponte no `.env`:

```
BACKEND_PATH=../caminho/para/quero-trabalhar
```

## Como o front conversa com a API

O frontend **não chama o backend diretamente pelo navegador**. Em ambos os
ambientes existe um proxy que coloca os dois na mesma origem:

```
 Desenvolvimento                          Container
 ───────────────                          ─────────
 navegador                                navegador
    │ /api/oportunidades                     │ /api/oportunidades
    ▼                                        ▼
 vite dev server  :5173                   nginx  :80  (publicado em :5173)
    │ proxy                                  │ proxy_pass
    ▼                                        ▼
 API Spring Boot  :8080                   quero-trabalhar-api:8080
```

Isso resolve dois problemas de uma vez:

1. **Sem CORS.** Como as requisições saem da mesma origem do front, o navegador
   não dispara preflight nem exige `Access-Control-Allow-Origin`.
2. **O token chega inteiro.** A API devolve o JWT no cabeçalho `Authorization`
   da resposta do login. Numa chamada cross-origin, esse cabeçalho só ficaria
   visível se a API mandasse `Access-Control-Expose-Headers`. No mesmo domínio,
   ele chega sem configuração extra.

Quem define o destino do proxy:

| Ambiente | Arquivo | Variável |
| --- | --- | --- |
| Desenvolvimento | [`vite.config.js`](vite.config.js) | `VITE_API_PROXY_TARGET` |
| Container | [`docker/nginx.conf.template`](docker/nginx.conf.template) | `API_URL` |

Para chamar a API diretamente, sem proxy, defina `VITE_API_BASE_URL=http://localhost:8080`
— nesse caso a API precisa liberar a origem em `CORS_ALLOWED_ORIGINS`.

## Camada de API

Todo acesso à API passa por [`src/api/`](src/api/). Os módulos espelham os
grupos da coleção Postman; nenhum componente monta URL na mão.

```jsx
import { auth, oportunidades, candidatos } from "../api";

await auth.login({ email, password });               // guarda o token
const { itens, totalPaginas } = await oportunidades.listar({ termo: "java" });
await candidatos.demonstrarInteresse(vagaId);
```

O que [`client.js`](src/api/client.js) cuida sozinho:

- injeta `Authorization: Bearer <token>` nas chamadas autenticadas;
- guarda e lê o token do `localStorage`;
- converte erro HTTP em `ApiError` com `status`, `corpo` e a mensagem do backend;
- descarta parâmetros de query vazios;
- normaliza a paginação do Spring (`Page<T>`) para `{ itens, pagina, totalPaginas, ... }`.

Tratando erros:

```jsx
import { ApiError } from "../api";

try {
  await candidatos.demonstrarInteresse(vagaId);
} catch (err) {
  if (err instanceof ApiError && err.naoAutorizado) {
    // 401 ou 403 — mandar para o login
  }
}
```

A lista completa de endpoints está em [`docs/api/ENDPOINTS.md`](docs/api/ENDPOINTS.md).

## Estrutura

```
src/
├── api/                  camada de acesso à API
│   ├── client.js         fetch, token, erros, paginação
│   ├── mapeadores.js     contrato da API -> formato da interface
│   ├── index.js          ponto único de importação
│   └── *.js              um módulo por grupo de endpoints
├── components/
│   ├── cadastro/         criação de conta
│   ├── criarVaga/        publicação de vaga (recrutador)
│   ├── layout/           casca da aplicação
│   ├── login/            autenticação
│   ├── perfil/           dados do usuário
│   └── vagas/            listagem e candidatura
└── main.jsx
```

## Documentação

| Arquivo | Conteúdo |
| --- | --- |
| [`docs/api/ENDPOINTS.md`](docs/api/ENDPOINTS.md) | Todos os endpoints, gerado da coleção |
| [`docs/api/QueroTrabalhar_API.postman_collection.json`](docs/api/) | Coleção Postman |
| [`docs/api/QueroTrabalhar_Local.postman_environment.json`](docs/api/) | Ambiente local do Postman |
| [`docs/GUIA-Documentacao-final-QueroTrabalharAPI.pdf`](docs/) | Guia da documentação |
| [`docs/Documentacao-final-QueroTrabalharAPI.pdf`](docs/) | Documentação final da API |
| `docs/Visão geral da API Quero Trabalhar Backend.docx` | Visão geral do backend |

Com a API no ar, o Swagger fica em <http://localhost:8080/swagger-ui.html>.

## Licença

[MIT](LICENSE).
