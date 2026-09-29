package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusLocalidadeFiltro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Agrupa os filtros da visão interna de oportunidades do recrutador autenticado.
 *
 * <p>Não existe campo de {@code recrutadorId} porque o escopo de {@code /me} é determinado pelo usuário autenticado.
 * Diferentemente da API pública, este DTO pode pedir explicitamente vagas com localidade pendente para apoiar a
 * gestão do próprio recurso.</p>
 */
@Schema(name = "OportunidadeRecrutadorMeFilterDTO", description = "Filtros disponíveis na listagem paginada das oportunidades do recrutador autenticado.")
public record OportunidadeRecrutadorMeFilterDTO(
        @Size(max = 500, message = "O termo de busca deve ter no máximo 500 caracteres.")
        @Schema(description = "Busca textual aplicada à descrição da oportunidade.", example = "java")
        String termo,

        @Positive(message = "O tipo de emprego informado é inválido.")
        @Schema(description = "Filtra oportunidades pelo tipo de emprego.", example = "3")
        Long tipoDeEmpregoId,

        @Positive(message = "A empresa informada é inválida.")
        @Schema(description = "Filtra oportunidades pela empresa associada à publicação.", example = "7")
        Long empresaId,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Filtra oportunidades por país. Considera recursos com localidade validada.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Filtra oportunidades por estado. Considera recursos com localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Filtra oportunidades por cidade. Considera recursos com localidade validada.", example = "2507507")
        Long cidadeId,

        @Schema(description = "Filtra oportunidades pela modalidade.", example = "REMOTO")
        Modalidade modalidade,

        @Schema(description = "Filtra a visão interna do recrutador pelo status de localidade da oportunidade. Use VALIDADA ou PENDENTE.", example = "PENDENTE")
        StatusLocalidadeFiltro statusLocalidade
) {
}
