package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.Empresa;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de empresas com consultas que distinguem o acervo interno do catálogo público.
 *
 * <p>No comportamento atual do MVP, a presença de {@code localidade.pais} funciona como marcador de localidade
 * validada para fins de visibilidade pública.</p>
 */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long>, JpaSpecificationExecutor<Empresa> {

    List<Empresa> findAllByOrderByNomeAsc();

    /**
     * Recupera somente empresas aptas ao catálogo público.
     */
    List<Empresa> findByLocalidadePaisIsNotNullOrderByNomeAsc();

    /**
     * Busca uma empresa por ID apenas quando sua localidade já está validada para exposição pública.
     */
    Optional<Empresa> findByIdAndLocalidadePaisIsNotNull(Long id);
}
