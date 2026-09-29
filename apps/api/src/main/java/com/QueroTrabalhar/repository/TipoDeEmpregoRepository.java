package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDeEmpregoRepository extends JpaRepository<TipoDeEmprego, Long>, JpaSpecificationExecutor<TipoDeEmprego> {
    Optional<TipoDeEmprego> findByTitulo(String titulo); // Busca por Titulo

    boolean existsByTitulo(String titulo);

    List<TipoDeEmprego> findByAprovadoTrue();

    List<TipoDeEmprego> findByAprovadoFalse();

    Optional<TipoDeEmprego> findByTituloIgnoreCase(String titulo);

    //Aprovação em lote
    @Modifying // Diz ao Spring que isso é um UPDATE/DELETE e não um SELECT
    @Query("UPDATE TipoDeEmprego " +
            "t SET t.aprovado = true " +
            "WHERE t.id IN :ids")
    int aprovarEmLote(@Param("ids") List<Long> ids);
}
