package com.QueroTrabalhar.infrastructure.client.google;

import com.QueroTrabalhar.infrastructure.client.google.dto.GoogleGeocodeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
public class GoogleMapsClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsClient.class);

    private final RestTemplate restTemplate;

    @Value("${google.maps.api.url}")
    private String domain;

    @Value("${google.maps.api.path}")
    private String path;

    @Value("${google.maps.api.key}")
    private String apiKey;

    public GoogleMapsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<GoogleGeocodeResponse> buscarLugarComFiltro(String termoBusca, String filtros) {
        long inicioChamada = System.nanoTime();
        String termoHash = gerarTextoHash(termoBusca);
        boolean filtrosPresentes = filtros != null && !filtros.isBlank();

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(domain + path)
                    .queryParam("address", termoBusca)
                    .queryParam("language", "pt-BR")
                    .queryParam("key", apiKey);

            if (filtrosPresentes) {
                builder.queryParam("components", filtros);
            }

            GoogleGeocodeResponse response = restTemplate.getForObject(
                    builder.build().toUri(),
                    GoogleGeocodeResponse.class
            );
            return Optional.ofNullable(response);
        } catch (Exception exception) {
            logger.warn(
                    "event=google_maps_request_falhou termoHash={} filtrosPresentes={} duracaoMs={} excecao={}",
                    termoHash,
                    filtrosPresentes,
                    calcularDuracaoMs(inicioChamada),
                    exception.getClass().getSimpleName()
            );
            logger.debug("Detalhes da falha ao comunicar com Google Maps.", exception);
            return Optional.empty();
        }
    }

    private long calcularDuracaoMs(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }

    private String gerarTextoHash(String termoBusca) {
        if (termoBusca == null) {
            return "null";
        }

        return Long.toHexString(Integer.toUnsignedLong(termoBusca.trim().hashCode()));
    }
}
