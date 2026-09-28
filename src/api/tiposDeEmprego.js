import { api } from "./client";

export function listarAprovados() {
  return api.get("/api/tipos-de-emprego/aprovados");
}

/** Sugere um novo tipo para aprovação do admin: { titulo, descricao } */
export function sugerir(dados) {
  return api.post("/api/tipos-de-emprego/sugerir", dados);
}

export function buscarPorId(id) {
  return api.get(`/api/tipos-de-emprego/${id}`);
}
