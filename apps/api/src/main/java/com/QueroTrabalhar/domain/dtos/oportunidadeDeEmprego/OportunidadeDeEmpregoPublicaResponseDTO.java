package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.Modalidade;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa a projeção pública de uma oportunidade.
 *
 * <p>O DTO deliberadamente não expõe detalhes de {@code LocalidadePendente} nem campos internos de suporte ao dono do
 * recurso. Ele assume que a vaga já foi filtrada pela camada pública e, portanto, só carrega dados consolidados.</p>
 */
@Schema(name = "OportunidadeDeEmpregoPublicaResponseDTO", description = "Dados públicos de uma oportunidade disponível nos endpoints públicos. Apenas oportunidades com localidade validada são expostas.")
public record OportunidadeDeEmpregoPublicaResponseDTO(
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

        @Schema(description = "Identificador do recrutador que publicou a oportunidade.", example = "12")
        Long recrutadorId,

        @Schema(description = "Nome do recrutador que publicou a oportunidade.", example = "Marina Lima")
        String recrutadorNome,

        @Schema(description = "Identificador da empresa associada à publicação, quando houver.", example = "7")
        Long empresaId,

        @Schema(description = "Nome da empresa associada à publicação, quando houver.", example = "Quero Trabalhar")
        String empresaNome
) {
    /**
     * Converte a entidade para a visão pública já filtrada pelas regras de localidade validada.
     */
    public static OportunidadeDeEmpregoPublicaResponseDTO daEntidade(OportunidadeDeEmprego entidade) {
        TipoDeEmprego tipoDeEmprego = entidade.getTipoDeEmprego();
        Localidade localidade = entidade.getLocalizacao();
        Pais pais = localidade != null ? localidade.getPais() : null;
        Estado estado = localidade != null ? localidade.getEstado() : null;
        Cidade cidade = localidade != null ? localidade.getCidade() : null;
        PerfilRecrutador recrutador = entidade.getPerfilRecrutador();
        Usuario usuario = recrutador != null ? recrutador.getUsuario() : null;
        Empresa empresa = entidade.getEmpresa();

        return new OportunidadeDeEmpregoPublicaResponseDTO(
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
                recrutador != null ? recrutador.getId() : null,
                usuario != null ? usuario.getNome() : null,
                empresa != null ? empresa.getId() : null,
                empresa != null ? empresa.getNome() : null
        );
    }
}
