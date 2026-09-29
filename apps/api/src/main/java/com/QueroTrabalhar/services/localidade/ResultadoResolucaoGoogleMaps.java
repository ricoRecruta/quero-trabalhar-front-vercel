package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;

record ResultadoResolucaoGoogleMaps(
        Localidade localidade,
        MotivoPendenciaLocalidade motivoPendencia,
        int tentativasExecutadas
) {

    static ResultadoResolucaoGoogleMaps resolvida(Localidade localidade, int tentativasExecutadas) {
        return new ResultadoResolucaoGoogleMaps(localidade, null, tentativasExecutadas);
    }

    static ResultadoResolucaoGoogleMaps pendente(
            MotivoPendenciaLocalidade motivoPendencia,
            int tentativasExecutadas
    ) {
        return new ResultadoResolucaoGoogleMaps(null, motivoPendencia, tentativasExecutadas);
    }

    boolean resolvida() {
        return localidade != null;
    }
}
