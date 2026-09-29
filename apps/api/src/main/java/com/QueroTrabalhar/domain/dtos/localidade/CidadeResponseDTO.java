package com.QueroTrabalhar.domain.dtos.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CidadeResponseDTO", description = "Dados de uma cidade disponível no catálogo oficial de localidades.")
public record CidadeResponseDTO(
        @Schema(description = "Identificador da cidade na base oficial.", example = "2507507")
        Long id,

        @Schema(description = "Nome da cidade na base oficial.", example = "João Pessoa")
        String nome,

        @Schema(description = "Identificador do estado associado à cidade, mantendo o nome de campo JSON atual.", example = "25")
        Long estado
) {
    public static CidadeResponseDTO daEntidade(Cidade entidade){
        return new CidadeResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEstado().getId()
        );
    }
}
