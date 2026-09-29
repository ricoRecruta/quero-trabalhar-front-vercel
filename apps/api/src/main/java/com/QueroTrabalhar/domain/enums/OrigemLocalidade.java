package com.QueroTrabalhar.domain.enums;

/**
 * Indica de onde veio a informação usada no processo de validação de localidade.
 *
 * <p>No MVP a pendência persistida nasce do texto informado pelo usuário. A origem externa permanece
 * documentada para separar o dado digitado da integração usada apenas como apoio técnico de resolução.</p>
 */
public enum OrigemLocalidade {
    /** Texto livre informado pelo próprio usuário no cadastro. */
    USUARIO,
    /** Origem externa usada apenas como apoio técnico e não como fonte oficial pública. */
    GOOGLE_MAPS
}
