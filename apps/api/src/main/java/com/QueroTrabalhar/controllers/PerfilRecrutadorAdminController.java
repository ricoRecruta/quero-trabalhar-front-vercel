package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorEmpresaResponseDTO;
import com.QueroTrabalhar.services.PerfilRecrutadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/recrutadores")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administração de vínculos de recrutador", description = "Endpoints administrativos para análise de vínculos entre recrutadores e empresas. Exigem permissão ADMIN.")
public class PerfilRecrutadorAdminController {

    private final PerfilRecrutadorService perfilRecrutadorService;

    public PerfilRecrutadorAdminController(PerfilRecrutadorService perfilRecrutadorService) {
        this.perfilRecrutadorService = perfilRecrutadorService;
    }

    @PatchMapping("/{recrutadorId}/empresa/aprovar")
    @Operation(
            summary = "Aprovar vínculo de recrutador com empresa",
            description = "Aprova, como ADMIN, um vínculo pendente entre recrutador e empresa. O endpoint exige permissão ADMIN e opera apenas sobre solicitações ainda pendentes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo aprovado com sucesso.", useReturnTypeSchema = true),
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
                    description = "Perfil de recrutador não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao aprovar o vínculo.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<PerfilRecrutadorEmpresaResponseDTO> aprovarVinculoEmpresa(
            @PathVariable Long recrutadorId
    ) {
        return ResponseEntity.ok(perfilRecrutadorService.aprovarVinculoEmpresaComoAdmin(recrutadorId));
    }

    @PatchMapping("/{recrutadorId}/empresa/recusar")
    @Operation(
            summary = "Recusar vínculo de recrutador com empresa",
            description = "Recusa, como ADMIN, um vínculo pendente entre recrutador e empresa. O endpoint exige permissão ADMIN e opera apenas sobre solicitações ainda pendentes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo recusado com sucesso.", useReturnTypeSchema = true),
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
                    description = "Perfil de recrutador não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao recusar o vínculo.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<PerfilRecrutadorEmpresaResponseDTO> recusarVinculoEmpresa(
            @PathVariable Long recrutadorId
    ) {
        return ResponseEntity.ok(perfilRecrutadorService.recusarVinculoEmpresaComoAdmin(recrutadorId));
    }
}
