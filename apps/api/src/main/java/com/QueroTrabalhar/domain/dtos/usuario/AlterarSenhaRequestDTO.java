package com.QueroTrabalhar.domain.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AlterarSenhaRequestDTO", description = "Dados para alteração da senha do usuário autenticado.")
public record AlterarSenhaRequestDTO(

        @NotBlank(message = "A senha atual é obrigatória.")
        @Schema(description = "Senha atual do usuário.", example = "SenhaAtual@123", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres.")
        @Schema(description = "Nova senha do usuário.", example = "NovaSenha@123", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String novaSenha,

        @NotBlank(message = "A confirmação da nova senha é obrigatória.")
        @Schema(description = "Confirmação da nova senha.", example = "NovaSenha@123", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String confirmacaoNovaSenha
) {}
