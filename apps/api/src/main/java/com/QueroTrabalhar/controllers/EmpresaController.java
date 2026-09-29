package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaFilterDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaRequestDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadesDaEmpresaFilterDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.RecrutadorDaEmpresaResponseDTO;
import com.QueroTrabalhar.services.EmpresaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Expõe o recorte HTTP do módulo de empresas no MVP.
 *
 * <p>O controller separa o fluxo autenticado de cadastro do catálogo público. As respostas públicas do módulo só
 * existem para empresas cuja localidade já foi validada; dados de pendência permanecem restritos aos retornos
 * internos usados no momento do cadastro.</p>
 */
@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Endpoints públicos e autenticados para cadastro, consulta e navegação de empresas.")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    @Operation(
            summary = "Criar empresa",
            description = "Cadastra uma empresa em fluxo autenticado. A localidade pode ser informada por IDs estruturados ou por texto livre. Quando a localidade ficar pendente, a resposta interna informa o status da pendência, mas a empresa não aparece nos endpoints públicos até possuir localidade validada. A confirmação manual da sugestão de localidade ainda não está implementada no MVP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa cadastrada com sucesso.", useReturnTypeSchema = true),
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
                    description = "País, estado ou cidade não encontrado para a localidade informada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao cadastrar a empresa.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<EmpresaResponseDTO> criarEmpresa(@RequestBody @Valid EmpresaRequestDTO empresaRequestDTO) {
        EmpresaResponseDTO empresaCriada = empresaService.criarEmpresa(empresaRequestDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(empresaCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(empresaCriada);
    }

    @GetMapping
    @Operation(
            summary = "Listar empresas públicas",
            description = "Lista empresas públicas com paginação e filtros. Aceita os filtros termo, paisId, estadoId e cidadeId. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=nome,asc. Os filtros de localidade consideram apenas empresas com localidade validada; empresas com localidade pendente ou sem localidade validada não aparecem neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresas listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de filtro ou paginação inválidos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<EmpresaPublicaResponseDTO>> listarEmpresas(
            @Valid @ParameterObject EmpresaFilterDTO filtro,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(empresaService.listarEmpresas(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar empresa pública por ID",
            description = "Retorna os dados públicos de uma empresa com localidade validada. Empresas com localidade pendente ou sem localidade validada não ficam disponíveis neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa retornada com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada ou indisponível nos endpoints públicos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<EmpresaPublicaResponseDTO> buscarEmpresaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.buscarEmpresaPorId(id));
    }

    @GetMapping("/{id}/recrutadores")
    @Operation(
            summary = "Listar recrutadores aprovados da empresa",
            description = "Lista os recrutadores aprovados vinculados a uma empresa disponível publicamente. Empresas com localidade pendente ou sem localidade validada não ficam disponíveis neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recrutadores aprovados listados com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada ou indisponível nos endpoints públicos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<List<RecrutadorDaEmpresaResponseDTO>> listarRecrutadoresDaEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.listarRecrutadoresAprovadosDaEmpresa(id));
    }

    @GetMapping("/{id}/oportunidades")
    @Operation(
            summary = "Listar oportunidades públicas da empresa",
            description = "Lista as oportunidades públicas de uma empresa com paginação e filtros. Aceita os filtros termo, tipoDeEmpregoId, recrutadorId, paisId, estadoId, cidadeId e modalidade. O escopo da empresa é definido pelo parâmetro de caminho id. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=id,desc. Os filtros de localidade consideram apenas oportunidades com localidade validada; oportunidades com localidade pendente ou sem localidade validada não aparecem neste endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oportunidades da empresa listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de filtro ou paginação inválidos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada ou indisponível nos endpoints públicos.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<OportunidadeDeEmpregoPublicaResponseDTO>> listarOportunidadesDaEmpresa(
            @PathVariable Long id,
            @Valid @ParameterObject OportunidadesDaEmpresaFilterDTO filtro,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(empresaService.listarOportunidadesDaEmpresa(id, filtro, pageable));
    }
}
