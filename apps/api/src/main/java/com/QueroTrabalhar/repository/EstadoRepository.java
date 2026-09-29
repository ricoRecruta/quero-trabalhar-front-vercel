package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {
    List<Estado> findByPaisAndNomeContainingIgnoreCase(Pais pais, String termoBusca);

    Optional<Estado> findByNomeAndPais(String s, Pais pais);

    List<Estado> findAllByNomeIgnoreCase(String nome);

    Optional<Estado> findFirstByNomeIgnoreCaseAndPais(String nome, Pais pais);
}
