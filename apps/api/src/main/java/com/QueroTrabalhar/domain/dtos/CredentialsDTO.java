package com.QueroTrabalhar.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CredentialsDTO", description = "Credenciais usadas na autenticação do usuário.")
public record CredentialsDTO(
        @Schema(
                description = "E-mail do usuário.",
                example = "usuario@exemplo.com",
                format = "email",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "Senha do usuário.",
                example = "Senha@123",
                format = "password",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String password
) {
}
