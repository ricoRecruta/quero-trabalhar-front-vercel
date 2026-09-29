package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.ExperienciaProfissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienciaProfissionalRepository extends
        JpaRepository<ExperienciaProfissional, Long>,
        JpaSpecificationExecutor<ExperienciaProfissional> {
}
