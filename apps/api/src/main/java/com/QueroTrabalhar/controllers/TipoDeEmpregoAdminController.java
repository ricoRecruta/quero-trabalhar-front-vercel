package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoResponseDTO;
import com.QueroTrabalhar.services.TipoDeEmpregoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tipos-emprego")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Administração de tipos de emprego",
        description = "Endpoints administrativos para moderação e manutenção do catálogo de tipos de emprego. Exigem permissão ADMIN."
)
public class TipoDeEmpregoAdminController {

    private final TipoDeEmpregoService tipoDeEmpregoService;

    public TipoDeEmpregoAdminController(TipoDeEmpregoService tipoDeEmpregoService) {
        this.tipoDeEmpregoService = tipoDeEmpregoService;
    }

    @GetMapping("/nao-aprovados")
    @Operation(
            summary = "Listar tipos de emprego não aprovados",
            description = "Lista a fila administrativa de moderação dos tipos de emprego ainda não aprovados. Aceita o filtro termo. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. O backend aplica obrigatoriamente o filtro aprovado=false e ignora qualquer tentativa do cliente de controlar esse estado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fila de tipos de emprego não aprovados listada com sucesso.", useReturnTypeSchema = true),
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
    public ResponseEntity<Page<TipoDeEmpregoResponseDTO>> listarNaoAprovados(
            @Valid @ParameterObject TipoDeEmpregoFilterDTO filtro,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(tipoDeEmpregoService.listarNaoAprovados(filtro, pageable));
    }

    @PostMapping
    @Operation(
            summary = "Criar tipo de emprego no catálogo",
            description = "Cria, como ADMIN, um tipo de emprego já aprovado diretamente no catálogo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipo de emprego criado com sucesso no catálogo.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido ou título já existente no catálogo.",
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
            )
    })
    public ResponseEntity<TipoDeEmpregoResponseDTO> criarNoCatalogo(
            @Valid @RequestBody TipoDeEmpregoRequestDTO tipoDeEmprego
    ) {
        TipoDeEmpregoResponseDTO tipoDeEmpregoCriado = tipoDeEmpregoService.criarNoCatalogo(tipoDeEmprego);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tipoDeEmpregoCriado.id())
                .toUri();

        return ResponseEntity.created(uri).body(tipoDeEmpregoCriado);
    }

    @PatchMapping("/{id}/aprovar")
    @Operation(
            summary = "Aprovar sugestão de tipo de emprego",
            description = "Aprova, como ADMIN, uma sugestão da fila de moderação. O payload permite ajustar título e descrição antes da aprovação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão aprovada com sucesso.", useReturnTypeSchema = true),
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
                    description = "Tipo de emprego não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<TipoDeEmpregoResponseDTO> aprovarSugestao(
            @Parameter(description = "Identificador do tipo de emprego pendente na fila de moderação.", example = "8", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TipoDeEmpregoRequestDTO tipoDeEmprego
    ) {
        TipoDeEmpregoResponseDTO aprovado = tipoDeEmpregoService.aprovarSugestao(
                id,
                tipoDeEmprego.titulo(),
                tipoDeEmprego.descricao()
        );
        return ResponseEntity.ok(aprovado);
    }

    @PatchMapping("/aprovar-lote")
    @Operation(
            summary = "Aprovar sugestões em lote",
            description = "Aprova, como ADMIN, várias sugestões da fila de moderação a partir de um array JSON simples de IDs numéricos, por exemplo [8, 11, 15]. No contrato atual, o endpoint não aceita objetos por item e não retorna corpo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sugestões aprovadas em lote com sucesso."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista de IDs inválida ou nenhum tipo de emprego encontrado para os IDs informados.",
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
    public ResponseEntity<Void> aprovarEmLote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Array JSON simples com os IDs numéricos das sugestões pendentes a aprovar em lote.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(type = "integer", format = "int64"))
                    )
            )
            @RequestBody List<Long> ids
    ) {
        tipoDeEmpregoService.aprovarEmLote(ids);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover tipo de emprego",
            description = "Remove, como ADMIN, um tipo de emprego pelo identificador. No contrato atual, o recurso precisa existir e não pode estar em uso por vagas associadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tipo de emprego removido com sucesso."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Tipo de emprego em uso ou operação inválida para o estado atual do recurso.",
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tipo de emprego não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do tipo de emprego a ser removido.", example = "8", required = true)
            @PathVariable Long id
    ) {
        tipoDeEmpregoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
