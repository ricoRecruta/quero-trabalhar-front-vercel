# =============================================================================
# Imagem standalone do backend, para rodar em CONTAINER SEPARADO do front
# (ao contrário de ../docker-allinone, que embute os dois no mesmo container).
#
# Por que fica aqui, no repositório do FRONT, e não no repositório do
# backend: este front (src/api/) foi escrito contra o contrato documentado
# na coleção Postman, que corresponde à branch `jefferson-experimental` do
# backend — não à branch que está de fato no disco do repositório do
# backend hoje. Para os dois containers conversarem de verdade (login,
# vagas, tudo respondendo certo), o backend aqui é clonado dessa branch
# direto do GitHub durante o build, sem tocar em nenhuma branch local.
#
# Build (a partir da raiz deste repositório):
#   docker build -t quero-trabalhar-api-jefferson:local -f docker-backend-jefferson/Dockerfile docker-backend-jefferson
# =============================================================================

ARG BACKEND_REPO=https://github.com/a4s-ufpb/quero-trabalhar.git
ARG BACKEND_BRANCH=jefferson-experimental

FROM alpine/git:2.45.2 AS backend-source
ARG BACKEND_REPO
ARG BACKEND_BRANCH

WORKDIR /src
RUN git clone --branch "$BACKEND_BRANCH" --depth 1 "$BACKEND_REPO" .

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY --from=backend-source /src/pom.xml .
RUN mvn -B -q dependency:go-offline

COPY --from=backend-source /src/src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Perfil "local" da jefferson-experimental: H2 em memória, já populado com
# usuários de teste (admin@gmail.com, dan@gmail.com, rh@tech.com,
# jane@gmail.com — senha 123456 para todos).
ENV SPRING_PROFILES_ACTIVE=local

# Origem do front liberada em CORS — só importa se algo chamar a API direto
# do navegador sem passar pelo proxy do nginx do front.
ENV CORS_ALLOWED_ORIGINS=http://localhost:5173

# Sem spring-boot-starter-actuator no pom desta branch, então /actuator/health
# não existe — usamos o swagger-ui.html (sempre público no perfil local)
# como sinal de que a aplicação subiu.
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1:8080/swagger-ui.html || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
