# =============================================================================
# Container único para TESTAR a aplicação Quero Trabalhar inteira: front +
# backend no mesmo container, se comunicando por localhost.
#
# Isto é uma imagem de teste/demo, não a forma de rodar em produção.
# Em produção, cada aplicação continua em seu próprio container — ver
# ../docker-compose.yml (frontend) e o docker-compose.yml do repositório
# quero-trabalhar (backend).
#
# Por que o backend vem de outra branch:
# O código deste front (src/api/) foi escrito contra o contrato documentado
# na coleção Postman, que corresponde à branch `jefferson-experimental` do
# backend — não à branch principal, cujos endpoints são outros. Para o teste
# fazer sentido (login, vagas, empresas etc. respondendo de verdade), o
# backend empacotado aqui é clonado dessa branch em tempo de build, direto do
# GitHub. Nada disso toca nas branches locais do repositório do backend.
#
# Build:
#   docker build -t quero-trabalhar-tudo:local -f docker-allinone/Dockerfile ..
#     (o contexto é a raiz do repositório do FRONT — ver instrução abaixo)
#
# Run:
#   docker run --rm -p 8888:80 quero-trabalhar-tudo:local
#   Depois abra http://localhost:8888
# =============================================================================

ARG BACKEND_REPO=https://github.com/a4s-ufpb/quero-trabalhar.git
ARG BACKEND_BRANCH=jefferson-experimental

# -----------------------------------------------------------------------------
# Etapa 1: baixa o código do backend na branch documentada
# -----------------------------------------------------------------------------
FROM alpine/git:2.45.2 AS backend-source
ARG BACKEND_REPO
ARG BACKEND_BRANCH

WORKDIR /src
RUN git clone --branch "$BACKEND_BRANCH" --depth 1 "$BACKEND_REPO" .

# -----------------------------------------------------------------------------
# Etapa 2: build do backend (Spring Boot / Maven)
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /app
COPY --from=backend-source /src/pom.xml .
RUN mvn -B -q dependency:go-offline

COPY --from=backend-source /src/src ./src
RUN mvn -B -q clean package -DskipTests

# -----------------------------------------------------------------------------
# Etapa 3: build do frontend (Vite)
# Contexto do build = raiz do repositório quero-trabalhar-front (ver comando
# de build no topo do arquivo: "-f docker-allinone/Dockerfile ..").
# -----------------------------------------------------------------------------
FROM node:22-alpine AS frontend-build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

COPY index.html vite.config.js eslint.config.js ./
COPY public ./public
COPY src ./src

# Vazio de propósito: dentro deste container o nginx faz o proxy para
# localhost:8080, então o front chama /api relativo, sem base URL fixa.
ENV VITE_API_BASE_URL=""
RUN npm run build

# -----------------------------------------------------------------------------
# Etapa final: nginx (serve o front + faz proxy) e o JRE (roda o backend),
# no mesmo container, um processo cada, iniciados por um script simples.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache nginx tini

WORKDIR /app

# --- backend ---
COPY --from=backend-build /app/target/*.jar backend.jar

# --- frontend + nginx ---
COPY --from=frontend-build /app/dist /usr/share/nginx/html
COPY docker-allinone/nginx.conf /etc/nginx/http.d/default.conf
RUN mkdir -p /run/nginx

COPY docker-allinone/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Perfil "local" da jefferson-experimental: H2 em memória, já populado com
# usuários de teste (ver README.md deste diretório) — sem MySQL, sem
# variáveis obrigatórias.
ENV SPRING_PROFILES_ACTIVE=local
ENV SERVER_PORT=8080

EXPOSE 80

# 127.0.0.1 explícito, não "localhost": no musl (Alpine), o wget resolve
# "localhost" preferindo ::1 (IPv6), e o nginx aqui só escuta em IPv4 — daria
# "connection refused" no healthcheck mesmo com tudo funcionando de verdade
# (confirmado testando: falha em localhost, funciona em 127.0.0.1).
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1/healthz || exit 1

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["/entrypoint.sh"]
