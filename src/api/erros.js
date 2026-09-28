import { ApiError } from "./client";

/** Extrai uma mensagem legível de qualquer erro capturado numa chamada à API. */
export function mensagemDeErro(erro, mensagemPadrao) {
  return erro instanceof ApiError
    ? erro.message
    : (mensagemPadrao ??
        "Não foi possível completar a operação. Verifique se a API está no ar.");
}
