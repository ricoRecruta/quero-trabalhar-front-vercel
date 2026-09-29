package com.QueroTrabalhar.domain.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(
        name = "UsuarioFilterDTO",
        description = "Filtros usados como parâmetros de query na listagem administrativa de usuários."
)
public record UsuarioFilterDTO(
        @Size(max = 150, message = "O termo de busca deve ter no máximo 150 caracteres.")
        @Schema(description = "Busca textual aplicada a nome, e-mail ou CPF.", example = "ana")
        String termo,

        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        @Schema(description = "Filtra usuários pelo nome.", example = "Maria")
        String nome,

        @Size(max = 255, message = "O email deve ter no máximo 255 caracteres.")
        @Schema(description = "Filtra usuários pelo e-mail.", example = "maria@exemplo.com", format = "email")
        String email,

        @Size(max = 14, message = "O CPF deve ter no máximo 14 caracteres.")
        @Schema(description = "Filtra usuários pelo CPF.", example = "12345678901")
        String cpf,

        @Schema(description = "Filtra usuários que possuem ou não perfil de candidato.", example = "true")
        Boolean temPerfilCandidato,

        @Schema(description = "Filtra usuários que possuem ou não perfil de recrutador.", example = "false")
        Boolean temPerfilRecrutador
) {
}
