package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.localidade.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<Pais, Long> {

    List<Pais> findByNomeContainingIgnoreCase(String termoBusca);

    Optional<Pais> findBySigla(String sigla);

    Optional<Pais> findFirstByNomeIgnoreCase(String nome);

    Optional<Pais> findFirstBySiglaIgnoreCase(String sigla);
}
