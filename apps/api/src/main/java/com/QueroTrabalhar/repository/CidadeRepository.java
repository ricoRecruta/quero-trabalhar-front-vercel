package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Long> {
    List<Cidade> findByEstadoAndNomeContainingIgnoreCase(Estado estado, String termoBusca);

    Optional<Cidade> findByNomeAndEstado(String s, Estado estado);

    List<Cidade> findAllByNomeIgnoreCase(String nome);

    Optional<Cidade> findFirstByNomeIgnoreCaseAndEstado(String nome, Estado estado);
}
