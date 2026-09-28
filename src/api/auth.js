import { ApiError, api, clearToken, requestRaw, setToken } from "./client";

/**
 * Autentica e guarda o token.
 *
 * Atenção: a API devolve o JWT no cabeçalho `Authorization` da resposta
 * (e não no corpo) — ver o script de teste do Postman em
 * "00 - Autenticação / Login". Por isso usamos requestRaw aqui.
 */
export async function login({ email, password }) {
  const resposta = await requestRaw("/login", {
    method: "POST",
    auth: false,
    body: { email, password },
  });

  if (!resposta.ok) {
    throw new ApiError(
      resposta.status,
      null,
      resposta.status === 401 || resposta.status === 403
        ? "E-mail ou senha inválidos."
        : `Não foi possível entrar (HTTP ${resposta.status}).`
    );
  }

  const cabecalho = resposta.headers.get("Authorization");
  const token = cabecalho ? cabecalho.replace(/^Bearer\s*/i, "").trim() : null;

  if (!token) {
    throw new ApiError(
      resposta.status,
      null,
      "Login aceito, mas o token não veio no cabeçalho Authorization. " +
        "Verifique se o proxy expõe esse cabeçalho."
    );
  }

  setToken(token);
  return token;
}

export function logout() {
  clearToken();
}

/** Dados do usuário autenticado. */
export function usuarioAtual() {
  return api.get("/api/usuarios/me");
}
