package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalRequestDTO;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalResponseDTO;
import com.QueroTrabalhar.services.ExperienciaProfissionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/usuarios/me/perfil-candidato/experiencias")
@Tag(name = "Experiências profissionais", description = "Endpoints autenticados para gestão das experiências profissionais do perfil de candidato do usuário autenticado.")
public class ExperienciaProfissionalController {

    private final ExperienciaProfissionalService experienciaProfissionalService;

    public ExperienciaProfissionalController(
            ExperienciaProfissionalService experienciaProfissionalService
    ) {
        this.experienciaProfissionalService = experienciaProfissionalService;
    }

    @GetMapping
    @Operation(
            summary = "Listar minhas experiências profissionais",
            description = "Lista as experiências profissionais do perfil de candidato do usuário autenticado. Opera sobre o recurso /me."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiências profissionais listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada para acessar o perfil de candidato autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<List<ExperienciaProfissionalResponseDTO>> listarExperienciasProfissionais() {
        return ResponseEntity.ok(experienciaProfissionalService.listarTodasAsExperienciaProfissionais());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar minha experiência profissional por ID",
            description = "Retorna uma experiência profissional do perfil de candidato do usuário autenticado pelo identificador informado. Opera sobre o recurso /me."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiência profissional retornada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Experiência profissional não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao consultar a experiência profissional.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<ExperienciaProfissionalResponseDTO> buscarMinhaExperienciaProfissionalPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(experienciaProfissionalService.buscarMinhaExperienciaPorId(id));
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar experiência profissional",
            description = "Cadastra uma nova experiência profissional no perfil de candidato do usuário autenticado. Opera sobre o recurso /me."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Experiência profissional cadastrada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido.",
                    content = @Content(schema = @Schema(oneOf = {StandardError.class, ValidationError.class}))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tipo de emprego não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao cadastrar a experiência profissional.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<ExperienciaProfissionalResponseDTO> cadastrarExperienciaProfissional(
            @Valid @RequestBody ExperienciaProfissionalRequestDTO experienciaProfissional
    ) {
        ExperienciaProfissionalResponseDTO experienciaCriada =
                experienciaProfissionalService.adicionarMinhaExperienciaProfissional(experienciaProfissional);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(experienciaCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(experienciaCriada);
    }

    @DeleteMapping("/{experienciaId}")
    @Operation(
            summary = "Remover minha experiência profissional",
            description = "Remove uma experiência profissional do perfil de candidato do usuário autenticado pelo identificador informado. Opera sobre o recurso /me."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Experiência profissional removida com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Experiência profissional não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao remover a experiência profissional.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> deletarMinhaExperienciaProfissional(@PathVariable Long experienciaId) {
        experienciaProfissionalService.deletarMinhaExperiencia(experienciaId);
        return ResponseEntity.noContent().build();
    }
}
