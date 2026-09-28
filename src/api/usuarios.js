import { api } from "./client";

/** Cadastro público: { cpf, nome, telefone, email, senha } */
export function cadastrar(dados) {
  return api.post("/api/usuarios/cadastrar", dados, { auth: false });
}

export function buscarMe() {
  return api.get("/api/usuarios/me");
}

/** Atualização do próprio usuário: { nome, telefone, email } */
export function atualizarMe(dados) {
  return api.put("/api/usuarios/me", dados);
}

export function removerMe() {
  return api.delete("/api/usuarios/me");
}

// PUT, não POST: o controller atual (UsuarioController.adicionarMeuPerfilCandidato)
// usa @PutMapping — confirmado lendo o código-fonte da jefferson-experimental.
export function adicionarPerfilCandidato() {
  return api.put("/api/usuarios/me/perfil-candidato");
}

export function removerPerfilCandidato() {
  return api.delete("/api/usuarios/me/perfil-candidato");
}

/** { nomeDaEmpresa } — PUT, não POST (mesmo motivo do perfil-candidato acima). */
export function adicionarPerfilRecrutador(dados) {
  return api.put("/api/usuarios/me/perfil-recrutador", dados);
}

export function removerPerfilRecrutador() {
  return api.delete("/api/usuarios/me/perfil-recrutador");
}

/** { senhaAtual, novaSenha, confirmacaoNovaSenha } */
export function alterarSenha(dados) {
  return api.put("/api/usuarios/me/senha", dados);
}
