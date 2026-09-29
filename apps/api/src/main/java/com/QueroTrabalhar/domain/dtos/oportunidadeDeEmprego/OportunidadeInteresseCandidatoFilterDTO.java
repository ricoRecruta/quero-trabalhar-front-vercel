package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusLocalidadeFiltro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Agrupa os filtros da visão interna das vagas marcadas como interesse pelo candidato autenticado.
 *
 * <p>Este DTO pode combinar filtros tradicionais com {@code statusLocalidade} para separar vagas já validadas das que
 * ainda dependem de resolução formal de localidade.</p>
 */
@Schema(name = "OportunidadeInteresseCandidatoFilterDTO", description = "Filtros disponíveis na listagem paginada das vagas de interesse do candidato autenticado.")
public record OportunidadeInteresseCandidatoFilterDTO(
        @Size(max = 500, message = "O termo de busca deve ter no máximo 500 caracteres.")
        @Schema(description = "Busca textual aplicada à descrição da oportunidade.", example = "spring boot")
        String termo,

        @Positive(message = "O tipo de emprego informado é inválido.")
        @Schema(description = "Filtra vagas de interesse pelo tipo de emprego.", example = "3")
        Long tipoDeEmpregoId,

        @Positive(message = "A empresa informada é inválida.")
        @Schema(description = "Filtra vagas de interesse pela empresa associada à publicação.", example = "7")
        Long empresaId,

        @Positive(message = "O recrutador informado é inválido.")
        @Schema(description = "Filtra vagas de interesse pelo recrutador responsável.", example = "12")
        Long recrutadorId,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Filtra vagas de interesse por país. Considera recursos com localidade validada.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Filtra vagas de interesse por estado. Considera recursos com localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Filtra vagas de interesse por cidade. Considera recursos com localidade validada.", example = "2507507")
        Long cidadeId,

        @Schema(description = "Filtra vagas de interesse pela modalidade.", example = "REMOTO")
        Modalidade modalidade,

        @Schema(description = "Filtra a visão interna do candidato pelo status de localidade da oportunidade. Use VALIDADA ou PENDENTE.", example = "VALIDADA")
        StatusLocalidadeFiltro statusLocalidade
) {
}
