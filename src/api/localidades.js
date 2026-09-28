import { api } from "./client";

export function listarPaises(termo) {
  return api.get("/api/localidades/paises", { query: { termo } });
}

export function listarEstados(paisId, termo) {
  return api.get("/api/localidades/estados", { query: { paisId, termo } });
}

export function listarCidades(estadoId, termo) {
  return api.get("/api/localidades/cidades", { query: { estadoId, termo } });
}
