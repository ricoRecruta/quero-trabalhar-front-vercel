package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;

record ResultadoResolucaoBaseInterna(Localidade localidade, MotivoPendenciaLocalidade motivoPendencia) {

    static ResultadoResolucaoBaseInterna resolvida(Localidade localidade) {
        return new ResultadoResolucaoBaseInterna(localidade, null);
    }

    static ResultadoResolucaoBaseInterna ambigua() {
        return new ResultadoResolucaoBaseInterna(null, MotivoPendenciaLocalidade.LOCALIDADE_AMBIGUA_BASE_INTERNA);
    }

    static ResultadoResolucaoBaseInterna semMatch() {
        return new ResultadoResolucaoBaseInterna(null, null);
    }

    boolean resolvida() {
        return localidade != null;
    }

    boolean deveGerarPendencia() {
        return motivoPendencia != null;
    }
}
