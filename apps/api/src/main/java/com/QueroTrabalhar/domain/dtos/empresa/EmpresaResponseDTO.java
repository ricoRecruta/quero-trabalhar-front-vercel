package com.QueroTrabalhar.domain.dtos.empresa;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa a resposta interna usada em fluxos autenticados do módulo de empresas.
 *
 * <p>Diferentemente do DTO público, este contrato pode informar status da localidade, texto original enviado e motivo
 * da pendência para apoiar o cliente que acabou de criar o recurso ou que precisa entender por que ele ainda não está
 * exposto publicamente. O ownership formal da empresa continua fora do escopo do MVP atual.</p>
 */
@Schema(name = "EmpresaResponseDTO", description = "Dados internos da empresa retornados em fluxos autenticados.")
public record EmpresaResponseDTO(
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
        String cidade,

        @Schema(description = "Status interno da localidade da empresa. Campo de apoio ao dono do recurso e não utilizado nos endpoints públicos.", example = "VALIDADA")
        String statusLocalidade,

        @Schema(description = "Texto original informado para a localidade quando a empresa ficou com localidade pendente. Campo interno; empresas nesse estado não aparecem nos endpoints públicos.", example = "João Pessoa e região")
        String localidadeTextoOriginal,

        @Schema(description = "Status detalhado da validação interna da localidade pendente. Campo interno do fluxo autenticado. No MVP atual, estados de confirmação ou recusa manual permanecem reservados para evolução futura e não indicam funcionalidade pública já disponível.", example = "PENDENTE_VALIDACAO")
        StatusValidacaoLocalidade statusValidacaoLocalidade,

        @Schema(description = "Motivo registrado para a pendência de localidade. Campo interno do fluxo autenticado.", example = "Localidade não encontrada na base estruturada.")
        String motivoPendenciaLocalidade
) {
    private static final String STATUS_LOCALIDADE_VALIDADA = "VALIDADA";
    private static final String STATUS_LOCALIDADE_PENDENTE = "PENDENTE";

    public static EmpresaResponseDTO daEntidade(Empresa entidade) {
        return daEntidade(entidade, null);
    }

    /**
     * Converte a entidade para o contrato interno priorizando a localidade validada sobre qualquer pendência antiga.
     *
     * <p>Se a empresa já possui localidade oficial, os campos de pendência são ocultados no DTO para refletir o estado
     * atual do recurso.</p>
     */
    public static EmpresaResponseDTO daEntidade(Empresa entidade, LocalidadePendente localidadePendente) {
        Localidade localidade = entidade.getLocalidade();
        LocalidadePendente pendenciaEfetiva = localidade != null ? null : localidadePendente;
        Pais pais = localidade != null ? localidade.getPais() : null;
        Estado estado = localidade != null ? localidade.getEstado() : null;
        Cidade cidade = localidade != null ? localidade.getCidade() : null;

        String statusLocalidade = null;
        if (localidade != null) {
            statusLocalidade = STATUS_LOCALIDADE_VALIDADA;
        } else if (pendenciaEfetiva != null) {
            statusLocalidade = STATUS_LOCALIDADE_PENDENTE;
        }

        return new EmpresaResponseDTO(
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
                cidade != null ? cidade.getNome() : null,
                statusLocalidade,
                pendenciaEfetiva != null ? pendenciaEfetiva.getTextoOriginal() : null,
                pendenciaEfetiva != null ? pendenciaEfetiva.getStatusValidacao() : null,
                pendenciaEfetiva != null ? pendenciaEfetiva.getMotivoPendencia() : null
        );
    }
}
