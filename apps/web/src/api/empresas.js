import { api, normalizarPagina } from "./client";

/** Lista paginada: { page, size, sort, termo, paisId, estadoId, cidadeId } */
export async function listar(filtros = {}) {
  const { page = 0, size = 10, sort = "nome,asc", ...resto } = filtros;
  return normalizarPagina(
    await api.get("/api/empresas", { query: { page, size, sort, ...resto } })
  );
}

/**
 * Cria empresa.
 * { nome, descricao, site, emailPublico, telefonePublico,
 *   paisId, estadoId, cidadeId, localidadeTexto }
 */
export function criar(dados) {
  return api.post("/api/empresas", dados);
}

export function buscarPorId(id) {
  return api.get(`/api/empresas/${id}`);
}

export async function listarOportunidades(empresaId, filtros = {}) {
  const { page = 0, size = 10, sort = "id,desc" } = filtros;
  return normalizarPagina(
    await api.get(`/api/empresas/${empresaId}/oportunidades`, {
      query: { page, size, sort },
    })
  );
}

export function listarRecrutadores(empresaId) {
  return api.get(`/api/empresas/${empresaId}/recrutadores`);
}
