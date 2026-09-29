package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.Indicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicacaoRepository extends JpaRepository<Indicacao, Long> {
    List<Indicacao> findByAutorId(Long autorId);

    List<Indicacao> findByUsuarioIndicadoId(Long usuarioIndicadoId);
}
