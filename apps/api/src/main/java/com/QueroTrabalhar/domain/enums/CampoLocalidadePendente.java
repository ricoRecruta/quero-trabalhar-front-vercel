package com.QueroTrabalhar.domain.enums;

/**
 * Identifica qual campo do recurso dono originou a pendência.
 *
 * <p>No MVP existe apenas o campo de localidade, mas a enumeração preserva a modelagem genérica do
 * vínculo técnico com o recurso e evita endurecer a solução em torno de uma única entidade.</p>
 */
public enum CampoLocalidadePendente {
    /** Campo de localidade ainda não resolvido para o recurso associado. */
    LOCALIDADE
}
