package com.QueroTrabalhar.controllers;

import com.QueroTrabalhar.controllers.exceptions.StandardError;
import com.QueroTrabalhar.domain.dtos.localidade.CidadeResponseDTO;
import com.QueroTrabalhar.domain.dtos.localidade.EstadoResponseDTO;
import com.QueroTrabalhar.domain.dtos.localidade.PaisResponseDTO;
import com.QueroTrabalhar.services.LocalidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/localidades")
@Tag(
        name = "Localidades",
        description = "Endpoints públicos de catálogo e autocomplete para consulta de localidades oficiais disponíveis na base. Não expõem Google Maps, localidades pendentes ou fluxos internos de confirmação manual."
)
public class LocalidadeController {


    private final LocalidadeService localidadeService;

    public LocalidadeController(LocalidadeService localidadeService) {
        this.localidadeService = localidadeService;
    }

    //GET /api/localidades/paises?termo={termo}
    @GetMapping("/paises")
    @Operation(
            summary = "Listar países oficiais",
            description = "Consulta o catálogo oficial de países disponíveis na base para autocomplete. Usa o parâmetro termo e retorna lista vazia quando a busca tiver menos de 2 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Países oficiais listados com sucesso.", useReturnTypeSchema = true)
    })
    public ResponseEntity<List<PaisResponseDTO>> buscarPaises(
            @Parameter(
                    description = "Trecho do nome do país usado no autocomplete oficial. Quando tiver menos de 2 caracteres, o endpoint retorna lista vazia.",
                    example = "bra"
            )
            @RequestParam(name = "termo", defaultValue = "") String termoBusca) {

        //Talvez mudar para o front (discutir com YASMIM)
        // Trava de Performance: Só busca se o usuário digitar pelo menos 2 letras
        if (termoBusca.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<PaisResponseDTO> resultado = localidadeService.buscarPais(termoBusca.trim());
        return ResponseEntity.ok(resultado);
    }


    //GET /api/localidades/estados?paisId={id}&termo={termo}
    @GetMapping("/estados")
    @Operation(
            summary = "Listar estados oficiais",
            description = "Consulta o catálogo oficial de estados disponíveis na base para um país já cadastrado, em fluxo de autocomplete. Requer paisId, aceita o parâmetro termo e retorna lista vazia quando a busca tiver menos de 2 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estados oficiais listados com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros inválidos ou país não encontrado na base oficial.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<List<EstadoResponseDTO>> buscarEstados(
            @Parameter(
                    description = "Identificador do país já existente na base oficial para restringir o catálogo de estados.",
                    example = "1",
                    required = true
            )
            @RequestParam(name = "paisId") Long paisId,
            @Parameter(
                    description = "Trecho do nome do estado usado no autocomplete oficial. Quando tiver menos de 2 caracteres, o endpoint retorna lista vazia.",
                    example = "para"
            )
            @RequestParam(name = "termo", defaultValue = "") String termoBusca) {

        if (termoBusca.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<EstadoResponseDTO> resultado = localidadeService.buscarEstado(paisId, termoBusca.trim());
        return ResponseEntity.ok(resultado);
    }


    //GET /api/localidades/cidades?estadoId={id}&termo={termo}
    @GetMapping("/cidades")
    @Operation(
            summary = "Listar cidades oficiais",
            description = "Consulta o catálogo oficial de cidades disponíveis na base para um estado já cadastrado, em fluxo de autocomplete. Requer estadoId, aceita o parâmetro termo e retorna lista vazia quando a busca tiver menos de 2 caracteres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cidades oficiais listadas com sucesso.", useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros inválidos ou estado não encontrado na base oficial.",
                    content = @Content(schema = @Schema(implementation = StandardError.class))
            )
    })
    public ResponseEntity<List<CidadeResponseDTO>> buscarCidades(
            @Parameter(
                    description = "Identificador do estado já existente na base oficial para restringir o catálogo de cidades.",
                    example = "25",
                    required = true
            )
            @RequestParam(name = "estadoId") Long estadoId,
            @Parameter(
                    description = "Trecho do nome da cidade usado no autocomplete oficial. Quando tiver menos de 2 caracteres, o endpoint retorna lista vazia.",
                    example = "jo"
            )
            @RequestParam(name = "termo", defaultValue = "") String termoBusca) {

        if (termoBusca.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<CidadeResponseDTO> resultado = localidadeService.buscarCidade(estadoId, termoBusca.trim());
        return ResponseEntity.ok(resultado);
    }

}
