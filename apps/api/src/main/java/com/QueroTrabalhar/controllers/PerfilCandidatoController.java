package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeInteresseCandidatoFilterDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.services.PerfilCandidatoService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidatos")
@Tag(name = "Perfil do candidato", description = "Endpoints autenticados para operações do perfil de candidato e gestão das vagas de interesse do usuário autenticado.")
public class PerfilCandidatoController {

    private final PerfilCandidatoService candidatoService;

    public PerfilCandidatoController(PerfilCandidatoService candidatoService) {
        this.candidatoService = candidatoService;
    }

    @PostMapping("/me/interesses/vagas/{vagaId}")
    @Operation(
            summary = "Demonstrar interesse em uma vaga",
            description = "Registra interesse do usuário autenticado com perfil de candidato em uma oportunidade pelo identificador informado. Opera sobre o recurso /me e não permite registrar interesse duplicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Interesse registrado com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Oportunidade de emprego não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao registrar interesse na vaga.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> demonstrarInteresse(@PathVariable Long vagaId) {
        candidatoService.demonstrarInteresseEmVaga(vagaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/interesses/vagas/{vagaId}")
    @Operation(
            summary = "Remover interesse em uma vaga",
            description = "Remove o interesse previamente registrado pelo usuário autenticado com perfil de candidato em uma oportunidade. Opera sobre o recurso /me."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Interesse removido com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Oportunidade de emprego não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao remover o interesse da vaga.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerInteresse(@PathVariable Long vagaId) {
        candidatoService.removerInteresseEmVaga(vagaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/interesses/vagas")
    @Operation(
            summary = "Listar minhas vagas de interesse",
            description = "Lista, em estrutura paginada, as oportunidades marcadas como interesse pelo usuário autenticado com perfil de candidato. Aceita os filtros termo, tipoDeEmpregoId, empresaId, recrutadorId, paisId, estadoId, cidadeId, modalidade e statusLocalidade. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. Os filtros de localidade por IDs se aplicam às vagas com localidade validada, enquanto statusLocalidade permite separar a visão interna do candidato entre VALIDADA e PENDENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vagas de interesse listadas com sucesso.", useReturnTypeSchema = true),
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
                    responseCode = "422",
                    description = "Regra de negócio violada para acessar o perfil de candidato autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<OportunidadeDeEmpregoResponseDTO>> listarMinhasVagasDeInteresse(
            @Valid @ParameterObject OportunidadeInteresseCandidatoFilterDTO filtro,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(candidatoService.listarMinhasVagasDeInteresse(filtro, pageable));
    }
}
