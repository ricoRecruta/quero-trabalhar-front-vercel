package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.infrastructure.client.google.GoogleMapsClient;
import com.QueroTrabalhar.infrastructure.client.google.dto.GoogleGeocodeResponse;
import com.QueroTrabalhar.infrastructure.client.google.dto.GoogleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Executa o fallback de integração externa para resolução de localidade.
 *
 * <p>O Google Maps só entra em cena depois que a base interna oficial não resolve o texto.
 * A integração fica encapsulada nesta etapa para não contaminar consultas públicas com fonte
 * externa. Resultados ambíguos, ausentes ou estruturalmente inválidos não são escolhidos
 * automaticamente; nesses casos o fluxo devolve motivo para manter ou criar pendência.</p>
 */
@Service
public class ResolvedorLocalidadeGoogleMaps {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedorLocalidadeGoogleMaps.class);

    private final GoogleMapsClient googleMapsClient;
    private final ConversorGoogleResultParaLocalidade conversorGoogleResultParaLocalidade;

    @Value("${google.maps.retry.max-attempts:2}")
    private int googleMapsRetryMaxAttempts = 2;

    @Value("${google.maps.retry.delay-ms:200}")
    private long googleMapsRetryDelayMs = 200L;

    public ResolvedorLocalidadeGoogleMaps(
            GoogleMapsClient googleMapsClient,
            ConversorGoogleResultParaLocalidade conversorGoogleResultParaLocalidade
    ) {
        this.googleMapsClient = googleMapsClient;
        this.conversorGoogleResultParaLocalidade = conversorGoogleResultParaLocalidade;
    }

    /**
     * Consulta a integração externa e retorna uma localidade validada apenas quando há resultado único e coerente.
     */
    public ResultadoResolucaoGoogleMaps resolver(String textoNormalizado, String textoHash) {
        int maxAttempts = Math.max(1, googleMapsRetryMaxAttempts);
        logger.info(
                "event=localidade_google_necessaria textoHash={} maxTentativas={}",
                textoHash,
                maxAttempts
        );

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info(
                    "event=localidade_google_tentativa textoHash={} tentativa={} maxTentativas={}",
                    textoHash,
                    attempt,
                    maxAttempts
            );

            ResultadoConsultaGoogleMaps resultadoConsulta = consultarGoogle(textoNormalizado, textoHash, attempt);
            if (!resultadoConsulta.deveTentarNovamente() || attempt == maxAttempts) {
                return resultadoConsulta.toResultadoResolucao(attempt);
            }

            logger.warn(
                    "event=localidade_google_retry textoHash={} tentativaAtual={} proximaTentativa={} motivo=FALHA_TRANSITORIA_OU_RESPOSTA_AUSENTE",
                    textoHash,
                    attempt,
                    attempt + 1
            );

            if (!aguardarRetry(textoHash, attempt, maxAttempts)) {
                return ResultadoResolucaoGoogleMaps.pendente(
                        MotivoPendenciaLocalidade.FALHA_INTEGRACAO_EXTERNA,
                        attempt
                );
            }
        }

        return ResultadoResolucaoGoogleMaps.pendente(
                MotivoPendenciaLocalidade.FALHA_INTEGRACAO_EXTERNA,
                maxAttempts
        );
    }

    private ResultadoConsultaGoogleMaps consultarGoogle(String textoNormalizado, String textoHash, int attempt) {
        long inicioConsulta = System.nanoTime();
        Optional<GoogleGeocodeResponse> responseOptional;

        try {
            responseOptional = googleMapsClient.buscarLugarComFiltro(textoNormalizado, null);
        } catch (RuntimeException exception) {
            logger.warn(
                    "event=localidade_google_falha_transitoria textoHash={} tentativa={} duracaoMs={} excecao={}",
                    textoHash,
                    attempt,
                    calcularDuracaoMs(inicioConsulta),
                    exception.getClass().getSimpleName()
            );
            logger.debug("Detalhes da falha transitória durante a resolução de localidade.", exception);
            return ResultadoConsultaGoogleMaps.retryFalhaExterna();
        }

        long duracaoMs = calcularDuracaoMs(inicioConsulta);
        if (responseOptional.isEmpty()) {
            logger.warn(
                    "event=localidade_google_resposta_ausente textoHash={} tentativa={} duracaoMs={}",
                    textoHash,
                    attempt,
                    duracaoMs
            );
            return ResultadoConsultaGoogleMaps.retryFalhaExterna();
        }

        GoogleGeocodeResponse response = responseOptional.get();
        String statusGoogle = normalizarStatusGoogle(response.status());
        if ("ZERO_RESULTS".equalsIgnoreCase(response.status())) {
            logger.warn(
                    "event=localidade_google_zero_results textoHash={} tentativa={} status={} quantidadeResultados={} duracaoMs={}",
                    textoHash,
                    attempt,
                    statusGoogle,
                    contarResultados(response.results()),
                    duracaoMs
            );
            return ResultadoConsultaGoogleMaps.pendente(MotivoPendenciaLocalidade.LOCALIDADE_NAO_ENCONTRADA);
        }

        List<GoogleResult> resultados = response.results();
        if (resultados == null || resultados.isEmpty()) {
            logger.warn(
                    "event=localidade_google_resultados_vazios textoHash={} tentativa={} status={} quantidadeResultados=0 duracaoMs={}",
                    textoHash,
                    attempt,
                    statusGoogle,
                    duracaoMs
            );
            return ResultadoConsultaGoogleMaps.pendente(MotivoPendenciaLocalidade.LOCALIDADE_NAO_ENCONTRADA);
        }

        if (!response.isSucesso()) {
            logger.warn(
                    "event=localidade_google_status_invalido textoHash={} tentativa={} status={} quantidadeResultados={} duracaoMs={}",
                    textoHash,
                    attempt,
                    statusGoogle,
                    resultados.size(),
                    duracaoMs
            );
            return ResultadoConsultaGoogleMaps.pendente(MotivoPendenciaLocalidade.FALHA_INTEGRACAO_EXTERNA);
        }

        if (resultados.size() > 1) {
            logger.warn(
                    "event=localidade_google_resultado_ambiguo textoHash={} tentativa={} status={} quantidadeResultados={} estrategia=PENDENCIA duracaoMs={}",
                    textoHash,
                    attempt,
                    statusGoogle,
                    resultados.size(),
                    duracaoMs
            );
            return ResultadoConsultaGoogleMaps.pendente(MotivoPendenciaLocalidade.LOCALIDADE_AMBIGUA_GOOGLE);
        }

        ResultadoConversaoGoogleParaLocalidade resultadoConversao =
                conversorGoogleResultParaLocalidade.converter(resultados.getFirst());
        if (resultadoConversao.resolvida()) {
            Localidade localidade = resultadoConversao.localidade();
            logger.info(
                    "event=localidade_google_sucesso textoHash={} tentativa={} nivel={} quantidadeResultados={} duracaoMs={}",
                    textoHash,
                    attempt,
                    determinarNivel(localidade),
                    resultados.size(),
                    duracaoMs
            );
            return ResultadoConsultaGoogleMaps.resolvida(localidade);
        }

        logger.warn(
                "event=localidade_google_resultado_invalido textoHash={} tentativa={} quantidadeResultados={} motivo={} duracaoMs={}",
                textoHash,
                attempt,
                resultados.size(),
                resultadoConversao.motivoInvalidez(),
                duracaoMs
        );
        return ResultadoConsultaGoogleMaps.pendente(MotivoPendenciaLocalidade.LOCALIDADE_NAO_ENCONTRADA);
    }

    private boolean aguardarRetry(String textoHash, int attempt, int maxAttempts) {
        if (googleMapsRetryDelayMs <= 0) {
            return true;
        }

        try {
            Thread.sleep(googleMapsRetryDelayMs);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.warn(
                    "event=localidade_google_retry_interrompido textoHash={} tentativaAtual={} maxTentativas={} delayMs={}",
                    textoHash,
                    attempt,
                    maxAttempts,
                    googleMapsRetryDelayMs
            );
            return false;
        }
    }

    private String determinarNivel(Localidade localidade) {
        if (localidade.getCidade() != null) {
            return "CIDADE";
        }
        if (localidade.getEstado() != null) {
            return "ESTADO";
        }
        return "PAIS";
    }

    private String normalizarStatusGoogle(String statusGoogle) {
        return textoPreenchido(statusGoogle) ? statusGoogle : "SEM_STATUS";
    }

    private boolean textoPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private int contarResultados(List<GoogleResult> resultados) {
        return resultados == null ? 0 : resultados.size();
    }

    private long calcularDuracaoMs(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }

    private record ResultadoConsultaGoogleMaps(
            Localidade localidade,
            MotivoPendenciaLocalidade motivoPendencia,
            boolean deveTentarNovamente
    ) {

        private ResultadoResolucaoGoogleMaps toResultadoResolucao(int tentativasExecutadas) {
            if (localidade != null) {
                return ResultadoResolucaoGoogleMaps.resolvida(localidade, tentativasExecutadas);
            }
            return ResultadoResolucaoGoogleMaps.pendente(motivoPendencia, tentativasExecutadas);
        }

        private static ResultadoConsultaGoogleMaps resolvida(Localidade localidade) {
            return new ResultadoConsultaGoogleMaps(localidade, null, false);
        }

        private static ResultadoConsultaGoogleMaps pendente(MotivoPendenciaLocalidade motivoPendencia) {
            return new ResultadoConsultaGoogleMaps(null, motivoPendencia, false);
        }

        private static ResultadoConsultaGoogleMaps retryFalhaExterna() {
            return new ResultadoConsultaGoogleMaps(
                    null,
                    MotivoPendenciaLocalidade.FALHA_INTEGRACAO_EXTERNA,
                    true
            );
        }
    }
}
