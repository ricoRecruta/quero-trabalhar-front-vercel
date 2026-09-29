package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorUsuarioRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioAtualizacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioFilterDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioResponseDTO;
import com.QueroTrabalhar.services.UsuarioService;
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
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administração de usuários", description = "Endpoints administrativos para consulta e gestão de usuários.")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    public UsuarioAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
            summary = "Listar usuários",
            description = "Lista usuários com paginação e filtros. Aceita os filtros termo, nome, email, cpf, temPerfilCandidato e temPerfilRecrutador. A paginação usa os parâmetros page, size e sort, com padrão page=0, size=10 e sort=nome,asc."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso.", useReturnTypeSchema = true),
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
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(
            @Valid @ParameterObject UsuarioFilterDTO filtro,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Busca um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar usuário por ID",
            description = "Atualiza os dados cadastrais de um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos.",
                    content = @Content(schema = @Schema(oneOf = {StandardError.class, ValidationError.class}))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "O e-mail informado já está em uso por outro usuário.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAtualizacaoRequestDTO usuarioRequest
    ) {
        return ResponseEntity.ok(usuarioService.atualizarUsuarioComoAdmin(id, usuarioRequest));
    }

    @DeleteMapping("/{id}/perfil-candidato")
    @Operation(
            summary = "Remover perfil de candidato por ID",
            description = "Remove o perfil de candidato de um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil de candidato removido com sucesso."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Operação inválida para o estado atual do usuário.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou perfil de candidato não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerPerfilCandidatoPorId(@PathVariable Long id) {
        usuarioService.removerPerfilCandidatoPorId(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/perfil-recrutador")
    @Operation(
            summary = "Remover perfil de recrutador por ID",
            description = "Remove o perfil de recrutador de um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil de recrutador removido com sucesso."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Operação inválida para o estado atual do usuário.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou perfil de recrutador não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerPerfilRecrutadorPorId(@PathVariable Long id) {
        usuarioService.removerPerfilRecrutadorPorId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/perfil-candidato")
    @Operation(
            summary = "Adicionar perfil de candidato por ID",
            description = "Adiciona o perfil de candidato a um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil de candidato adicionado com sucesso."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Operação inválida para o estado atual do usuário.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> adicionarPerfilCandidato(@PathVariable Long id) {
        usuarioService.adicionarPerfilCandidatoPorId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/perfil-recrutador")
    @Operation(
            summary = "Adicionar perfil de recrutador por ID",
            description = "Adiciona o perfil de recrutador a um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil de recrutador adicionado com sucesso."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou operação incompatível com o estado atual do usuário.",
                    content = @Content(schema = @Schema(oneOf = {StandardError.class, ValidationError.class}))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> adicionarPerfilRecrutador(
            @PathVariable Long id,
            @Valid @RequestBody PerfilRecrutadorUsuarioRequestDTO perfilRecrutadorRequest
    ) {
        usuarioService.adicionarPerfilRecrutadorPorId(id, perfilRecrutadorRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover usuário por ID",
            description = "Remove um usuário pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerUsuarioPorId(@PathVariable Long id) {
        usuarioService.deletarUsuarioPorId(id);
        return ResponseEntity.noContent().build();
    }
}
