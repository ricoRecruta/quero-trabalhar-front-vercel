import { api, normalizarPagina } from "./client";

export function buscarMe() {
  return api.get("/api/recrutadores/me");
}

export function buscarMinhaEmpresa() {
  return api.get("/api/recrutadores/me/empresa");
}

/** Solicita vínculo com uma empresa (depende de aprovação do admin). */
export function solicitarVinculo(empresaId) {
  return api.post(`/api/recrutadores/me/empresa/${empresaId}/solicitar-vinculo`);
}

/** Filtros: { page, size, sort, statusLocalidade } */
export async function listarMinhasOportunidades(filtros = {}) {
  const { page = 0, size = 10, sort = "id,desc", ...resto } = filtros;
  return normalizarPagina(
    await api.get("/api/recrutadores/me/oportunidades", {
      query: { page, size, sort, ...resto },
    })
  );
}
