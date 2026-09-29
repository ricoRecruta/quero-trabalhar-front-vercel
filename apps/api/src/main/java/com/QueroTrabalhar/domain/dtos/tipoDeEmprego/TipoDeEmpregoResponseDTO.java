package com.QueroTrabalhar.domain.dtos.tipoDeEmprego;

import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TipoDeEmpregoResponseDTO", description = "Dados de um tipo de emprego retornados pela API.")
public record TipoDeEmpregoResponseDTO(
        @Schema(description = "Identificador do tipo de emprego.", example = "3")
        Long id,

        @Schema(description = "Título do tipo de emprego.", example = "Desenvolvedor Backend")
        String titulo,

        @Schema(description = "Descrição do tipo de emprego.", example = "Atuação com APIs, microsserviços e banco de dados.")
        String descricao,

        @Schema(description = "Indica se o tipo de emprego já faz parte do catálogo aprovado.", example = "true")
        boolean aprovado
) {
    public static TipoDeEmpregoResponseDTO daEntidade(TipoDeEmprego entidade) {
        return new TipoDeEmpregoResponseDTO(
                entidade.getId(),
                entidade.getTitulo(),
                entidade.getDescricao(),
                entidade.isAprovado()
        );
    }
}
