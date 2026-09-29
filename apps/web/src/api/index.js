/**
 * Ponto único de acesso à API.
 *
 * Uso:
 *   import { auth, oportunidades } from "../api";
 *   await auth.login({ email, password });
 *   const { itens } = await oportunidades.listar({ termo: "dev" });
 */

export * as admin from "./admin";
export * as auth from "./auth";
export * as candidatos from "./candidatos";
export * as empresas from "./empresas";
export * as experiencias from "./experiencias";
export * as indicacoes from "./indicacoes";
export * as localidades from "./localidades";
export * as oportunidades from "./oportunidades";
export * as recrutadores from "./recrutadores";
export * as tiposDeEmprego from "./tiposDeEmprego";
export * as usuarios from "./usuarios";

export {
  ApiError,
  api,
  buildQuery,
  clearToken,
  estaAutenticado,
  getToken,
  normalizarPagina,
  request,
  requestRaw,
  setToken,
} from "./client";

export { mensagemDeErro } from "./erros";
