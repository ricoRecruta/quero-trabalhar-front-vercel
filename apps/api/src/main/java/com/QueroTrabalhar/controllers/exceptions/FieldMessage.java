package com.QueroTrabalhar.controllers.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FieldMessage", description = "Detalhe de validação para um campo específico do payload.")
public record FieldMessage (
        @Schema(description = "Nome do campo com erro.", example = "email")
        String fieldName,

        @Schema(description = "Mensagem de validação retornada para o campo.", example = "Formato de e-mail inválido.")
        String message
) {}
