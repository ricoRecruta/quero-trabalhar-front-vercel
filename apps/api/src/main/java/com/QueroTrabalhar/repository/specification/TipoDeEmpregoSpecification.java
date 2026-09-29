package com.QueroTrabalhar.repository.specification;

import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TipoDeEmpregoSpecification {

    private TipoDeEmpregoSpecification() {
    }

    public static Specification<TipoDeEmprego> comFiltros(TipoDeEmpregoFilterDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("aprovado")));

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
            }

            String termo = normalizarTexto(filtro.termo());
            if (termo != null) {
                String termoLike = "%" + termo.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), termoLike),
                        criteriaBuilder.like(root.get("descricao"), "%" + termo + "%")
                ));
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
