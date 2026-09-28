# Container único — front + backend juntos, para testar a aplicação inteira

Esta imagem existe só para testar o Quero Trabalhar de ponta a ponta rápido,
num único `docker run`, sem precisar clonar o repositório do backend nem
subir dois containers. **Não é como a aplicação roda em produção** — lá cada
parte continua no seu próprio container (ver `../Dockerfile` e
`../docker-compose.yml` no repositório do front, e o `Dockerfile`/
`docker-compose.yml` no repositório do backend).

## Por que o backend vem de outra branch

O código deste front (`src/api/`) foi escrito contra o contrato documentado
na coleção Postman (`docs/api/`), que corresponde à branch
`jefferson-experimental` do backend — não à branch principal, cujos
endpoints são outros (`/auth/login` em vez de `/login`, por exemplo). Para o
teste fazer sentido — login, listagem de vagas, candidatura, tudo respondendo
de verdade — o `Dockerfile` clona essa branch direto do GitHub durante o
build (`git clone --branch jefferson-experimental`). Isso não toca em nenhuma
branch local do repositório do backend; é só a fonte usada dentro do build
desta imagem de teste.

## Build

Rode a partir da raiz do repositório do **front** (o contexto precisa
enxergar `src/`, `package.json` etc.):

```bash
docker build -t quero-trabalhar-tudo:local -f docker-allinone/Dockerfile .
```

O build faz três coisas em paralelo lógico: clona e compila o backend
(Maven), compila o front (Vite), e monta a imagem final com JRE + nginx.
Leva alguns minutos na primeira vez.

## Rodar

```bash
docker run --rm -p 8888:80 --name quero-trabalhar-tudo quero-trabalhar-tudo:local
```

Abra <http://localhost:8888>. O Swagger fica em
<http://localhost:8888/swagger-ui.html>, e o console do H2 em
<http://localhost:8888/h2-console> (JDBC URL
`jdbc:h2:mem:querotrabalhar-local`, usuário `sa`, senha em branco).

Pare com `Ctrl+C` (o `--rm` já remove o container ao sair).

## Contas de teste já cadastradas

O perfil `local` popula o banco H2 automaticamente na subida (ver
`SemeadorBd` no backend). Todas usam a senha **`123456`**:

| E-mail | Perfil |
| --- | --- |
| `admin@gmail.com` | Admin + recrutador |
| `dan@gmail.com` | Candidato |
| `rh@tech.com` | Recrutador |
| `jane@gmail.com` | Candidato + recrutador (perfil duplo) |

## Como front e backend se falam aqui dentro

Um processo Java (o backend, porta 8080) e um processo nginx (porta 80,
publicada como `8888` no seu host) rodam lado a lado no mesmo container,
iniciados por `entrypoint.sh`. O nginx serve os arquivos estáticos do React e
faz proxy de `/api`, `/login`, `/swagger-ui` etc. para `127.0.0.1:8080` — a
mesma ideia do proxy reverso usado no container de produção do front, só que
aqui "backend" é `localhost` em vez de outro container na rede Docker.

Se qualquer um dos dois processos cair, o `entrypoint.sh` derruba o outro e
encerra o container — evita mascarar uma falha atrás do processo que ainda
está de pé.

## Limitações (é imagem de teste, não de produção)

- **Dados não persistem.** H2 em memória: cada `docker run` novo começa do
  zero, populado só com o seed padrão.
- **Chave do Google Maps é fake** (`dev-google-maps-key`, valor padrão da
  branch). Funcionalidades de resolução de localidade por texto livre que
  dependem da API do Google Maps não vão funcionar de verdade — localidades
  por ID (país/estado/cidade) continuam OK.
- **Sem HTTPS.** É HTTP puro, para teste local.
- **A branch do backend pode mudar.** Como o Dockerfile clona
  `jefferson-experimental` na hora do build, um rebuild em outro dia pode
  trazer código diferente do que você testou hoje. Se precisar reproduzir
  exatamente esta versão, o commit usado na criação desta imagem foi
  `da26075977428109b0a0f02e3c452b40a40f2d9c` — passe
  `--build-arg BACKEND_BRANCH=da26075977428109b0a0f02e3c452b40a40f2d9c` para
  fixar nele.
