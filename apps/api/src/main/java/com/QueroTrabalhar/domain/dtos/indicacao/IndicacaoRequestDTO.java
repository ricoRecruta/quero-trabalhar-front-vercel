package com.QueroTrabalhar.domain.dtos.indicacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(name = "IndicacaoRequestDTO", description = "Dados para criação de uma indicação pelo usuário autenticado.")
public record IndicacaoRequestDTO(
        @NotNull(message = "O usuário indicado é obrigatório.")
        @Positive(message = "O usuário indicado informado é inválido.")
        @Schema(description = "Identificador do usuário que receberá a indicação.", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long usuarioIndicadoId,

        @NotBlank(message = "A mensagem é obrigatória.")
        @Size(max = 500, message = "A mensagem deve ter no máximo 500 caracteres.")
        @Schema(description = "Mensagem da indicação.", example = "Profissional com excelente domínio de Java e Spring Boot.", requiredMode = Schema.RequiredMode.REQUIRED)
        String mensagem
) {
}
