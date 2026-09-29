package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.Preferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteresseEmEmpregoRepository extends JpaRepository<Preferencia, Long> {

}
