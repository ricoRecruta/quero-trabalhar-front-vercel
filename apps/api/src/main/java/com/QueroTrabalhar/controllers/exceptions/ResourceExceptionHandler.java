package com.QueroTrabalhar.controllers.exceptions;


import com.QueroTrabalhar.services.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> objectNotFoundException(
            ObjectNotFoundException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<StandardError> duplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandardError> businessRuleException(
            BusinessRuleException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Regra de negócio violada",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> dataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Violação de dados",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> validationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ValidationError errors = new ValidationError(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro na validação dos campos",
                "Campos inválidos",
                request.getRequestURI()
        );

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardError> constraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro na validação dos parâmetros",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> illegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro inválido",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<StandardError> resourceInUseException(
            ResourceInUseException ex,
            HttpServletRequest request
    ) {
        StandardError error = new StandardError(
                System.currentTimeMillis(),
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
