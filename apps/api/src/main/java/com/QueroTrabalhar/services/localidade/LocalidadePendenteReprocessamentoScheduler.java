package com.QueroTrabalhar.services.localidade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

/**
 * Dispara periodicamente o reprocessamento da fila de localidades pendentes.
 *
 * <p>O scheduler não contém regra de negócio. Sua responsabilidade é apenas verificar se a rotina
 * está habilitada, delegar a execução ao serviço apropriado e registrar métricas operacionais do lote.</p>
 */
@Component
public class LocalidadePendenteReprocessamentoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LocalidadePendenteReprocessamentoScheduler.class);

    private final LocalidadePendenteReprocessamentoService localidadePendenteReprocessamentoService;
    private final boolean reprocessamentoEnabled;

    public LocalidadePendenteReprocessamentoScheduler(
            LocalidadePendenteReprocessamentoService localidadePendenteReprocessamentoService,
            @Value("${localidade.pendente.reprocessamento.enabled:true}") boolean reprocessamentoEnabled
    ) {
        this.localidadePendenteReprocessamentoService = localidadePendenteReprocessamentoService;
        this.reprocessamentoEnabled = reprocessamentoEnabled;
    }

    /**
     * Aciona uma rodada de reprocessamento técnico das pendências abertas.
     */
    @Scheduled(fixedDelayString = "${localidade.pendente.reprocessamento.fixed-delay-ms:600000}")
    public void reprocessarPendenciasAbertas() {
        if (!reprocessamentoEnabled) {
            logger.debug("event=localidade_pendente_reprocessamento_scheduler_ignorado enabled=false");
            return;
        }

        long inicioExecucao = System.nanoTime();
        logger.info("event=localidade_pendente_reprocessamento_scheduler_iniciado");

        try {
            List<LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia> resultados =
                    localidadePendenteReprocessamentoService.reprocessarPendenciasAbertas();
            EnumMap<LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia, Long> contagens =
                    inicializarContagens();

            for (LocalidadePendenteReprocessamentoService.ResultadoReprocessamentoPendencia resultado : resultados) {
                contagens.merge(resultado.status(), 1L, Long::sum);
            }

            logger.info(
                    "event=localidade_pendente_reprocessamento_scheduler_finalizado processadas={} resolvidas={} mantidas={} semDono={} tipoRecursoNaoSuportado={} falhas={} duracaoMs={}",
                    resultados.size(),
                    contagens.get(LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.RESOLVIDA),
                    contagens.get(LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.MANTIDA),
                    contagens.get(LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.SEM_DONO),
                    contagens.get(LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.TIPO_RECURSO_NAO_SUPORTADO),
                    contagens.get(LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.FALHA),
                    calcularDuracaoMs(inicioExecucao)
            );
        } catch (Exception exception) {
            logger.error(
                    "event=localidade_pendente_reprocessamento_scheduler_falha_geral excecao={} duracaoMs={}",
                    exception.getClass().getSimpleName(),
                    calcularDuracaoMs(inicioExecucao),
                    exception
            );
        }
    }

    private EnumMap<LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia, Long> inicializarContagens() {
        EnumMap<LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia, Long> contagens =
                new EnumMap<>(LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.class);

        for (LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia status :
                LocalidadePendenteReprocessamentoService.StatusReprocessamentoPendencia.values()) {
            contagens.put(status, 0L);
        }

        return contagens;
    }

    private long calcularDuracaoMs(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }
}
