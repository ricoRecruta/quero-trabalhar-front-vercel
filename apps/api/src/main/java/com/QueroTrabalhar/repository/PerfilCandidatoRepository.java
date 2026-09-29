package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilCandidatoRepository extends JpaRepository<PerfilCandidato, Long> {
}