package com.QueroTrabalhar.domain.dtos.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Agrupa os filtros aceitos pelo catálogo público de empresas.
 *
 * <p>Os parâmetros deste DTO apenas refinam o recorte público; eles não conseguem incluir empresas com localidade
 * pendente, porque essa exclusão acontece antes da aplicação dos demais predicados.</p>
 */
@Schema(name = "EmpresaFilterDTO", description = "Filtros disponíveis na listagem pública de empresas.")
public record EmpresaFilterDTO(
        @Size(max = 150, message = "O termo de busca deve ter no máximo 150 caracteres.")
        @Schema(description = "Busca textual aplicada ao nome da empresa.", example = "tecnologia")
        String termo,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Filtra empresas públicas por país. Considera apenas empresas com localidade validada.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Filtra empresas públicas por estado. Considera apenas empresas com localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Filtra empresas públicas por cidade. Considera apenas empresas com localidade validada.", example = "2507507")
        Long cidadeId
) {
}
