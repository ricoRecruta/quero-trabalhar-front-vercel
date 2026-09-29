package com.QueroTrabalhar.controllers.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StandardError", description = "Modelo padrão de erro retornado pela API.")
public class StandardError  {
    @Schema(description = "Momento em que o erro ocorreu, em milissegundos desde epoch.", example = "1746727687000")
    private Long timestamp;

    @Schema(description = "Código HTTP da resposta.", example = "401")
    private Integer status;

    @Schema(description = "Resumo do tipo de erro.", example = "Não autorizado")
    private String error;

    @Schema(description = "Mensagem detalhando o erro retornado pela API.", example = "E-mail ou senha inválidos.")
    private String message;

    @Schema(description = "Caminho da requisição que gerou o erro.", example = "/login")
    private String path;

    public StandardError() {
    }

    public StandardError(Long timestamp, Integer status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
