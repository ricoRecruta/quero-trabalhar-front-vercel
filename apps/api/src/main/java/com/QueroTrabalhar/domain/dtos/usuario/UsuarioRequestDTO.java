package com.QueroTrabalhar.domain.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "UsuarioRequestDTO", description = "Dados para cadastro de um novo usuário.")
public record UsuarioRequestDTO(

        @NotBlank(message = "CPF é obrigatório")
        @Schema(description = "CPF do usuário.", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
        String cpf,

        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo do usuário.", example = "Maria da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,

        @NotBlank(message = "Telefone é obrigatório")
        @Schema(description = "Telefone do usuário.", example = "83999999999", requiredMode = Schema.RequiredMode.REQUIRED)
        String telefone,

        @NotBlank(message = "Email é obrigatório")
        @Schema(description = "E-mail do usuário.", example = "maria.silva@exemplo.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha de acesso do usuário.", example = "Senha@123", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String senha
) {}
