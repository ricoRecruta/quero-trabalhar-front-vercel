package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Centraliza a criação e atualização da fila técnica de {@link LocalidadePendente}.
 *
 * <p>O objetivo deste serviço é impedir que a pendência seja criada de forma espalhada pelo domínio.
 * A entidade de negócio continua carregando apenas a localidade validada, enquanto a fila técnica
 * registra o texto pendente e, quando necessário, a associação genérica com o recurso dono.</p>
 */
@Service
public class RegistroLocalidadePendenteService {

    private static final Logger logger = LoggerFactory.getLogger(RegistroLocalidadePendenteService.class);

    private final LocalidadePendenteRepository localidadePendenteRepository;

    public RegistroLocalidadePendenteService(LocalidadePendenteRepository localidadePendenteRepository) {
        this.localidadePendenteRepository = localidadePendenteRepository;
    }

    /**
     * Registra uma pendência sem associá-la imediatamente a um recurso específico.
     */
    public LocalidadePendente registrar(
            String textoNormalizado,
            MotivoPendenciaLocalidade motivoPendencia,
            String textoHash,
            int tentativasExecutadas,
            long inicioResolucao
    ) {
        return registrar(
                textoNormalizado,
                motivoPendencia,
                null,
                null,
                null,
                textoHash,
                tentativasExecutadas,
                inicioResolucao
        );
    }

    /**
     * Registra uma nova pendência já vinculada ao recurso que falhou na validação.
     *
     * <p>Este é o ponto que materializa a fila técnica temporária. O registro preserva o texto original
     * para reprocessamento futuro, mas não transforma a pendência em estado público normal do recurso.</p>
     */
    public LocalidadePendente registrar(
            String textoNormalizado,
            MotivoPendenciaLocalidade motivoPendencia,
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo,
            String textoHash,
            int tentativasExecutadas,
            long inicioResolucao
    ) {
        // A pendência continua sendo um fallback técnico interno e não substitui a localidade validada.
        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                textoNormalizado,
                motivoPendencia.descricao(),
                tipoRecurso,
                recursoId,
                campoAlvo
        );
        LocalidadePendente localidadePendenteSalva = localidadePendenteRepository.save(localidadePendente);

        logger.warn(
                "event=localidade_pendente_criada textoHash={} motivoCategoria={} tentativasExecutadas={} statusValidacao={} origem={} tipoRecurso={} recursoId={} campoAlvo={} duracaoMs={}",
                textoHash,
                motivoPendencia.categoriaLog(),
                tentativasExecutadas,
                localidadePendenteSalva.getStatusValidacao(),
                localidadePendenteSalva.getOrigem(),
                localidadePendenteSalva.getTipoRecurso(),
                localidadePendenteSalva.getRecursoId(),
                localidadePendenteSalva.getCampoAlvo(),
                calcularDuracaoMs(inicioResolucao)
        );

        return localidadePendenteSalva;
    }

    /**
     * Associa a pendência existente ao recurso dono por tipo, id e campo alvo.
     *
     * <p>Esse vínculo genérico permite que o próprio dono enxergue o status da pendência em fluxos
     * autenticados e que o reprocessamento consiga reencontrar o recurso sem criar dependência direta
     * da entidade de negócio para {@link LocalidadePendente}.</p>
     */
    public LocalidadePendente associarDonoGenerico(
            LocalidadePendente localidadePendente,
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    ) {
        if (localidadePendente.possuiDonoGenerico()
                && localidadePendente.getTipoRecurso() == tipoRecurso
                && Objects.equals(recursoId, localidadePendente.getRecursoId())
                && localidadePendente.getCampoAlvo() == campoAlvo) {
            return localidadePendente;
        }

        // Mantemos o FK legado nas entidades durante a migração, mas a pendência agora conhece
        // o recurso dono para preparar o reprocessamento técnico sem acoplá-lo ao modelo público.
        localidadePendente.definirDonoGenerico(tipoRecurso, recursoId, campoAlvo);
        return localidadePendenteRepository.save(localidadePendente);
    }

    private long calcularDuracaoMs(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }
}
