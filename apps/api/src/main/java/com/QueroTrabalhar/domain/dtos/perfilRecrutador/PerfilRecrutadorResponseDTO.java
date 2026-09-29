package com.QueroTrabalhar.domain.dtos.perfilRecrutador;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PerfilRecrutadorResponseDTO", description = "Dados do perfil de recrutador retornados no fluxo autenticado do usuário.")
public record PerfilRecrutadorResponseDTO(
        @Schema(description = "Identificador do perfil de recrutador.", example = "12")
        Long id,

        @Schema(description = "Nome do usuário dono do perfil de recrutador.", example = "Marina Lima")
        String nome,

        @Schema(description = "Nome legado da empresa informado no cadastro do perfil. Campo mantido por compatibilidade e não representa vínculo aprovado com empresa.", example = "Empresa Legada A")
        String empresaLegada,

        @Schema(description = "Identificador da empresa atualmente vinculada ao recrutador, quando houver.", example = "7")
        Long empresaVinculadaId,

        @Schema(description = "Nome da empresa atualmente vinculada ao recrutador, quando houver.", example = "Quero Trabalhar")
        String empresaVinculadaNome,

        @Schema(description = "Status atual do vínculo do recrutador com a empresa vinculada, quando houver.", example = "PENDENTE")
        StatusVinculoEmpresa statusVinculoEmpresa
) {
    public static PerfilRecrutadorResponseDTO daEntidade(PerfilRecrutador entidade) {
        Usuario usuario = entidade.getUsuario();
        Empresa empresaVinculada = entidade.getEmpresaVinculada();

        return new PerfilRecrutadorResponseDTO(
                entidade.getId(),
                usuario != null ? usuario.getNome() : null,
                entidade.getEmpresa(),
                empresaVinculada != null ? empresaVinculada.getId() : null,
                empresaVinculada != null ? empresaVinculada.getNome() : null,
                entidade.getStatusVinculoEmpresa()
        );
    }
}
