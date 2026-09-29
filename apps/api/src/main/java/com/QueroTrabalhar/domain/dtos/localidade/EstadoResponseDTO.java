package com.QueroTrabalhar.domain.dtos.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Estado;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EstadoResponseDTO", description = "Dados de um estado disponível no catálogo oficial de localidades.")
public record EstadoResponseDTO (
      @Schema(description = "Identificador do estado na base oficial.", example = "25")
      Long id,

      @Schema(description = "Nome do estado na base oficial.", example = "Paraíba")
      String nome,

      @Schema(description = "Sigla do estado na base oficial.", example = "PB")
      String sigla,

      @Schema(description = "Identificador do país associado ao estado, mantendo o nome de campo JSON atual.", example = "1")
      Long pais_id
){
    public static EstadoResponseDTO daEntidade(Estado entidade){
        return new EstadoResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getSigla(),
                entidade.getPais().getId()
        );
    }
}
