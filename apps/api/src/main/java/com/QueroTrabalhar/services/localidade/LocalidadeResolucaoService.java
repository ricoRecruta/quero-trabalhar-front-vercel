package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra o fluxo normal de resolução de localidade durante cadastro e atualização de recursos.
 *
 * <p>Este serviço decide se o texto livre pode ser promovido imediatamente para uma localidade validada
 * ou se precisa virar {@link LocalidadePendente}. A tentativa de resolução fica separada em um fluxo puro;
 * a criação da pendência acontece apenas aqui, no caminho normal do domínio.</p>
 */
@Service
public class LocalidadeResolucaoService {

    private static final Logger logger = LoggerFactory.getLogger(LocalidadeResolucaoService.class);

    private final FluxoResolucaoLocalidadeService fluxoResolucaoLocalidadeService;
    private final RegistroLocalidadePendenteService registroLocalidadePendenteService;

    public LocalidadeResolucaoService(
            FluxoResolucaoLocalidadeService fluxoResolucaoLocalidadeService,
            RegistroLocalidadePendenteService registroLocalidadePendenteService
    ) {
        this.fluxoResolucaoLocalidadeService = fluxoResolucaoLocalidadeService;
        this.registroLocalidadePendenteService = registroLocalidadePendenteService;
    }

    /**
     * Resolve uma localidade no fluxo normal do cadastro e registra pendência quando não há validação segura.
     */
    @Transactional
    public ResultadoResolucaoLocalidade resolver(String textoLivre) {
        long inicioResolucao = System.nanoTime();
        FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade resultadoTentativa =
                fluxoResolucaoLocalidadeService.resolver(textoLivre);

        if (resultadoTentativa.resolvida()) {
            Localidade localidade = resultadoTentativa.localidadeValidada();
            if (resultadoTentativa.origemResolucao() == FluxoResolucaoLocalidadeService.OrigemResolucaoLocalidade.BASE_INTERNA) {
                logger.info(
                        "event=localidade_resolvida origem=BASE_INTERNA textoHash={} nivel={} duracaoMs={}",
                        resultadoTentativa.textoHash(),
                        resultadoTentativa.nivelLocalidade(),
                        calcularDuracaoMs(inicioResolucao)
                );
            } else {
                logger.info(
                        "event=localidade_resolvida origem=GOOGLE_MAPS textoHash={} nivel={} tentativasExecutadas={} duracaoMs={}",
                        resultadoTentativa.textoHash(),
                        resultadoTentativa.nivelLocalidade(),
                        resultadoTentativa.tentativasExecutadas(),
                        calcularDuracaoMs(inicioResolucao)
                );
            }
            return ResultadoResolucaoLocalidade.resolvida(localidade);
        }

        return criarResultadoPendente(
                resultadoTentativa.textoNormalizado(),
                resultadoTentativa.textoHash(),
                resultadoTentativa.motivoPendencia(),
                resultadoTentativa.tentativasExecutadas(),
                inicioResolucao
        );
    }

    /**
     * Executa apenas a tentativa de resolução, sem criar ou atualizar a fila de pendências.
     *
     * <p>Esse atalho existe para o reprocessamento técnico de pendências já persistidas, evitando duplicar
     * registros de {@link LocalidadePendente} para o mesmo texto.</p>
     */
    FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade resolverSemRegistrarPendencia(
            String textoLivre
    ) {
        return fluxoResolucaoLocalidadeService.resolver(textoLivre);
    }

    private ResultadoResolucaoLocalidade criarResultadoPendente(
            String textoNormalizado,
            String textoHash,
            MotivoPendenciaLocalidade motivoPendencia,
            int tentativasExecutadas,
            long inicioResolucao
    ) {
        // A localidade validada continua sendo a fonte oficial.
        // A pendência apenas preserva o texto original até uma evolução futura do fluxo.
        LocalidadePendente localidadePendente = registroLocalidadePendenteService.registrar(
                textoNormalizado,
                motivoPendencia,
                textoHash,
                tentativasExecutadas,
                inicioResolucao
        );
        return ResultadoResolucaoLocalidade.pendente(localidadePendente);
    }

    private long calcularDuracaoMs(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }
}
