package com.QueroTrabalhar.domain.dtos.perfilRecrutador;

import com.QueroTrabalhar.domain.dtos.empresa.EmpresaResponseDTO;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PerfilRecrutadorEmpresaResponseDTO", description = "Empresa vinculada ao recrutador no fluxo autenticado, com o respectivo status do vínculo.")
public record PerfilRecrutadorEmpresaResponseDTO(
        @Schema(description = "Dados internos da empresa vinculada, quando houver. Informações de localidade pendente aparecem apenas como apoio ao usuário autenticado.", implementation = EmpresaResponseDTO.class)
        EmpresaResponseDTO empresa,

        @Schema(description = "Status atual do vínculo entre o recrutador e a empresa.", example = "PENDENTE")
        StatusVinculoEmpresa statusVinculoEmpresa
) {
    public static PerfilRecrutadorEmpresaResponseDTO daEntidade(PerfilRecrutador entidade) {
        return daEntidade(entidade, null);
    }

    public static PerfilRecrutadorEmpresaResponseDTO daEntidade(
            PerfilRecrutador entidade,
            LocalidadePendente localidadePendente
    ) {
        return new PerfilRecrutadorEmpresaResponseDTO(
                entidade.getEmpresaVinculada() != null
                        ? EmpresaResponseDTO.daEntidade(entidade.getEmpresaVinculada(), localidadePendente)
                        : null,
                entidade.getStatusVinculoEmpresa()
        );
    }
}
