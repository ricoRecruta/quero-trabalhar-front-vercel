package com.QueroTrabalhar.domain.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UsuarioAtualizacaoRequestDTO", description = "Dados para atualização cadastral de usuário.")
public record UsuarioAtualizacaoRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Schema(description = "Nome completo do usuário.", example = "Maria da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,

        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres.")
        @Schema(description = "Telefone do usuário.", example = "83999999999", requiredMode = Schema.RequiredMode.REQUIRED)
        String telefone,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Schema(description = "E-mail do usuário.", example = "maria.silva@exemplo.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {}
