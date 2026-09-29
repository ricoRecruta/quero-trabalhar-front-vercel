package com.QueroTrabalhar.domain.dtos.experienciaProfissional;

import com.QueroTrabalhar.domain.entity.ExperienciaProfissional;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "ExperienciaProfissionalResponseDTO", description = "Dados de uma experiência profissional retornados pela API.")
public record ExperienciaProfissionalResponseDTO(
        @Schema(description = "Identificador da experiência profissional.", example = "15")
        Long id,

        @Schema(description = "Identificador do tipo de emprego associado à experiência profissional.", example = "3")
        Long tipoDeEmprego,

        @Schema(description = "Descrição da experiência profissional.", example = "Desenvolvimento e manutenção de APIs REST com Java e Spring Boot.")
        String descricao,

        @Schema(description = "Data de início da experiência profissional.", example = "2022-01-10")
        LocalDate dataInicio,

        @Schema(description = "Data de término da experiência profissional. Pode ser nula quando a experiência estiver em andamento.", example = "2024-03-31")
        LocalDate dataFim
) {
    public static ExperienciaProfissionalResponseDTO daEntidade(ExperienciaProfissional entidade) {
        return new ExperienciaProfissionalResponseDTO(
                entidade.getId(),
                entidade.getTipoDeEmprego().getId(),
                entidade.getDescricao(),
                entidade.getDataInicio(),
                entidade.getDataFim()
        );
    }
}
