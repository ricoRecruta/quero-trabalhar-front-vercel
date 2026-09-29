import { api } from "./client";

/** { usuarioIndicadoId, mensagem } */
export function criar(dados) {
  return api.post("/api/indicacoes", dados);
}

export function listarDadas() {
  return api.get("/api/indicacoes/me/dadas");
}

export function listarRecebidas() {
  return api.get("/api/indicacoes/me/recebidas");
}

export function remover(id) {
  return api.delete(`/api/indicacoes/${id}`);
}
