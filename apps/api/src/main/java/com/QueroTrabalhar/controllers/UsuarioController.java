package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.controllers.exceptions.ValidationError;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorUsuarioRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.AlterarSenhaRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioAtualizacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioRequestDTO;
import com.QueroTrabalhar.domain.dtos.usuario.UsuarioResponseDTO;
import com.QueroTrabalhar.services.UsuarioService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Endpoints para cadastro e gestão do usuário autenticado.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastrar")
    @Operation(
            summary = "Cadastrar usuário",
            description = "Cria uma nova conta de usuário. Este endpoint é público e não requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou CPF já cadastrado.",
                    content = @Content(schema = @Schema(oneOf = {StandardError.class, ValidationError.class}))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um usuário com o e-mail informado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@RequestBody @Valid UsuarioRequestDTO usuarioRequest) {
        UsuarioResponseDTO usuarioResponse = usuarioService.cadastrarUsuario(usuarioRequest);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuarioResponse.id())
                .toUri();

        return ResponseEntity.created(uri).body(usuarioResponse);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Consultar meu usuário",
            description = "Retorna os dados do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado retornado com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário autenticado não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<UsuarioResponseDTO> buscarMeuUsuario() {
        return ResponseEntity.ok(usuarioService.buscarUsuarioAutenticado());
    }

    @PutMapping("/me")
    @Operation(
            summary = "Atualizar meu usuário",
            description = "Atualiza os dados cadastrais do usuário autenticado."
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
                    responseCode = "404",
                    description = "Usuário autenticado não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "O e-mail informado já está em uso por outro usuário.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<UsuarioResponseDTO> atualizarMeuUsuario(
            @Valid @RequestBody UsuarioAtualizacaoRequestDTO usuarioAtualizacaoRequest
    ) {
        return ResponseEntity.ok(usuarioService.atualizarMeuUsuario(usuarioAtualizacaoRequest));
    }

    @PutMapping("/me/senha")
    @Operation(
            summary = "Alterar minha senha",
            description = "Altera a senha do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso."),
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
                    responseCode = "422",
                    description = "Regra de negócio violada ao alterar a senha.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> alterarMinhaSenha(@Valid @RequestBody AlterarSenhaRequestDTO alterarSenhaRequest) {
        usuarioService.alterarMinhaSenha(alterarSenhaRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/perfil-candidato")
    @Operation(
            summary = "Remover meu perfil de candidato",
            description = "Remove o perfil de candidato do usuário autenticado."
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
                    responseCode = "404",
                    description = "Perfil de candidato não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerMeuPerfilCandidato() {
        usuarioService.removerMeuPerfilCandidato();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/perfil-recrutador")
    @Operation(
            summary = "Remover meu perfil de recrutador",
            description = "Remove o perfil de recrutador do usuário autenticado."
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
                    responseCode = "404",
                    description = "Perfil de recrutador não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> removerMeuPerfilRecrutador() {
        usuarioService.removerMeuPerfilRecrutador();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/perfil-candidato")
    @Operation(
            summary = "Adicionar meu perfil de candidato",
            description = "Adiciona o perfil de candidato ao usuário autenticado."
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
            )
    })
    public ResponseEntity<Void> adicionarMeuPerfilCandidato() {
        usuarioService.adicionarMeuPerfilCandidato();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/perfil-recrutador")
    @Operation(
            summary = "Adicionar meu perfil de recrutador",
            description = "Adiciona o perfil de recrutador ao usuário autenticado."
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
            )
    })
    public ResponseEntity<Void> adicionarMeuPerfilRecrutador(
            @Valid @RequestBody PerfilRecrutadorUsuarioRequestDTO perfilRecrutadorRequest
    ) {
        usuarioService.adicionarMeuPerfilRecrutador(perfilRecrutadorRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "Remover meu usuário",
            description = "Remove a conta do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário autenticado não encontrado.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<Void> meRemover() {
        usuarioService.meRemover();
        return ResponseEntity.noContent().build();
    }
}
