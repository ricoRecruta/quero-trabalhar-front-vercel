# ---------------------------------------------------------------------------
# Etapa 1: build do bundle com Vite
# ---------------------------------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /app

# O contexto deste Dockerfile é a raiz do monorepo.
COPY package.json package-lock.json ./
COPY apps/web/package.json ./apps/web/package.json
RUN npm ci

COPY apps/web ./apps/web

# Vazio de propósito: em produção quem roteia /api é o nginx (mesma origem).
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build --workspace @quero-trabalhar/web

# ---------------------------------------------------------------------------
# Etapa 2: servir os estáticos + proxy reverso para a API
# ---------------------------------------------------------------------------
FROM nginx:1.27-alpine

# Endereço do backend. Dentro do compose usamos o nome do serviço;
# sobrescreva com -e API_URL=... para apontar para outro lugar.
ENV API_URL=http://quero-trabalhar-api:8080

# A imagem oficial aplica envsubst em /etc/nginx/templates/*.template.
# O filtro impede que $uri, $host etc. sejam substituídos por vazio.
ENV NGINX_ENVSUBST_FILTER=API_

COPY apps/web/docker/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/apps/web/dist /usr/share/nginx/html

EXPOSE 80

# 127.0.0.1 explícito, não "localhost": no musl (Alpine), o wget resolve
# "localhost" preferindo ::1 (IPv6), e o nginx aqui só escuta em IPv4 — daria
# "connection refused" no healthcheck mesmo com tudo funcionando de verdade
# (o mesmo bug já encontrado e corrigido em docker-allinone/Dockerfile).
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]
