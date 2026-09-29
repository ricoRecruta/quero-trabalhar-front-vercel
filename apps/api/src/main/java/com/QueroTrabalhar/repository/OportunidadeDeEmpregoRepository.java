package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de oportunidades com consultas voltadas tanto ao acervo interno quanto ao catálogo público.
 *
 * <p>Assim como em empresa, a presença de {@code localidade.pais} é usada no MVP como marcador de localidade validada
 * para exposição pública.</p>
 */
@Repository
public interface OportunidadeDeEmpregoRepository
        extends JpaRepository<OportunidadeDeEmprego, Long>, JpaSpecificationExecutor<OportunidadeDeEmprego> {

    List<OportunidadeDeEmprego> findByEmpresaId(Long empresaId);

    /**
     * Recupera apenas oportunidades públicas de uma empresa, já restringindo a localidade a valores validados.
     */
    List<OportunidadeDeEmprego> findByEmpresaIdAndLocalidadePaisIsNotNull(Long empresaId);

    List<OportunidadeDeEmprego> findByPerfilRecrutadorId(Long perfilRecrutadorId);

    /**
     * Busca uma oportunidade por ID somente quando ela já pode ser exposta em endpoints públicos.
     */
    Optional<OportunidadeDeEmprego> findByIdAndLocalidadePaisIsNotNull(Long id);

    /**
     * Remove a associação de candidatos interessados antes da exclusão da vaga.
     *
     * <p>O método evita resíduos na tabela de relacionamento muitos-para-muitos quando o dono do recurso apaga a
     * oportunidade.</p>
     */
    @Modifying
    @Query(value = "DELETE FROM candidato_vaga_interesse " +
                   "WHERE oportunidade_id = :idVaga",  nativeQuery = true)
    void removerTodosInteressesDaVaga(@Param("idVaga") Long idVaga);
}
