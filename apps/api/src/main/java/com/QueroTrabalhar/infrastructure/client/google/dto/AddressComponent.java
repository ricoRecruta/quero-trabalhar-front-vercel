package com.QueroTrabalhar.infrastructure.client.google.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Representa cada "pedaço" do endereço (Ex: A cidade, o estado, o país).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressComponent(

        @JsonProperty("long_name")
        String longName,

        @JsonProperty("short_name")
        String shortName,

        //Tags do Google. Exemplo: locality, administrative_area, etc etc
        List<String> types
) {}
