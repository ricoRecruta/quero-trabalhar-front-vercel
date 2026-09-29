package com.QueroTrabalhar.domain.dtos.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Pais;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PaisResponseDTO", description = "Dados de um país disponível no catálogo oficial de localidades.")
public record PaisResponseDTO(
        @Schema(description = "Identificador do país na base oficial.", example = "1")
        Long id,

        @Schema(description = "Nome do país na base oficial.", example = "Brasil")
        String nome,

        @Schema(description = "Sigla do país na base oficial.", example = "BR")
        String sigla
) {
    public static PaisResponseDTO daEntidade(Pais entidade) {
        return new PaisResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getSigla()
        );
    }
}
