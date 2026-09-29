package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeRecrutadorMeFilterDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorEmpresaResponseDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorResponseDTO;
import com.QueroTrabalhar.services.OportunidadeDeEmpregoService;
import com.QueroTrabalhar.services.PerfilRecrutadorService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recrutadores")
@Tag(name = "Perfil do recrutador", description = "Endpoints autenticados para consulta do perfil do recrutador, empresa vinculada e oportunidades do usuário autenticado.")
public class PerfilRecrutadorController {

    private final PerfilRecrutadorService recrutadorService;
    private final OportunidadeDeEmpregoService oportunidadeDeEmpregoService;

    public PerfilRecrutadorController(
            PerfilRecrutadorService recrutadorService,
            OportunidadeDeEmpregoService oportunidadeDeEmpregoService
    ) {
        this.recrutadorService = recrutadorService;
        this.oportunidadeDeEmpregoService = oportunidadeDeEmpregoService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Buscar meu perfil de recrutador",
            description = "Retorna os dados do perfil de recrutador do usuário autenticado. Opera sobre o recurso /me."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil de recrutador retornado com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada para acessar o perfil de recrutador autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<PerfilRecrutadorResponseDTO> buscarMeuPerfil() {
        return ResponseEntity.ok(recrutadorService.buscarMeuPerfil());
    }

    @GetMapping("/me/oportunidades")
    @Operation(
            summary = "Listar minhas oportunidades",
            description = "Lista, em estrutura paginada, as oportunidades publicadas pelo usuário autenticado com perfil de recrutador. Aceita os filtros termo, tipoDeEmpregoId, empresaId, paisId, estadoId, cidadeId, modalidade e statusLocalidade. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. O filtro statusLocalidade permite separar a visão interna do recrutador entre oportunidades com localidade VALIDADA e PENDENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oportunidades listadas com sucesso.", useReturnTypeSchema = true),
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
                    description = "Regra de negócio violada para acessar o perfil de recrutador autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<OportunidadeDeEmpregoResponseDTO>> listarMinhasOportunidades(
            @Valid @ParameterObject OportunidadeRecrutadorMeFilterDTO filtro,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                oportunidadeDeEmpregoService.listarOportunidadesDoRecrutadorAutenticado(filtro, pageable)
        );
    }

    @PostMapping("/me/empresa/{empresaId}/solicitar-vinculo")
    @Operation(
            summary = "Solicitar vínculo com empresa",
            description = "Solicita o vínculo do recrutador autenticado com a empresa informada por ID. A solicitação depende de análise administrativa posterior e não representa ownership formal da empresa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação de vínculo registrada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao solicitar vínculo com a empresa.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<PerfilRecrutadorEmpresaResponseDTO> solicitarVinculoEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(recrutadorService.solicitarVinculoEmpresa(empresaId));
    }

    @GetMapping("/me/empresa")
    @Operation(
            summary = "Buscar minha empresa vinculada",
            description = "Retorna a empresa atualmente vinculada ao recrutador autenticado e o status do vínculo. Opera sobre o recurso /me e expõe informações internas de localidade apenas como apoio ao próprio usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa vinculada retornada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao consultar a empresa vinculada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<PerfilRecrutadorEmpresaResponseDTO> buscarMinhaEmpresa() {
        return ResponseEntity.ok(recrutadorService.buscarMinhaEmpresa());
    }
}
