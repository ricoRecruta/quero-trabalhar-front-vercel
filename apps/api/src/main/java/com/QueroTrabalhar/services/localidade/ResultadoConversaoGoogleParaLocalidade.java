package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;

record ResultadoConversaoGoogleParaLocalidade(Localidade localidade, String motivoInvalidez) {

    static ResultadoConversaoGoogleParaLocalidade resolvida(Localidade localidade) {
        return new ResultadoConversaoGoogleParaLocalidade(localidade, null);
    }

    static ResultadoConversaoGoogleParaLocalidade invalida(String motivoInvalidez) {
        return new ResultadoConversaoGoogleParaLocalidade(null, motivoInvalidez);
    }

    boolean resolvida() {
        return localidade != null;
    }
}
