package com.QueroTrabalhar.domain.enums;

/**
 * Filtro simplificado para distinguir recursos com localidade oficial daqueles ainda retidos por pendência.
 *
 * <p>Ele é especialmente útil em consultas autenticadas do dono do recurso. Endpoints públicos devem
 * continuar exibindo apenas recursos com localidade validada.</p>
 */
public enum StatusLocalidadeFiltro {
    /** Recurso já associado a uma localidade oficial validada. */
    VALIDADA,
    /** Recurso ainda oculto do fluxo público por depender de resolução técnica. */
    PENDENTE
}
