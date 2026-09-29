package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa a resposta interna das oportunidades em fluxos autenticados do recrutador.
 *
 * <p>Além dos dados públicos da vaga, o DTO pode expor status de localidade, texto original e motivo de pendência,
 * porque essas informações são úteis para o dono do recurso entender por que a oportunidade ainda não aparece nos
 * endpoints públicos. O contrato também explicita quem é o recrutador dono e se a publicação está associada a uma
 * empresa.</p>
 */
@Schema(name = "OportunidadeDeEmpregoResponseDTO", description = "Dados internos da oportunidade retornados em fluxos autenticados do recrutador.")
public record OportunidadeDeEmpregoResponseDTO(
        @Schema(description = "Identificador da oportunidade.", example = "101")
        Long id,

        @Schema(description = "Descrição pública da oportunidade.", example = "Desenvolvedor Backend Java 21 com Spring Boot.")
        String descricao,

        @Schema(description = "Modalidade da oportunidade.", example = "REMOTO")
        Modalidade modalidade,

        @Schema(description = "Identificador do tipo de emprego.", example = "3")
        Long tipoDeEmpregoId,

        @Schema(description = "Título do tipo de emprego.", example = "Desenvolvedor Backend")
        String tipoDeEmprego,

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

        @Schema(description = "Status interno da localidade da oportunidade. Campo de apoio ao dono do recurso e não utilizado como comportamento público.", example = "VALIDADA")
        String statusLocalidade,

        @Schema(description = "Texto original informado para a localidade quando a oportunidade ficou pendente. Campo interno; oportunidades nesse estado não aparecem nos endpoints públicos.", example = "João Pessoa e região")
        String localidadeTextoOriginal,

        @Schema(description = "Status detalhado da validação interna da localidade pendente. Campo interno do dono do recurso. No MVP atual, estados de confirmação ou recusa manual permanecem reservados para evolução futura e não indicam funcionalidade pública já disponível.", example = "PENDENTE_VALIDACAO")
        StatusValidacaoLocalidade statusValidacaoLocalidade,

        @Schema(description = "Motivo registrado para a pendência de localidade. Campo interno do dono do recurso.", example = "Localidade não encontrada na base estruturada.")
        String motivoPendenciaLocalidade,

        @Schema(description = "Identificador do recrutador dono da oportunidade.", example = "12")
        Long recrutadorId,

        @Schema(description = "Nome do recrutador dono da oportunidade.", example = "Marina Lima")
        String recrutadorNome,

        @Schema(description = "Identificador da empresa vinculada à publicação, quando houver.", example = "7")
        Long empresaId,

        @Schema(description = "Nome da empresa vinculada à publicação, quando houver.", example = "Quero Trabalhar")
        String empresaNome
) {
    private static final String STATUS_LOCALIDADE_VALIDADA = "VALIDADA";
    private static final String STATUS_LOCALIDADE_PENDENTE = "PENDENTE";

    public static OportunidadeDeEmpregoResponseDTO daEntidade(OportunidadeDeEmprego entidade) {
        return daEntidade(entidade, null);
    }

    /**
     * Converte a entidade para a visão interna, ocultando pendência antiga quando a localidade já estiver validada.
     */
    public static OportunidadeDeEmpregoResponseDTO daEntidade(
            OportunidadeDeEmprego entidade,
            LocalidadePendente localidadePendente
    ) {
        TipoDeEmprego tipoDeEmprego = entidade.getTipoDeEmprego();
        Localidade localidade = entidade.getLocalizacao();
        LocalidadePendente pendenciaEfetiva = localidade != null ? null : localidadePendente;
        Pais pais = localidade != null ? localidade.getPais() : null;
        Estado estado = localidade != null ? localidade.getEstado() : null;
        Cidade cidade = localidade != null ? localidade.getCidade() : null;
        PerfilRecrutador recrutador = entidade.getPerfilRecrutador();
        Usuario usuario = recrutador != null ? recrutador.getUsuario() : null;
        Empresa empresa = entidade.getEmpresa();
        String statusLocalidade = null;

        if (localidade != null) {
            statusLocalidade = STATUS_LOCALIDADE_VALIDADA;
        } else if (pendenciaEfetiva != null) {
            statusLocalidade = STATUS_LOCALIDADE_PENDENTE;
        }

        return new OportunidadeDeEmpregoResponseDTO(
                entidade.getId(),
                entidade.getDescricao(),
                entidade.getModalidade(),
                tipoDeEmprego != null ? tipoDeEmprego.getId() : null,
                tipoDeEmprego != null ? tipoDeEmprego.getTitulo() : null,
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
                pendenciaEfetiva != null ? pendenciaEfetiva.getMotivoPendencia() : null,
                recrutador != null ? recrutador.getId() : null,
                usuario != null ? usuario.getNome() : null,
                empresa != null ? empresa.getId() : null,
                empresa != null ? empresa.getNome() : null
        );
    }
}
