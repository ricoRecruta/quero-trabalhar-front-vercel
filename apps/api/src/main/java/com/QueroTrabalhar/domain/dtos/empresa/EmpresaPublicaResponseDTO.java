package com.QueroTrabalhar.domain.dtos.empresa;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa a projeção pública de uma empresa.
 *
 * <p>Este DTO existe para manter a API pública limitada a dados já consolidados. Informações sobre localidade
 * pendente, texto original enviado ou status interno de validação não são expostas aqui.</p>
 */
@Schema(name = "EmpresaPublicaResponseDTO", description = "Dados públicos de uma empresa disponível nos endpoints públicos. Apenas empresas com localidade validada são expostas.")
public record EmpresaPublicaResponseDTO(
        @Schema(description = "Identificador da empresa.", example = "7")
        Long id,

        @Schema(description = "Nome da empresa.", example = "Quero Trabalhar")
        String nome,

        @Schema(description = "Descrição pública da empresa.", example = "Plataforma de empregabilidade com foco em recrutamento e indicação.")
        String descricao,

        @Schema(description = "Site público da empresa.", example = "https://www.querotrabalhar.com.br")
        String site,

        @Schema(description = "E-mail público de contato da empresa.", example = "contato@querotrabalhar.com.br", format = "email")
        String emailPublico,

        @Schema(description = "Telefone público de contato da empresa.", example = "83999999999")
        String telefonePublico,

        @Schema(description = "Indica se a empresa está ativa.", example = "true")
        boolean ativo,

        @Schema(description = "Identificador do país da localidade validada.", example = "1")
        Long paisId,

        @Schema(description = "Nome do país da localidade validada.", example = "Brasil")
        String pais,

        @Schema(description = "Sigla do país da localidade validada.", example = "BR")
        String paisSigla,

        @Schema(description = "Identificador do estado da localidade validada.", example = "25")
        Long estadoId,

        @Schema(description = "Nome do estado da localidade validada.", example = "Paraíba")
        String estado,

        @Schema(description = "Sigla do estado da localidade validada.", example = "PB")
        String estadoSigla,

        @Schema(description = "Identificador da cidade da localidade validada.", example = "2507507")
        Long cidadeId,

        @Schema(description = "Nome da cidade da localidade validada.", example = "João Pessoa")
        String cidade
) {
    /**
     * Converte a entidade para a visão pública já filtrada pela camada de serviço/repositório.
     */
    public static EmpresaPublicaResponseDTO daEntidade(Empresa entidade) {
        Localidade localidade = entidade.getLocalidade();
        Pais pais = localidade != null ? localidade.getPais() : null;
        Estado estado = localidade != null ? localidade.getEstado() : null;
        Cidade cidade = localidade != null ? localidade.getCidade() : null;

        return new EmpresaPublicaResponseDTO(
                entidade.getId(),
                entidade.getNome(),
                entidade.getDescricao(),
                entidade.getSite(),
                entidade.getEmailPublico(),
                entidade.getTelefonePublico(),
                entidade.isAtivo(),
                pais != null ? pais.getId() : null,
                pais != null ? pais.getNome() : null,
                pais != null ? pais.getSigla() : null,
                estado != null ? estado.getId() : null,
                estado != null ? estado.getNome() : null,
                estado != null ? estado.getSigla() : null,
                cidade != null ? cidade.getId() : null,
                cidade != null ? cidade.getNome() : null
        );
    }
}
