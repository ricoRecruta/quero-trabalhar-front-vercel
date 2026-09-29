package com.QueroTrabalhar.infrastructure.client.google.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Representa a raiz do JSON retornado pelo Google Geocoding API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleGeocodeResponse(
        List<GoogleResult> results,
        String status
) {
    public boolean isSucesso() {
        return "OK".equals(this.status) && this.results != null && !this.results.isEmpty();
    }
}
