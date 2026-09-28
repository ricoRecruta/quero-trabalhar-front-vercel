/**
 * Cliente HTTP da API Quero Trabalhar.
 *
 * Contrato seguido: docs/api/QueroTrabalhar_API.postman_collection.json
 *
 * Base URL:
 *   - Em dev, VITE_API_BASE_URL fica vazio e o proxy do Vite (vite.config.js)
 *     redireciona /login, /api e /auth para o backend. Isso mantém tudo na
 *     mesma origem e evita CORS.
 *   - Em produção/container, o nginx faz o mesmo proxy (docker/nginx.conf).
 *   - Se preferir apontar direto para o backend, defina
 *     VITE_API_BASE_URL=http://localhost:8080 (exige CORS liberado na API).
 */

const BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/+$/, "");

const TOKEN_KEY = "queroTrabalhar.token";

/** Erro lançado por qualquer chamada que retorne status >= 400. */
export class ApiError extends Error {
  constructor(status, corpo, mensagem) {
    super(mensagem || `Falha na requisição (HTTP ${status})`);
    this.name = "ApiError";
    this.status = status;
    this.corpo = corpo;
  }

  get naoAutorizado() {
    return this.status === 401 || this.status === 403;
  }
}

/* ----------------------------- token JWT ----------------------------- */

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    /* localStorage indisponível (modo privado, por exemplo) */
  }
}

export function clearToken() {
  setToken(null);
}

export function estaAutenticado() {
  return Boolean(getToken());
}

/* --------------------------- montagem da URL -------------------------- */

/** Monta a query string ignorando valores vazios, null e undefined. */
export function buildQuery(params) {
  if (!params) return "";
  const busca = new URLSearchParams();

  for (const [chave, valor] of Object.entries(params)) {
    if (valor === undefined || valor === null || valor === "") continue;
    if (Array.isArray(valor)) {
      valor.forEach((item) => {
        if (item !== undefined && item !== null && item !== "") {
          busca.append(chave, item);
        }
      });
    } else {
      busca.append(chave, valor);
    }
  }

  const texto = busca.toString();
  return texto ? `?${texto}` : "";
}

/* ------------------------------ requisição ---------------------------- */

async function lerCorpo(resposta) {
  if (resposta.status === 204) return null;

  const tipo = resposta.headers.get("Content-Type") || "";
  const texto = await resposta.text();
  if (!texto) return null;

  if (tipo.includes("application/json")) {
    try {
      return JSON.parse(texto);
    } catch {
      return texto;
    }
  }
  return texto;
}

// Mensagens do Hibernate Validator que já sabemos traduzir. A validação de
// CPF (@CPF na entidade Usuario) usa o texto padrão em inglês da lib —
// vale a pena traduzir esse caso específico porque é um erro comum de
// digitação no cadastro.
const TRADUCOES_CONHECIDAS = {
  "invalid Brazilian individual taxpayer registry number (CPF)":
    "CPF inválido — confira os números digitados.",
};

/**
 * Nem toda falha de validação passa pelo formato limpo do
 * ResourceExceptionHandler (`errors: [...]`, tratado abaixo). Quando a
 * validação falha no momento de gravar a ENTIDADE (não o DTO recebido —
 * ex.: o @CPF do Hibernate Validator na entidade Usuario), o backend deixa
 * vazar o `toString()` bruto de um `ConstraintViolationException` dentro do
 * campo `message`, algo como:
 *   "Validation failed for classes [...] ...
 *    ConstraintViolationImpl{interpolatedMessage='invalid Brazilian...',
 *    propertyPath=cpf, ...}"
 * Isso não é um bug do front — é o backend não formatando esse tipo de
 * exceção como os outros. Como não dá pra corrigir o backend aqui, este
 * parser extrai só a parte útil (campo + mensagem) de cada violação em vez
 * de jogar esse texto de exceção Java na tela do usuário.
 */
function extrairViolacoesDeEntidade(mensagem) {
  if (typeof mensagem !== "string" || !mensagem.includes("ConstraintViolationImpl")) {
    return null;
  }

  const blocos = mensagem.match(/ConstraintViolationImpl\{[^}]*\}/g);
  if (!blocos || blocos.length === 0) return null;

  const partes = blocos
    .map((bloco) => {
      const textoOriginal = bloco.match(/interpolatedMessage='([^']*)'/)?.[1];
      const campo = bloco.match(/propertyPath=(\w+)/)?.[1];
      if (!textoOriginal) return null;

      const traduzido = TRADUCOES_CONHECIDAS[textoOriginal] ?? textoOriginal;
      // Pro caso do CPF (e outros já traduzidos), a mensagem já deixa claro
      // o campo — não precisa repetir "cpf: ...". Pros demais, mantém o
      // nome do campo já que a mensagem crua da lib não é auto-explicativa.
      if (campo && !TRADUCOES_CONHECIDAS[textoOriginal]) {
        return `${campo}: ${traduzido}`;
      }
      return traduzido;
    })
    .filter(Boolean);

  return partes.length > 0 ? partes.join(" | ") : null;
}

function mensagemDeErro(corpo, status) {
  if (!corpo) return `Falha na requisição (HTTP ${status})`;
  if (typeof corpo === "string") return corpo;
  // Formato limpo do ResourceExceptionHandler para @Valid em DTOs.
  if (Array.isArray(corpo.errors) && corpo.errors.length) {
    return corpo.errors.map((e) => e.message || e.fieldMessage).join(", ");
  }
  if (corpo.message) {
    return extrairViolacoesDeEntidade(corpo.message) ?? corpo.message;
  }
  if (corpo.error) return corpo.error;
  return `Falha na requisição (HTTP ${status})`;
}

/**
 * Executa a requisição e devolve o objeto Response cru.
 * Use quando precisar dos cabeçalhos — o login, por exemplo, lê o token
 * do cabeçalho Authorization em vez do corpo.
 */
export async function requestRaw(caminho, opcoes = {}) {
  const { method = "GET", body, query, auth = true, headers = {}, signal } = opcoes;

  const cabecalhos = { Accept: "application/json", ...headers };

  if (body !== undefined && !(body instanceof FormData)) {
    cabecalhos["Content-Type"] = "application/json";
  }

  if (auth) {
    const token = getToken();
    if (token) cabecalhos.Authorization = `Bearer ${token}`;
  }

  let corpoEnviado;
  if (body instanceof FormData) corpoEnviado = body;
  else if (body !== undefined) corpoEnviado = JSON.stringify(body);

  return fetch(`${BASE_URL}${caminho}${buildQuery(query)}`, {
    method,
    headers: cabecalhos,
    body: corpoEnviado,
    signal,
  });
}

/** Executa a requisição e devolve o corpo já desserializado. */
export async function request(caminho, opcoes = {}) {
  const resposta = await requestRaw(caminho, opcoes);
  const corpo = await lerCorpo(resposta);

  if (!resposta.ok) {
    throw new ApiError(resposta.status, corpo, mensagemDeErro(corpo, resposta.status));
  }
  return corpo;
}

export const api = {
  get: (caminho, opcoes) => request(caminho, { ...opcoes, method: "GET" }),
  post: (caminho, body, opcoes) => request(caminho, { ...opcoes, method: "POST", body }),
  put: (caminho, body, opcoes) => request(caminho, { ...opcoes, method: "PUT", body }),
  patch: (caminho, body, opcoes) => request(caminho, { ...opcoes, method: "PATCH", body }),
  delete: (caminho, opcoes) => request(caminho, { ...opcoes, method: "DELETE" }),
};

/**
 * Normaliza a resposta paginada do Spring (Page<T>) para um formato estável,
 * tolerando endpoints que devolvem uma lista simples.
 */
export function normalizarPagina(resposta) {
  if (Array.isArray(resposta)) {
    return {
      itens: resposta,
      pagina: 0,
      tamanho: resposta.length,
      totalItens: resposta.length,
      totalPaginas: 1,
      ultima: true,
    };
  }
  if (!resposta || typeof resposta !== "object") {
    return { itens: [], pagina: 0, tamanho: 0, totalItens: 0, totalPaginas: 0, ultima: true };
  }
  return {
    itens: resposta.content ?? [],
    pagina: resposta.number ?? 0,
    tamanho: resposta.size ?? (resposta.content?.length ?? 0),
    totalItens: resposta.totalElements ?? (resposta.content?.length ?? 0),
    totalPaginas: resposta.totalPages ?? 1,
    ultima: resposta.last ?? true,
  };
}
