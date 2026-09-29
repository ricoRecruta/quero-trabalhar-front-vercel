package com.QueroTrabalhar.domain.dtos.tipoDeEmprego;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(
        name = "TipoDeEmpregoFilterDTO",
        description = "Filtros disponíveis na fila administrativa de tipos de emprego não aprovados. O backend aplica aprovado=false de forma obrigatória."
)
public record TipoDeEmpregoFilterDTO(
        @Size(max = 150, message = "O termo de busca deve ter no máximo 150 caracteres.")
        @Schema(description = "Busca textual aplicada ao título e à descrição dos tipos pendentes.", example = "backend")
        String termo
) {
}
