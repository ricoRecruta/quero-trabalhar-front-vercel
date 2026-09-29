package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoResponseDTO;
import com.QueroTrabalhar.services.TipoDeEmpregoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tipos-de-emprego")
@Tag(
        name = "Tipos de emprego",
        description = "Endpoints para consulta do catálogo de tipos de emprego aprovados e para envio de sugestões para moderação administrativa."
)
public class TipoDeEmpregoController {

    private final TipoDeEmpregoService tipoDeEmpregoService;

    public TipoDeEmpregoController(TipoDeEmpregoService tipoDeEmpregoService) {
        this.tipoDeEmpregoService = tipoDeEmpregoService;
    }

    @GetMapping("/aprovados")
    @Operation(
            summary = "Listar tipos de emprego aprovados",
            description = "Retorna o catálogo público de tipos de emprego aprovados. A resposta segue o contrato atual de lista simples, sem paginação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos de emprego aprovados listados com sucesso.", useReturnTypeSchema = true)
    })
    public ResponseEntity<List<TipoDeEmpregoResponseDTO>> listarAprovados() {
        return ResponseEntity.ok(tipoDeEmpregoService.listarAprovados());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar tipo de emprego aprovado por ID",
            description = "Retorna um item do catálogo público de tipos de emprego aprovados pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de emprego aprovado retornado com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tipo de emprego aprovado não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<TipoDeEmpregoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador do tipo de emprego aprovado.", example = "3", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(tipoDeEmpregoService.buscarAprovadoPorId(id));
    }

    @PostMapping("/sugerir")
    @Operation(
            summary = "Sugerir tipo de emprego para o catálogo",
            description = "Cria uma sugestão de tipo de emprego ainda não aprovado para a fila de moderação administrativa. No comportamento atual da API, este endpoint exige autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sugestão de tipo de emprego criada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido ou título já existente no catálogo.",
                    content = @Content(schema = @Schema(oneOf = {StandardError.class, ValidationError.class}))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<TipoDeEmpregoResponseDTO> sugerirNoCatalogo(
            @Valid @RequestBody TipoDeEmpregoRequestDTO tipoDeEmprego
    ) {
        TipoDeEmpregoResponseDTO tipoDeEmpregoSugerido = tipoDeEmpregoService.sugerirNoCatalogo(tipoDeEmprego);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tipoDeEmpregoSugerido.id())
                .toUri();

        return ResponseEntity.created(uri).body(tipoDeEmpregoSugerido);
    }
}
