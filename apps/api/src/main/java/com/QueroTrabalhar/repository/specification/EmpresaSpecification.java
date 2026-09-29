package com.QueroTrabalhar.repository.specification;

import com.QueroTrabalhar.domain.dtos.empresa.EmpresaFilterDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Centraliza os filtros do catálogo público de empresas.
 *
 * <p>A specification sempre injeta a restrição de localidade validada antes de aplicar filtros opcionais de texto ou
 * recorte geográfico. Isso garante que empresas pendentes não escapem para endpoints públicos por combinação de
 * parâmetros.</p>
 */
public final class EmpresaSpecification {

    private EmpresaSpecification() {
    }

    /**
     * Monta os predicados da listagem pública de empresas.
     */
    public static Specification<Empresa> comFiltros(EmpresaFilterDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNotNull(root.get("localidade").get("pais")));

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
            }

            String termo = normalizarTexto(filtro.termo());
            if (termo != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + termo.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (filtro.paisId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("pais").get("id"),
                        filtro.paisId()
                ));
            }

            if (filtro.estadoId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("estado").get("id"),
                        filtro.estadoId()
                ));
            }

            if (filtro.cidadeId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("localidade").get("cidade").get("id"),
                        filtro.cidadeId()
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
