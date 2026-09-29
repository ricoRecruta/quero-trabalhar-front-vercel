package com.QueroTrabalhar.domain.dtos.usuario;

import com.QueroTrabalhar.domain.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UsuarioResponseDTO", description = "Dados do usuário retornados pela API.")
public record UsuarioResponseDTO(
        @Schema(description = "Identificador do usuário.", example = "1")
        Long id,

        @Schema(description = "Nome completo do usuário.", example = "Maria da Silva")
        String nome,

        @Schema(description = "Telefone do usuário.", example = "83999999999")
        String telefone,

        @Schema(description = "E-mail do usuário.", example = "maria.silva@exemplo.com", format = "email")
        String email
) {
    public static UsuarioResponseDTO daEntidade(Usuario entidade) {
        return new UsuarioResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getTelefone(),
                entidade.getEmail()
        );
    }
}
