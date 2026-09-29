package com.QueroTrabalhar.controllers.exceptions;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "ValidationError", description = "Modelo de erro de validação com detalhes por campo.")
public class ValidationError extends StandardError{

    @ArraySchema(
            arraySchema = @Schema(description = "Lista de erros de validação por campo."),
            schema = @Schema(implementation = FieldMessage.class)
    )
    private List<FieldMessage> erros = new ArrayList<>();

    public ValidationError(Long timestamp, Integer status, String error, String message, String path) {
        super(timestamp, status, error, message, path);
    }

    public void addError(String fieldName, String message) {
        this.erros.add(new FieldMessage(fieldName, message));
    }

    public List<FieldMessage> getErros() {
        return erros;
    }
}
