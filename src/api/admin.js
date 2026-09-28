import { api, normalizarPagina } from "./client";

/* Todos os endpoints abaixo exigem um usuário com papel ADMIN. */

/* --------------------------- vínculo recrutador ------------------------- */

export function aprovarVinculoRecrutador(recrutadorId) {
  return api.patch(`/api/admin/recrutadores/${recrutadorId}/empresa/aprovar`);
}

export function recusarVinculoRecrutador(recrutadorId) {
  return api.patch(`/api/admin/recrutadores/${recrutadorId}/empresa/recusar`);
}

/* ------------------------------- usuários ------------------------------- */

/** Filtros: { page, size, sort, termo, temPerfilCandidato, temPerfilRecrutador } */
export async function listarUsuarios(filtros = {}) {
  const { page = 0, size = 10, sort = "nome,asc", ...resto } = filtros;
  return normalizarPagina(
    await api.get("/api/admin/usuarios", { query: { page, size, sort, ...resto } })
  );
}

export function buscarUsuario(id) {
  return api.get(`/api/admin/usuarios/${id}`);
}

export function atualizarUsuario(id, dados) {
  return api.put(`/api/admin/usuarios/${id}`, dados);
}

export function removerUsuario(id) {
  return api.delete(`/api/admin/usuarios/${id}`);
}

// PUT, não POST — mesma correção de src/api/usuarios.js, confirmada lendo
// UsuarioAdminController na jefferson-experimental.
export function adicionarPerfilCandidato(usuarioId) {
  return api.put(`/api/admin/usuarios/${usuarioId}/perfil-candidato`);
}

export function removerPerfilCandidato(usuarioId) {
  return api.delete(`/api/admin/usuarios/${usuarioId}/perfil-candidato`);
}

/** { nomeDaEmpresa } — PUT, não POST. */
export function adicionarPerfilRecrutador(usuarioId, dados) {
  return api.put(`/api/admin/usuarios/${usuarioId}/perfil-recrutador`, dados);
}

export function removerPerfilRecrutador(usuarioId) {
  return api.delete(`/api/admin/usuarios/${usuarioId}/perfil-recrutador`);
}

/* --------------------------- tipos de emprego --------------------------- */

/** { titulo, descricao } */
export function criarTipoDeEmprego(dados) {
  return api.post("/api/admin/tipos-emprego", dados);
}

/** Recebe um array de ids: [1, 2, 3] */
export function aprovarTiposEmLote(ids) {
  return api.patch("/api/admin/tipos-emprego/aprovar-lote", ids);
}

export async function listarTiposNaoAprovados(filtros = {}) {
  const { page = 0, size = 10, sort = "id,desc", ...resto } = filtros;
  return normalizarPagina(
    await api.get("/api/admin/tipos-emprego/nao-aprovados", {
      query: { page, size, sort, ...resto },
    })
  );
}

// Não existe GET nem PUT por ID em /api/admin/tipos-emprego/{id} no
// TipoDeEmpregoAdminController atual — só nao-aprovados, criar, aprovar
// (individual e em lote) e deletar. Removidas as funções buscarTipoDeEmprego
// e atualizarTipoDeEmprego que chamavam endpoints inexistentes.

export function removerTipoDeEmprego(id) {
  return api.delete(`/api/admin/tipos-emprego/${id}`);
}

// Exige corpo { titulo, descricao } — confirmado lendo o controller: o
// admin pode ajustar o título/descrição da sugestão no momento de aprovar,
// não é um PATCH "vazio". Sem o corpo, a API responde 401 through /error
// (um HttpMessageNotReadableException não tratado é re-despachado pro
// endpoint de erro, que por sua vez exige autenticação e falha de um jeito
// que parece, mas não é, um problema de token).
export function aprovarTipoDeEmprego(id, dados) {
  return api.patch(`/api/admin/tipos-emprego/${id}/aprovar`, dados);
}

/* ------------------------------ experiências ---------------------------- */

export async function listarExperiencias(filtros = {}) {
  const { page = 0, size = 10, sort = "id,desc" } = filtros;
  return normalizarPagina(
    await api.get("/api/admin/experiencias", { query: { page, size, sort } })
  );
}

export function buscarExperiencia(id) {
  return api.get(`/api/admin/experiencias/${id}`);
}

export function atualizarExperiencia(id, dados) {
  return api.put(`/api/admin/experiencias/${id}`, dados);
}

export function removerExperiencia(id) {
  return api.delete(`/api/admin/experiencias/${id}`);
}
