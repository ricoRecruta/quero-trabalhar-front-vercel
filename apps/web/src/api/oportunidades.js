import { api, normalizarPagina } from "./client";

/**
 * Lista pública de vagas.
 * Filtros: { page, size, sort, termo, tipoDeEmpregoId, empresaId,
 *            recrutadorId, paisId, estadoId, cidadeId, modalidade }
 * modalidade: "REMOTO" | "PRESENCIAL" | "HIBRIDO"
 */
export async function listar(filtros = {}) {
  const { page = 0, size = 10, sort = "id,desc", signal, ...resto } = filtros;
  return normalizarPagina(
    await api.get("/api/oportunidades", {
      query: { page, size, sort, ...resto },
      signal,
    })
  );
}

/**
 * Cria vaga (exige perfil de recrutador).
 * { descricao, tipoDeEmpregoId, modalidade, paisId, estadoId,
 *   cidadeId, localidadeTexto, empresaId, publicarComoEmpresa }
 */
export function criar(dados) {
  return api.post("/api/oportunidades", dados);
}

export function buscarPorId(id) {
  return api.get(`/api/oportunidades/${id}`);
}

export function atualizar(id, dados) {
  return api.put(`/api/oportunidades/${id}`, dados);
}

export function remover(id) {
  return api.delete(`/api/oportunidades/${id}`);
}
