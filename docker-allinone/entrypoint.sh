#!/bin/sh
# Sobe backend (Java) e frontend (nginx) no mesmo container.
#
# Não existe orquestrador aqui — é um script simples de propósito, porque
# esta imagem é para testar a aplicação inteira rapidamente, não para rodar
# em produção (lá cada aplicação continua em seu próprio container).
#
# Se qualquer um dos dois processos cair, o script mata o outro e encerra o
# container com o código de saída de quem morreu primeiro — assim um
# `docker ps` mostra "Exited" em vez de mascarar a falha atrás do processo
# que sobrou de pé.
set -eu

echo "[entrypoint] iniciando backend (Spring Boot, perfil ${SPRING_PROFILES_ACTIVE:-local})..."
java -jar /app/backend.jar &
BACKEND_PID=$!

echo "[entrypoint] iniciando nginx..."
nginx -g 'daemon off;' &
NGINX_PID=$!

shutdown() {
  echo "[entrypoint] encerrando..."
  kill "$BACKEND_PID" "$NGINX_PID" 2>/dev/null || true
  wait "$BACKEND_PID" 2>/dev/null || true
  wait "$NGINX_PID" 2>/dev/null || true
  exit 0
}
trap shutdown TERM INT

while true; do
  if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo "[entrypoint] backend (java) parou inesperadamente — encerrando o container"
    wait "$BACKEND_PID"
    EXIT_CODE=$?
    kill "$NGINX_PID" 2>/dev/null || true
    exit "$EXIT_CODE"
  fi

  if ! kill -0 "$NGINX_PID" 2>/dev/null; then
    echo "[entrypoint] nginx parou inesperadamente — encerrando o container"
    wait "$NGINX_PID"
    EXIT_CODE=$?
    kill "$BACKEND_PID" 2>/dev/null || true
    exit "$EXIT_CODE"
  fi

  sleep 2
done
