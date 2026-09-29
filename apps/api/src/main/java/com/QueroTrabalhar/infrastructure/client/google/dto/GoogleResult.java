package com.QueroTrabalhar.infrastructure.client.google.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Representa um único resultado de endereço encontrado pelo Google.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleResult(

        @JsonProperty("address_components")
        List<AddressComponent> addressComponents,

        //Essa informação não tá sendo exposta.
        //Ela retorna uma informação maior e formatada.
        //Exemplo: pesquiso a cidade na Paraíba chamada Guarabira.
        //Ela retorna esse String: "Guarabira, PB, Brasil"
        //Ou seja, é a informação mais completa, mas que pode ser completamente montada pelas pequenas informações que o banco guarda.
        //MAS, pode ser usada pelo front em algum momento (discutir com YASMIM), talvez no mobile, não sei
        @JsonProperty("formatted_address")
        String formattedAddress
) {}
