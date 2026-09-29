package com.QueroTrabalhar.domain.dtos.tipoDeEmprego;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "TipoDeEmpregoRequestDTO", description = "Dados para criação administrativa ou sugestão de tipo de emprego.")
public record TipoDeEmpregoRequestDTO(
        @NotBlank(message = "O título não pode ser vazio")
        @Schema(description = "Título do tipo de emprego.", example = "Desenvolvedor Backend", requiredMode = Schema.RequiredMode.REQUIRED)
        String titulo,

        @NotBlank(message = "O tipo de emprego deve ter uma descrição")
        @Schema(description = "Descrição do tipo de emprego.", example = "Atuação com APIs, microsserviços e banco de dados.", requiredMode = Schema.RequiredMode.REQUIRED)
        String descricao
) {}
