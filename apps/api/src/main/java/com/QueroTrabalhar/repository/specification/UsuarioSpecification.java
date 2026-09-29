package com.QueroTrabalhar.repository.specification;

import com.QueroTrabalhar.domain.dtos.usuario.UsuarioFilterDTO;
import com.QueroTrabalhar.domain.entity.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> comFiltros(UsuarioFilterDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
            }

            String termo = normalizarTexto(filtro.termo());
            if (termo != null) {
                String termoLike = "%" + termo.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), termoLike),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), termoLike),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("cpf")), termoLike)
                ));
            }

            String nome = normalizarTexto(filtro.nome());
            if (nome != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            String email = normalizarTexto(filtro.email());
            if (email != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            String cpf = normalizarTexto(filtro.cpf());
            if (cpf != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("cpf")),
                        "%" + cpf.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (filtro.temPerfilCandidato() != null) {
                Join<Object, Object> perfilCandidatoJoin = root.join("perfilCandidato", JoinType.LEFT);
                predicates.add(Boolean.TRUE.equals(filtro.temPerfilCandidato())
                        ? criteriaBuilder.isNotNull(perfilCandidatoJoin.get("id"))
                        : criteriaBuilder.isNull(perfilCandidatoJoin.get("id")));
            }

            if (filtro.temPerfilRecrutador() != null) {
                Join<Object, Object> perfilRecrutadorJoin = root.join("perfilRecrutador", JoinType.LEFT);
                predicates.add(Boolean.TRUE.equals(filtro.temPerfilRecrutador())
                        ? criteriaBuilder.isNotNull(perfilRecrutadorJoin.get("id"))
                        : criteriaBuilder.isNull(perfilRecrutadorJoin.get("id")));
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
