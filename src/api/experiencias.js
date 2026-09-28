import { api } from "./client";

const BASE = "/api/usuarios/me/perfil-candidato/experiencias";

export function listar() {
  return api.get(BASE);
}

export function buscarPorId(id) {
  return api.get(`${BASE}/${id}`);
}

/** { tipoDeEmpregoId, descricao, dataInicio, dataFim } — datas em YYYY-MM-DD */
export function criar(dados) {
  return api.post(BASE, dados);
}

// Não existe PUT/atualização para a própria experiência no
// ExperienciaProfissionalController atual (só listar, buscar por id, criar
// e deletar) — removida a função atualizar() que chamava um endpoint
// inexistente. Editar uma experiência hoje é: remover e criar de novo.
// A edição via PUT só existe no fluxo admin (ver src/api/admin.js).

export function remover(id) {
  return api.delete(`${BASE}/${id}`);
}
