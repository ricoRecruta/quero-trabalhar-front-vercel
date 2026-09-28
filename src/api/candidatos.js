import { api, normalizarPagina } from "./client";

/** Vagas em que o candidato demonstrou interesse. */
export async function listarInteresses(filtros = {}) {
  const { page = 0, size = 10, sort = "id,desc", ...resto } = filtros;
  return normalizarPagina(
    await api.get("/api/candidatos/me/interesses/vagas", {
      query: { page, size, sort, ...resto },
    })
  );
}

export function demonstrarInteresse(vagaId) {
  return api.post(`/api/candidatos/me/interesses/vagas/${vagaId}`);
}

export function removerInteresse(vagaId) {
  return api.delete(`/api/candidatos/me/interesses/vagas/${vagaId}`);
}
