package com.QueroTrabalhar.domain.dtos.perfilRecrutador;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RecrutadorDaEmpresaResponseDTO", description = "Dados públicos de um recrutador aprovado vinculado a uma empresa.")
public record RecrutadorDaEmpresaResponseDTO(
        @Schema(description = "Identificador do recrutador.", example = "12")
        Long recrutadorId,

        @Schema(description = "Nome do recrutador.", example = "Marina Lima")
        String nome,

        @Schema(description = "Identificador da empresa vinculada.", example = "7")
        Long empresaId,

        @Schema(description = "Nome da empresa vinculada.", example = "Quero Trabalhar")
        String empresaNome,

        @Schema(description = "Status atual do vínculo do recrutador com a empresa.", example = "APROVADO")
        StatusVinculoEmpresa statusVinculoEmpresa
) {
    public static RecrutadorDaEmpresaResponseDTO daEntidade(PerfilRecrutador entidade) {
        Usuario usuario = entidade.getUsuario();
        Empresa empresa = entidade.getEmpresaVinculada();

        return new RecrutadorDaEmpresaResponseDTO(
                entidade.getId(),
                usuario != null ? usuario.getNome() : null,
                empresa != null ? empresa.getId() : null,
                empresa != null ? empresa.getNome() : null,
                entidade.getStatusVinculoEmpresa()
        );
    }
}
