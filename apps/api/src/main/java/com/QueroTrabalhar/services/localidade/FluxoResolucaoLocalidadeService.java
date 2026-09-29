package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import org.springframework.stereotype.Service;

/**
 * Concentra a tentativa pura de resolução de localidade, sem persistir pendências.
 *
 * <p>A separação desta classe evita duplicar regra de decisão entre o fluxo normal de cadastro
 * e o reprocessamento da fila técnica. Primeiro ela consulta o catálogo interno oficial; só depois
 * aciona o Google Maps como detalhe de integração encapsulado. Ambiguidades ou falhas retornam
 * apenas o motivo técnico da pendência.</p>
 */
@Service
class FluxoResolucaoLocalidadeService {

    private final ResolvedorLocalidadeBaseInterna resolvedorLocalidadeBaseInterna;
    private final ResolvedorLocalidadeGoogleMaps resolvedorLocalidadeGoogleMaps;

    FluxoResolucaoLocalidadeService(
            ResolvedorLocalidadeBaseInterna resolvedorLocalidadeBaseInterna,
            ResolvedorLocalidadeGoogleMaps resolvedorLocalidadeGoogleMaps
    ) {
        this.resolvedorLocalidadeBaseInterna = resolvedorLocalidadeBaseInterna;
        this.resolvedorLocalidadeGoogleMaps = resolvedorLocalidadeGoogleMaps;
    }

    /**
     * Tenta resolver o texto informado sem efeitos colaterais de persistência.
     */
    ResultadoTentativaResolucaoLocalidade resolver(String textoLivre) {
        String textoNormalizado = normalizarTextoObrigatorio(textoLivre);
        String textoHash = gerarTextoHash(textoNormalizado);

        ResultadoResolucaoBaseInterna resultadoBaseInterna =
                resolvedorLocalidadeBaseInterna.resolver(textoNormalizado, textoHash);
        if (resultadoBaseInterna.resolvida()) {
            return ResultadoTentativaResolucaoLocalidade.resolvida(
                    textoNormalizado,
                    textoHash,
                    resultadoBaseInterna.localidade(),
                    OrigemResolucaoLocalidade.BASE_INTERNA,
                    0
            );
        }
        if (resultadoBaseInterna.deveGerarPendencia()) {
            return ResultadoTentativaResolucaoLocalidade.pendente(
                    textoNormalizado,
                    textoHash,
                    resultadoBaseInterna.motivoPendencia(),
                    0
            );
        }

        ResultadoResolucaoGoogleMaps resultadoGoogleMaps =
                resolvedorLocalidadeGoogleMaps.resolver(textoNormalizado, textoHash);
        if (resultadoGoogleMaps.resolvida()) {
            return ResultadoTentativaResolucaoLocalidade.resolvida(
                    textoNormalizado,
                    textoHash,
                    resultadoGoogleMaps.localidade(),
                    OrigemResolucaoLocalidade.GOOGLE_MAPS,
                    resultadoGoogleMaps.tentativasExecutadas()
            );
        }

        return ResultadoTentativaResolucaoLocalidade.pendente(
                textoNormalizado,
                textoHash,
                resultadoGoogleMaps.motivoPendencia(),
                resultadoGoogleMaps.tentativasExecutadas()
        );
    }

    private String normalizarTextoObrigatorio(String textoLivre) {
        String textoNormalizado = normalizarTextoOpcional(textoLivre);
        if (textoNormalizado == null) {
            throw new BusinessRuleException("O texto da localidade é obrigatório.");
        }
        return textoNormalizado;
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isBlank() ? null : valorNormalizado;
    }

    private String gerarTextoHash(String textoNormalizado) {
        return Long.toHexString(Integer.toUnsignedLong(textoNormalizado.hashCode()));
    }

    enum OrigemResolucaoLocalidade {
        BASE_INTERNA,
        GOOGLE_MAPS
    }

    /**
     * Resultado imutável da tentativa de resolução.
     *
     * <p>Por contrato, a instância carrega exatamente uma destas informações: uma localidade validada
     * pronta para uso no domínio ou um motivo técnico de pendência.</p>
     */
    record ResultadoTentativaResolucaoLocalidade(
            String textoNormalizado,
            String textoHash,
            Localidade localidadeValidada,
            MotivoPendenciaLocalidade motivoPendencia,
            OrigemResolucaoLocalidade origemResolucao,
            int tentativasExecutadas
    ) {

        ResultadoTentativaResolucaoLocalidade {
            boolean possuiLocalidadeValidada = localidadeValidada != null;
            boolean possuiMotivoPendencia = motivoPendencia != null;

            if (possuiLocalidadeValidada == possuiMotivoPendencia) {
                throw new IllegalArgumentException(
                        "A tentativa de resolução deve conter localidade validada ou motivo de pendência."
                );
            }
        }

        static ResultadoTentativaResolucaoLocalidade resolvida(
                String textoNormalizado,
                String textoHash,
                Localidade localidadeValidada,
                OrigemResolucaoLocalidade origemResolucao,
                int tentativasExecutadas
        ) {
            return new ResultadoTentativaResolucaoLocalidade(
                    textoNormalizado,
                    textoHash,
                    localidadeValidada,
                    null,
                    origemResolucao,
                    tentativasExecutadas
            );
        }

        static ResultadoTentativaResolucaoLocalidade pendente(
                String textoNormalizado,
                String textoHash,
                MotivoPendenciaLocalidade motivoPendencia,
                int tentativasExecutadas
        ) {
            return new ResultadoTentativaResolucaoLocalidade(
                    textoNormalizado,
                    textoHash,
                    null,
                    motivoPendencia,
                    null,
                    tentativasExecutadas
            );
        }

        boolean resolvida() {
            return localidadeValidada != null;
        }

        String nivelLocalidade() {
            if (!resolvida()) {
                return null;
            }
            if (localidadeValidada.getCidade() != null) {
                return "CIDADE";
            }
            if (localidadeValidada.getEstado() != null) {
                return "ESTADO";
            }
            return "PAIS";
        }
    }
}
