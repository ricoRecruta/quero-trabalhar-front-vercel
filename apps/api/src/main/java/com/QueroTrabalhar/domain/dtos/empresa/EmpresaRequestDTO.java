package com.QueroTrabalhar.domain.dtos.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Contrato de entrada do cadastro autenticado de empresas.
 *
 * <p>O DTO aceita dois modos de informar localidade: por IDs já validados no catálogo interno ou por texto livre para
 * resolução técnica. Quando qualquer ID é enviado, esse caminho tem precedência sobre {@code localidadeTexto}. O DTO
 * não carrega campos de ownership porque essa relação formal ainda não existe no MVP.</p>
 */
@Schema(name = "EmpresaRequestDTO", description = "Dados para cadastro de empresa em fluxo autenticado.")
public record EmpresaRequestDTO(
        @NotBlank(message = "O nome da empresa é obrigatório.")
        @Size(max = 150, message = "O nome da empresa deve ter no máximo 150 caracteres.")
        @Schema(description = "Nome da empresa.", example = "Quero Trabalhar", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,

        @Size(max = 1000, message = "A descrição da empresa deve ter no máximo 1000 caracteres.")
        @Schema(description = "Descrição pública da empresa.", example = "Plataforma de empregabilidade com foco em recrutamento e indicação.")
        String descricao,

        @Size(max = 255, message = "O site da empresa deve ter no máximo 255 caracteres.")
        @Schema(description = "Site público da empresa.", example = "https://www.querotrabalhar.com.br")
        String site,

        @Email(message = "O e-mail público deve ser válido.")
        @Size(max = 150, message = "O e-mail público deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail público de contato da empresa.", example = "contato@querotrabalhar.com.br", format = "email")
        String emailPublico,

        @Size(max = 20, message = "O telefone público deve ter no máximo 20 caracteres.")
        @Schema(description = "Telefone público de contato da empresa.", example = "83999999999")
        String telefonePublico,

        @Positive(message = "O país informado é inválido.")
        @Schema(description = "Identificador do país da localidade validada. Quando qualquer ID de localidade é informado, os IDs estruturados têm prioridade sobre localidadeTexto.", example = "1")
        Long paisId,

        @Positive(message = "O estado informado é inválido.")
        @Schema(description = "Identificador do estado da localidade validada.", example = "25")
        Long estadoId,

        @Positive(message = "A cidade informada é inválida.")
        @Schema(description = "Identificador da cidade da localidade validada.", example = "2507507")
        Long cidadeId,

        @Size(max = 255, message = "O texto da localidade deve ter no máximo 255 caracteres.")
        @Schema(description = "Texto livre da localidade. Use quando não houver IDs estruturados. Se a resolução automática não validar a localidade, a empresa será criada com localidade pendente no fluxo interno e não aparecerá nos endpoints públicos. A confirmação manual da sugestão ainda não está implementada no MVP.", example = "João Pessoa, PB")
        String localidadeTexto
) {
}
