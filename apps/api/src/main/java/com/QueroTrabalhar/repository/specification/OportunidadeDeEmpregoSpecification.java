package com.QueroTrabalhar.repository.specification;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadesDaEmpresaFilterDTO;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.enums.Modalidade;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Centraliza os filtros do catálogo público de oportunidades.
 *
 * <p>Tanto a listagem global quanto a listagem pública por empresa passam pela mesma regra-base: somente
 * oportunidades com localidade validada entram no resultado. Os demais filtros apenas refinam esse conjunto.</p>
 */
public final class OportunidadeDeEmpregoSpecification {

    private OportunidadeDeEmpregoSpecification() {
    }

    /**
     * Monta os predicados da listagem pública geral de oportunidades.
     */
    public static Specification<OportunidadeDeEmprego> comFiltros(OportunidadeDeEmpregoFilterDTO filtro) {
        return comFiltrosInternos(
                filtro == null ? null : filtro.termo(),
                filtro == null ? null : filtro.tipoDeEmpregoId(),
                filtro == null ? null : filtro.empresaId(),
                filtro == null ? null : filtro.recrutadorId(),
                filtro == null ? null : filtro.paisId(),
                filtro == null ? null : filtro.estadoId(),
                filtro == null ? null : filtro.cidadeId(),
                filtro == null ? null : filtro.modalidade()
        );
    }

    /**
     * Monta os predicados da listagem pública de oportunidades dentro do contexto de uma empresa.
     *
     * <p>O identificador da empresa vem do path na camada HTTP e é injetado aqui como escopo fixo, sem depender de
     * query parameter do cliente.</p>
     */
    public static Specification<OportunidadeDeEmprego> comFiltrosDaEmpresa(
            Long empresaId,
            OportunidadesDaEmpresaFilterDTO filtro
    ) {
        return comFiltrosInternos(
                filtro == null ? null : filtro.termo(),
                filtro == null ? null : filtro.tipoDeEmpregoId(),
                empresaId,
                filtro == null ? null : filtro.recrutadorId(),
                filtro == null ? null : filtro.paisId(),
                filtro == null ? null : filtro.estadoId(),
                filtro == null ? null : filtro.cidadeId(),
                filtro == null ? null : filtro.modalidade()
        );
    }

    private static Specification<OportunidadeDeEmprego> comFiltrosInternos(
            String termoBruto,
            Long tipoDeEmpregoId,
            Long empresaId,
            Long recrutadorId,
            Long paisId,
            Long estadoId,
            Long cidadeId,
            Modalidade modalidade
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNotNull(root.get("localidade").get("pais")));

            String termo = normalizarTexto(termoBruto);
            if (termo != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("descricao")),
                        "%" + termo.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (tipoDeEmpregoId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("tipoDeEmprego").get("id"),
                        tipoDeEmpregoId
                ));
            }

            if (empresaId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("empresa", jakarta.persistence.criteria.JoinType.LEFT).get("id"),
                        empresaId
                ));
            }

            if (recrutadorId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("perfilRecrutador").get("id"),
                        recrutadorId
                ));
            }

            if (paisId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("pais").get("id"),
                        paisId
                ));
            }

            if (estadoId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("estado").get("id"),
                        estadoId
                ));
            }

            if (cidadeId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("cidade").get("id"),
                        cidadeId
                ));
            }

            if (modalidade != null) {
                predicates.add(criteriaBuilder.equal(root.get("modalidade"), modalidade));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;
    }
}
