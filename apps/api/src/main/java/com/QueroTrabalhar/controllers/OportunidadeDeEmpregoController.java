package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.services.OportunidadeDeEmpregoService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Expõe os endpoints públicos e autenticados do núcleo de oportunidades do MVP.
 *
 * <p>A camada HTTP mantém a mesma separação de estados adotada no domínio: a API pública retorna apenas
 * oportunidades com localidade validada, enquanto os fluxos autenticados usam respostas internas para informar
 * pendências e contexto de publicação ao dono do recurso.</p>
 */
@RestController
@RequestMapping("/api/oportunidades")
@Tag(name = "Oportunidades de emprego", description = "Endpoints públicos e autenticados para cadastro, consulta e gestão de oportunidades de emprego.")
public class OportunidadeDeEmpregoController {

    private final OportunidadeDeEmpregoService oportunidadeDeEmpregoService;

    public OportunidadeDeEmpregoController(OportunidadeDeEmpregoService oportunidadeDeEmpregoService) {
        this.oportunidadeDeEmpregoService = oportunidadeDeEmpregoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar oportunidades públicas",
            description = "Lista oportunidades públicas com paginação e filtros. Aceita os filtros termo, tipoDeEmpregoId, empresaId, recrutadorId, paisId, estadoId, cidadeId e modalidade. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. Os filtros de localidade consideram apenas oportunidades com localidade validada; oportunidades com localidade pendente ou sem localidade validada não aparecem neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oportunidades listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de filtro ou paginação inválidos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<OportunidadeDeEmpregoPublicaResponseDTO>> listar(
            @Valid @ParameterObject OportunidadeDeEmpregoFilterDTO filtro,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(oportunidadeDeEmpregoService.listarOportunidadesDeEmprego(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar oportunidade pública por ID",
            description = "Retorna os dados públicos de uma oportunidade com localidade validada. Oportunidades com localidade pendente ou sem localidade validada não ficam disponíveis neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oportunidade retornada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "404",
                    description = "Oportunidade não encontrada ou indisponível nos endpoints públicos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<OportunidadeDeEmpregoPublicaResponseDTO> buscarOportunidadeDeEmpregoPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(oportunidadeDeEmpregoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(
            summary = "Criar oportunidade de emprego",
            description = "Cria uma oportunidade para o recrutador autenticado. A localidade pode ser informada por IDs estruturados ou por texto livre. Se a localidade ficar pendente, a resposta interna retorna os campos de status para o dono do recurso, mas a oportunidade não aparece nos endpoints públicos até possuir localidade validada. Quando publicarComoEmpresa=true, a publicação ocorre em nome da empresa vinculada ao recrutador somente se o vínculo estiver aprovado. A confirmação manual da sugestão de localidade ainda não está implementada no MVP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oportunidade cadastrada com sucesso.", useReturnTypeSchema = true),
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
                    description = "Oportunidade relacionada a tipo de emprego, país, estado ou cidade não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao cadastrar a oportunidade.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<OportunidadeDeEmpregoResponseDTO> salvarOportunidadeDeEmprego(
            @RequestBody @Valid OportunidadeDeEmpregoRequestDTO oportunidadeDeEmpregoRequestDTO
    ) {
        OportunidadeDeEmpregoResponseDTO oportunidadeCriada =
                oportunidadeDeEmpregoService.criarOportunidadeDeEmprego(oportunidadeDeEmpregoRequestDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(oportunidadeCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(oportunidadeCriada);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar oportunidade de emprego",
            description = "Atualiza uma oportunidade do recrutador autenticado. Nesta subfase, o contexto original de publicação é preservado: o payload não converte a oportunidade entre publicação pessoal e publicação em nome de empresa. A localidade pode ser atualizada por IDs estruturados ou por texto livre; se ficar pendente, a resposta interna retorna os campos de status para o dono do recurso e a oportunidade deixa de aparecer nos endpoints públicos até possuir localidade validada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oportunidade atualizada com sucesso.", useReturnTypeSchema = true),
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
                    description = "Oportunidade, tipo de emprego, país, estado ou cidade não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao atualizar a oportunidade.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<OportunidadeDeEmpregoResponseDTO> atualizarOportunidadeDeEmprego(
            @PathVariable Long id,
            @RequestBody @Valid OportunidadeDeEmpregoRequestDTO oportunidadeDeEmpregoRequestDTO
    ) {
        return ResponseEntity.ok(
                oportunidadeDeEmpregoService.atualizarOportunidadeDeEmprego(id, oportunidadeDeEmpregoRequestDTO)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover oportunidade de emprego",
            description = "Remove uma oportunidade do recrutador autenticado. Somente o recrutador dono do recurso pode excluir a oportunidade no comportamento atual do MVP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Oportunidade removida com sucesso."),
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
                    description = "Regra de negócio violada ao remover a oportunidade.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerOportunidadeDeEmprego(@PathVariable Long id) {
        oportunidadeDeEmpregoService.removerOportunidadeDeEmprego(id);
        return ResponseEntity.noContent().build();
    }
}
