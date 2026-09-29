package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;

import java.util.Objects;

public record ResultadoResolucaoLocalidade(
        Localidade localidadeValidada,
        LocalidadePendente localidadePendente
) {

    public ResultadoResolucaoLocalidade {
        boolean possuiLocalidadeValidada = localidadeValidada != null;
        boolean possuiLocalidadePendente = localidadePendente != null;

        if (possuiLocalidadeValidada == possuiLocalidadePendente) {
            throw new IllegalArgumentException(
                    "O resultado da resolução deve conter exatamente uma localidade validada ou pendente."
            );
        }
    }

    public static ResultadoResolucaoLocalidade resolvida(Localidade localidadeValidada) {
        return new ResultadoResolucaoLocalidade(
                Objects.requireNonNull(localidadeValidada, "A localidade validada é obrigatória."),
                null
        );
    }

    public static ResultadoResolucaoLocalidade pendente(LocalidadePendente localidadePendente) {
        return new ResultadoResolucaoLocalidade(
                null,
                Objects.requireNonNull(localidadePendente, "A localidade pendente é obrigatória.")
        );
    }

    public boolean resolvida() {
        return localidadeValidada != null;
    }

    public boolean pendente() {
        return localidadePendente != null;
    }
}
