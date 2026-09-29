package com.QueroTrabalhar.domain.dtos.experienciaProfissional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(name = "ExperienciaProfissionalRequestDTO", description = "Dados para cadastro ou atualização de experiência profissional do candidato.")
public record ExperienciaProfissionalRequestDTO(
        @NotNull
        @Schema(description = "Identificador do tipo de emprego associado à experiência profissional.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Long tipoDeEmpregoId,

        @NotBlank
        @Schema(description = "Descrição da experiência profissional.", example = "Desenvolvimento e manutenção de APIs REST com Java e Spring Boot.", requiredMode = Schema.RequiredMode.REQUIRED)
        String descricao,

        @NotNull
        @Schema(description = "Data de início da experiência profissional.", example = "2022-01-10", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate dataInicio,

        @Schema(description = "Data de término da experiência profissional. Pode ser nula quando a experiência estiver em andamento.", example = "2024-03-31")
        LocalDate dataFim
) {}
