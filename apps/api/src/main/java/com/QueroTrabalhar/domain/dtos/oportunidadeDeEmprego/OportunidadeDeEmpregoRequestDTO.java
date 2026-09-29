package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.enums.Modalidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Contrato de entrada para criação e atualização de oportunidades pelo recrutador autenticado.
 *
 * <p>O DTO não expõe {@code recrutadorId} porque a autoria da vaga é definida pelo contexto autenticado do endpoint.
 * A localidade segue a mesma regra do módulo de empresa: IDs estruturados têm precedência sobre texto livre. O campo
 * {@code publicarComoEmpresa} apenas solicita o contexto desejado; a associação final depende das validações de
 * vínculo do recrutador com a empresa.</p>
 */
@Schema(name = "OportunidadeDeEmpregoRequestDTO", description = "Dados para criação ou atualização de oportunidade de emprego em fluxo autenticado de recrutador.")
public record OportunidadeDeEmpregoRequestDTO(
        @NotBlank(message = "A descrição da oportunidade é obrigatória.")
        @Size(max = 500, message = "A descrição da oportunidade deve ter no máximo 500 caracteres.")
        @Schema(description = "Descrição pública da oportunidade.", example = "Desenvolvedor Backend Java 21 com Spring Boot.", requiredMode = Schema.RequiredMode.REQUIRED)
        String descricao,

        @NotNull(message = "O tipo de emprego é obrigatório.")
        @Positive(message = "O tipo de emprego informado é inválido.")
        @Schema(description = "Identificador do tipo de emprego. Apenas tipos aprovados podem ser usados.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Long tipoDeEmpregoId,

        @NotNull(message = "A modalidade é obrigatória.")
        @Schema(description = "Modalidade da oportunidade.", example = "REMOTO", requiredMode = Schema.RequiredMode.REQUIRED)
        Modalidade modalidade,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Identificador do país da localidade validada. Quando qualquer ID de localidade é informado, os IDs estruturados têm prioridade sobre localidadeTexto.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Identificador do estado da localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Identificador da cidade da localidade validada.", example = "2507507")
        Long cidadeId,

        @Size(max = 255, message = "O texto da localidade deve ter no máximo 255 caracteres.")
        @Schema(description = "Texto livre da localidade. Use quando não houver IDs estruturados. Se a resolução automática não validar a localidade, a oportunidade ficará pendente no fluxo interno e não aparecerá nos endpoints públicos. A confirmação manual da sugestão ainda não está implementada no MVP.", example = "João Pessoa, PB")
        String localidadeTexto,

        @Schema(description = "Quando true, tenta publicar a oportunidade em nome da empresa vinculada ao recrutador autenticado. Exige vínculo aprovado. Quando false ou nulo, a oportunidade permanece pessoal.", example = "true")
        Boolean publicarComoEmpresa
) {
}
