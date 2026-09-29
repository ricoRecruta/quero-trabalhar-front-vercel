package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalFilterDTO;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/experiencias")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administração de experiências profissionais", description = "Endpoints administrativos para consulta e manutenção de experiências profissionais. Exigem permissão ADMIN.")
public class ExperienciaProfissionalAdminController {

    private final ExperienciaProfissionalService experienciaService;

    public ExperienciaProfissionalAdminController(ExperienciaProfissionalService experienciaService) {
        this.experienciaService = experienciaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar experiências profissionais",
            description = "Lista experiências profissionais em estrutura paginada para uso administrativo. Exige permissão ADMIN. Aceita os filtros termo, tipoDeEmpregoId, emAndamento, dataInicioDe, dataInicioAte, dataFimDe e dataFimAte. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiências profissionais listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de filtro ou paginação inválidos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso administrativo.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<ExperienciaProfissionalResponseDTO>> listarTodasAsExperiencias(
            @Valid @ParameterObject ExperienciaProfissionalFilterDTO filtro,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(experienciaService.listarTodasExperienciasComoAdmin(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar experiência profissional por ID",
            description = "Retorna, como ADMIN, uma experiência profissional pelo identificador informado. Exige permissão ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiência profissional retornada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso administrativo.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Experiência profissional não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<ExperienciaProfissionalResponseDTO> buscarExperienciaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(experienciaService.buscarExperienciaPorIdComoAdmin(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar experiência profissional",
            description = "Atualiza, como ADMIN, uma experiência profissional pelo identificador informado. Exige permissão ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiência profissional atualizada com sucesso.", useReturnTypeSchema = true),
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
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso administrativo.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Experiência profissional ou tipo de emprego não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<ExperienciaProfissionalResponseDTO> atualizarExperiencia(
            @PathVariable Long id,
            @Valid @RequestBody ExperienciaProfissionalRequestDTO dto
    ) {
        return ResponseEntity.ok(experienciaService.atualizarExperienciaComoAdmin(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover experiência profissional",
            description = "Remove, como ADMIN, uma experiência profissional pelo identificador informado. Exige permissão ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Experiência profissional removida com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso administrativo.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Experiência profissional não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> deletarExperiencia(@PathVariable Long id) {
        experienciaService.deletarExperienciaComoAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
