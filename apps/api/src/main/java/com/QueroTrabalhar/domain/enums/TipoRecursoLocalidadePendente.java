package com.QueroTrabalhar.domain.enums;

/**
 * Identifica o tipo do recurso que possui uma localidade em fila técnica.
 *
 * <p>O enum sustenta o vínculo por dono genérico da pendência, evitando que entidades de negócio
 * carreguem referência direta para {@code LocalidadePendente}.</p>
 */
public enum TipoRecursoLocalidadePendente {
    /** Empresa cujo campo de localidade ainda não foi validado. */
    EMPRESA,
    /** Oportunidade cujo campo de localidade ainda não foi validado. */
    OPORTUNIDADE_DE_EMPREGO
}
