package com.QueroTrabalhar.controllers;


import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.indicacao.IndicacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.indicacao.IndicacaoResponseDTO;
import com.QueroTrabalhar.services.IndicacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/indicacoes")
@Tag(name = "Indicações", description = "Endpoints autenticados para criação, consulta e remoção de indicações entre usuários.")
public class IndicacaoController {

    private final IndicacaoService indicacaoService;

    public IndicacaoController(IndicacaoService indicacaoService) {
        this.indicacaoService = indicacaoService;
    }

    @PostMapping
    @Operation(
            summary = "Criar indicação",
            description = "Cria uma indicação em nome do usuário autenticado para outro usuário existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Indicação criada com sucesso.", useReturnTypeSchema = true),
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
                    description = "Usuário indicado não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao criar a indicação.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<IndicacaoResponseDTO> criarIndicacao(
            @RequestBody @Valid IndicacaoRequestDTO indicacaoRequestDTO
    ) {
        IndicacaoResponseDTO indicacaoCriada = indicacaoService.criarIndicacao(indicacaoRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(indicacaoCriada);
    }

    @GetMapping("/me/dadas")
    @Operation(
            summary = "Listar minhas indicações dadas",
            description = "Lista as indicações criadas pelo usuário autenticado. Opera sobre o recurso /me e mantém o contrato atual de lista simples, sem paginação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicações dadas listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<List<IndicacaoResponseDTO>> listarMinhasIndicacoesDadas() {
        return ResponseEntity.ok(indicacaoService.listarIndicacoesDadasPeloUsuarioAutenticado());
    }

    @GetMapping("/me/recebidas")
    @Operation(
            summary = "Listar minhas indicações recebidas",
            description = "Lista as indicações recebidas pelo usuário autenticado. Opera sobre o recurso /me e mantém o contrato atual de lista simples, sem paginação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicações recebidas listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<List<IndicacaoResponseDTO>> listarMinhasIndicacoesRecebidas() {
        return ResponseEntity.ok(indicacaoService.listarIndicacoesRecebidasPeloUsuarioAutenticado());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir minha indicação",
            description = "Exclui uma indicação criada pelo usuário autenticado. O usuário autenticado só pode remover indicações de sua própria autoria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Indicação removida com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Indicação não encontrada.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada ao remover a indicação.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> deletarMinhaIndicacao(
            @Parameter(description = "Identificador da indicação a ser removida.", example = "14", required = true)
            @PathVariable Long id
    ) {
        indicacaoService.excluirIndicacaoDoUsuarioAutenticado(id);
        return ResponseEntity.noContent().build();
    }
}
