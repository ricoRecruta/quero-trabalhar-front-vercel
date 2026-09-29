package com.QueroTrabalhar.domain.dtos.experienciaProfissional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(name = "ExperienciaProfissionalFilterDTO", description = "Filtros disponíveis na listagem administrativa de experiências profissionais.")
public record ExperienciaProfissionalFilterDTO(
        @Size(max = 500, message = "O termo de busca deve ter no máximo 500 caracteres.")
        @Schema(description = "Busca textual aplicada à descrição da experiência e ao título do tipo de emprego associado.", example = "backend")
        String termo,

        @Positive(message = "O tipo de emprego informado é inválido.")
        @Schema(description = "Filtra experiências pelo tipo de emprego associado.", example = "3")
        Long tipoDeEmpregoId,

        @Schema(description = "Filtra experiências em andamento. Quando true, retorna registros sem dataFim; quando false, retorna registros com dataFim preenchida.", example = "true")
        Boolean emAndamento,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Retorna experiências com dataInicio maior ou igual ao valor informado.", example = "2022-01-01")
        LocalDate dataInicioDe,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Retorna experiências com dataInicio menor ou igual ao valor informado.", example = "2024-12-31")
        LocalDate dataInicioAte,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Retorna experiências com dataFim maior ou igual ao valor informado.", example = "2022-01-01")
        LocalDate dataFimDe,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Retorna experiências com dataFim menor ou igual ao valor informado.", example = "2024-12-31")
        LocalDate dataFimAte
) {
}
