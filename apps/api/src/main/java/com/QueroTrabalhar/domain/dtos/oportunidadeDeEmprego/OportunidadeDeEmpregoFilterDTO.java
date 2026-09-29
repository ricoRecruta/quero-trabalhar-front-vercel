package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.enums.Modalidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Agrupa os filtros aceitos pela listagem pública geral de oportunidades.
 *
 * <p>Os campos deste DTO apenas refinam o catálogo público. O filtro não muda a regra-base do módulo: vagas com
 * localidade pendente permanecem invisíveis nesse recorte.</p>
 */
@Schema(name = "OportunidadeDeEmpregoFilterDTO", description = "Filtros disponíveis na listagem pública de oportunidades de emprego.")
public record OportunidadeDeEmpregoFilterDTO(
        @Size(max = 500, message = "O termo de busca deve ter no máximo 500 caracteres.")
        @Schema(description = "Busca textual aplicada à descrição da oportunidade.", example = "spring boot")
        String termo,

        @Positive(message = "O tipo de emprego informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas pelo tipo de emprego.", example = "3")
        Long tipoDeEmpregoId,

        @Positive(message = "A empresa informada é inválida.")
        @Schema(description = "Filtra oportunidades públicas pela empresa associada à publicação.", example = "7")
        Long empresaId,

        @Positive(message = "O recrutador informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas pelo recrutador responsável.", example = "12")
        Long recrutadorId,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas por país. Considera apenas recursos com localidade validada.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas por estado. Considera apenas recursos com localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Filtra oportunidades públicas por cidade. Considera apenas recursos com localidade validada.", example = "2507507")
        Long cidadeId,

        @Schema(description = "Filtra oportunidades públicas pela modalidade.", example = "REMOTO")
        Modalidade modalidade
) {
}
