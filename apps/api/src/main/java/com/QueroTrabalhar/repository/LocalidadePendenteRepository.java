package com.QueroTrabalhar.repository;

import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso de persistência para a fila técnica de {@link LocalidadePendente}.
 *
 * <p>Os métodos ordenados por atualização/criação atendem dois cenários centrais do módulo:
 * recuperar a pendência mais recente de um recurso específico e reprocessar a fila aberta
 * em ordem previsível.</p>
 */
@Repository
public interface LocalidadePendenteRepository extends JpaRepository<LocalidadePendente, Long> {

    List<LocalidadePendente> findByStatusValidacao(StatusValidacaoLocalidade statusValidacao);

    List<LocalidadePendente> findByStatusValidacaoOrderByAtualizadaEmAscCriadaEmAsc(
            StatusValidacaoLocalidade statusValidacao
    );

    Optional<LocalidadePendente> findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    );

    List<LocalidadePendente> findByTipoRecursoAndCampoAlvoAndRecursoIdInOrderByRecursoIdAscAtualizadaEmDescCriadaEmDesc(
            TipoRecursoLocalidadePendente tipoRecurso,
            CampoLocalidadePendente campoAlvo,
            List<Long> recursoIds
    );

    default Optional<LocalidadePendente> findFirstByTipoRecursoAndRecursoIdAndCampoAlvo(
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    ) {
        return findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                tipoRecurso,
                recursoId,
                campoAlvo
        );
    }

    default List<LocalidadePendente> findByTipoRecursoAndCampoAlvoAndRecursoIdIn(
            TipoRecursoLocalidadePendente tipoRecurso,
            CampoLocalidadePendente campoAlvo,
            List<Long> recursoIds
    ) {
        return findByTipoRecursoAndCampoAlvoAndRecursoIdInOrderByRecursoIdAscAtualizadaEmDescCriadaEmDesc(
                tipoRecurso,
                campoAlvo,
                recursoIds
        );
    }

    /**
     * Retorna a fila atualmente aberta para reprocessamento técnico.
     */
    default List<LocalidadePendente> findPendenciasAbertas() {
        return findByStatusValidacaoOrderByAtualizadaEmAscCriadaEmAsc(
                StatusValidacaoLocalidade.PENDENTE_VALIDACAO
        );
    }
}
