package com.QueroTrabalhar.domain.dtos.perfilRecrutador;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "PerfilRecrutadorUsuarioRequestDTO",
        description = "Dados para adicionar ou atualizar o perfil de recrutador de um usuário, tanto em fluxo administrativo quanto no fluxo do próprio usuário autenticado."
)
public record PerfilRecrutadorUsuarioRequestDTO(
        @NotBlank(message = "O nome da empresa é obrigatório.")
        @Size(max = 100, message = "O nome da empresa deve ter no máximo 100 caracteres.")
        @Schema(description = "Nome legado da empresa informado na criação ou atualização do perfil de recrutador.", example = "Quero Trabalhar", requiredMode = Schema.RequiredMode.REQUIRED)
        String nomeDaEmpresa
) {
}
