package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.enums.Modalidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Agrupa os filtros da navegação pública de oportunidades dentro de uma empresa.
 *
 * <p>O identificador da empresa não faz parte deste DTO porque o escopo vem do parâmetro de caminho do endpoint. Os
 * campos aqui presentes apenas refinam as vagas públicas já pertencentes à empresa selecionada.</p>
 */
@Schema(name = "OportunidadesDaEmpresaFilterDTO", description = "Filtros disponíveis na listagem pública de oportunidades de uma empresa.")
public record OportunidadesDaEmpresaFilterDTO(
        @Size(max = 500, message = "O termo de busca deve ter no máximo 500 caracteres.")
        @Schema(description = "Busca textual aplicada à descrição da oportunidade.", example = "java")
        String termo,

        @Positive(message = "O tipo de emprego informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas da empresa pelo tipo de emprego.", example = "3")
        Long tipoDeEmpregoId,

        @Positive(message = "O recrutador informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas da empresa pelo recrutador responsável.", example = "12")
        Long recrutadorId,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas da empresa por país. Considera apenas recursos com localidade validada.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Filtra oportunidades públicas da empresa por estado. Considera apenas recursos com localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Filtra oportunidades públicas da empresa por cidade. Considera apenas recursos com localidade validada.", example = "2507507")
        Long cidadeId,

        @Schema(description = "Filtra oportunidades públicas da empresa pela modalidade.", example = "REMOTO")
        Modalidade modalidade
) {
}
