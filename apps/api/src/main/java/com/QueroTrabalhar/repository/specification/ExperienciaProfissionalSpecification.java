package com.QueroTrabalhar.repository.specification;

import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalFilterDTO;
import com.QueroTrabalhar.domain.entity.ExperienciaProfissional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ExperienciaProfissionalSpecification {

    private ExperienciaProfissionalSpecification() {
    }

    public static Specification<ExperienciaProfissional> comFiltros(ExperienciaProfissionalFilterDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
            }

            Join<Object, Object> tipoDeEmpregoJoin = null;

            String termo = normalizarTexto(filtro.termo());
            if (termo != null) {
                String termoLike = "%" + termo.toLowerCase(Locale.ROOT) + "%";
                tipoDeEmpregoJoin = root.join("tipoDeEmprego");
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), termoLike),
                        criteriaBuilder.like(criteriaBuilder.lower(tipoDeEmpregoJoin.get("titulo")), termoLike)
                ));
            }

            if (filtro.tipoDeEmpregoId() != null) {
                if (tipoDeEmpregoJoin == null) {
                    tipoDeEmpregoJoin = root.join("tipoDeEmprego");
                }

                predicates.add(criteriaBuilder.equal(tipoDeEmpregoJoin.get("id"), filtro.tipoDeEmpregoId()));
            }

            if (filtro.emAndamento() != null) {
                predicates.add(Boolean.TRUE.equals(filtro.emAndamento())
                        ? criteriaBuilder.isNull(root.get("dataFim"))
                        : criteriaBuilder.isNotNull(root.get("dataFim")));
            }

            if (filtro.dataInicioDe() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataInicio"), filtro.dataInicioDe()));
            }

            if (filtro.dataInicioAte() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataInicio"), filtro.dataInicioAte()));
            }

            if (filtro.dataFimDe() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dataFim"), filtro.dataFimDe()));
            }

            if (filtro.dataFimAte() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dataFim"), filtro.dataFimAte()));
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
