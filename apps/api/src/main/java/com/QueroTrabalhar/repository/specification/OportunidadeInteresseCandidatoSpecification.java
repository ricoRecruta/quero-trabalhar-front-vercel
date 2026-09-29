package com.QueroTrabalhar.repository.specification;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeInteresseCandidatoFilterDTO;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusLocalidadeFiltro;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Define os filtros da visão interna das oportunidades marcadas como interesse pelo candidato autenticado.
 *
 * <p>A specification preserva o recorte de relacionamento do candidato e, opcionalmente, permite distinguir vagas com
 * localidade validada das que ainda dependem de resolução. Isso evita que o cliente trate ausência de localidade como
 * sinônimo automático de pendência sem registro formal.</p>
 */
public final class OportunidadeInteresseCandidatoSpecification {

    private OportunidadeInteresseCandidatoSpecification() {
    }

    /**
     * Monta os predicados da listagem de vagas de interesse do candidato autenticado.
     */
    public static Specification<OportunidadeDeEmprego> comFiltros(
            Long perfilCandidatoId,
            OportunidadeInteresseCandidatoFilterDTO filtro
    ) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(
                    root.join("candidatosInteressados").get("id"),
                    perfilCandidatoId
            ));

            String termo = normalizarTexto(filtro == null ? null : filtro.termo());
            if (termo != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("descricao")),
                        "%" + termo.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (filtro != null && filtro.tipoDeEmpregoId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("tipoDeEmprego").get("id"),
                        filtro.tipoDeEmpregoId()
                ));
            }

            if (filtro != null && filtro.empresaId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("empresa", jakarta.persistence.criteria.JoinType.LEFT).get("id"),
                        filtro.empresaId()
                ));
            }

            if (filtro != null && filtro.recrutadorId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("perfilRecrutador").get("id"),
                        filtro.recrutadorId()
                ));
            }

            if (filtro != null && filtro.paisId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("pais").get("id"),
                        filtro.paisId()
                ));
            }

            if (filtro != null && filtro.estadoId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("estado").get("id"),
                        filtro.estadoId()
                ));
            }

            if (filtro != null && filtro.cidadeId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("cidade").get("id"),
                        filtro.cidadeId()
                ));
            }

            if (filtro != null && filtro.modalidade() != null) {
                predicates.add(criteriaBuilder.equal(root.get("modalidade"), filtro.modalidade()));
            }

            StatusLocalidadeFiltro statusLocalidade = filtro == null ? null : filtro.statusLocalidade();
            if (statusLocalidade == StatusLocalidadeFiltro.VALIDADA) {
                predicates.add(criteriaBuilder.isNotNull(root.get("localidade").get("pais")));
            } else if (statusLocalidade == StatusLocalidadeFiltro.PENDENTE) {
                predicates.add(criteriaBuilder.isNull(root.get("localidade").get("pais")));
                predicates.add(criteriaBuilder.exists(criarSubqueryDePendenciaLocalidade(query, root, criteriaBuilder)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Confirma que a vaga sem localidade validada realmente possui pendência registrada de localidade.
     */
    private static Subquery<Long> criarSubqueryDePendenciaLocalidade(
            CriteriaQuery<?> query,
            Root<OportunidadeDeEmprego> root,
            CriteriaBuilder criteriaBuilder
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<LocalidadePendente> pendenciaRoot = subquery.from(LocalidadePendente.class);

        subquery.select(pendenciaRoot.get("id"));
        subquery.where(
                criteriaBuilder.equal(
                        pendenciaRoot.get("tipoRecurso"),
                        TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO
                ),
                criteriaBuilder.equal(
                        pendenciaRoot.get("campoAlvo"),
                        CampoLocalidadePendente.LOCALIDADE
                ),
                criteriaBuilder.equal(
                        pendenciaRoot.get("recursoId"),
                        root.get("id")
                )
        );

        return subquery;
    }

    private static String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;
    }
}
