package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Reprocessa pendências já persistidas tentando promovê-las para localidade validada.
 *
 * <p>Diferente do fluxo normal de cadastro, este serviço nunca cria uma nova {@link LocalidadePendente}.
 * Ele reutiliza a pendência existente, executa novamente a tentativa pura de resolução e, conforme o
 * resultado, ou aplica a localidade oficial ao recurso dono e remove a fila, ou apenas atualiza o motivo
 * técnico da pendência.</p>
 *
 * <p>O reencontro com o recurso é feito pelo trio tipo/id/campo porque {@code Empresa} e
 * {@code OportunidadeDeEmprego} não referenciam a pendência diretamente.</p>
 */
@Service
public class LocalidadePendenteReprocessamentoService {

    private static final Logger logger = LoggerFactory.getLogger(LocalidadePendenteReprocessamentoService.class);

    private final LocalidadePendenteRepository localidadePendenteRepository;
    private final EmpresaRepository empresaRepository;
    private final OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;
    private final FluxoResolucaoLocalidadeService fluxoResolucaoLocalidadeService;
    private final TransactionTemplate transactionTemplate;

    public LocalidadePendenteReprocessamentoService(
            LocalidadePendenteRepository localidadePendenteRepository,
            EmpresaRepository empresaRepository,
            OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository,
            FluxoResolucaoLocalidadeService fluxoResolucaoLocalidadeService,
            PlatformTransactionManager transactionManager
    ) {
        this.localidadePendenteRepository = localidadePendenteRepository;
        this.empresaRepository = empresaRepository;
        this.oportunidadeDeEmpregoRepository = oportunidadeDeEmpregoRepository;
        this.fluxoResolucaoLocalidadeService = fluxoResolucaoLocalidadeService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Lista a fila técnica aberta na ordem usada pelo reprocessamento.
     */
    @Transactional(readOnly = true)
    public List<LocalidadePendente> buscarPendenciasAbertas() {
        return localidadePendenteRepository.findPendenciasAbertas();
    }

    /**
     * Reprocessa todas as pendências abertas sem criar novos registros para o mesmo problema.
     */
    public List<ResultadoReprocessamentoPendencia> reprocessarPendenciasAbertas() {
        List<Long> pendenciaIds = buscarPendenciasAbertas().stream()
                .map(LocalidadePendente::getId)
                .filter(Objects::nonNull)
                .toList();

        List<ResultadoReprocessamentoPendencia> resultados = new ArrayList<>(pendenciaIds.size());
        for (Long pendenciaId : pendenciaIds) {
            resultados.add(reprocessarPendencia(pendenciaId));
        }
        return List.copyOf(resultados);
    }

    /**
     * Reprocessa uma pendência específica em transação isolada.
     */
    public ResultadoReprocessamentoPendencia reprocessarPendencia(Long pendenciaId) {
        try {
            return Objects.requireNonNull(
                    transactionTemplate.execute(status -> reprocessarPendenciaEmTransacao(pendenciaId))
            );
        } catch (RuntimeException exception) {
            logger.error(
                    "event=localidade_pendente_reprocessamento_falha pendenciaId={} excecao={}",
                    pendenciaId,
                    exception.getClass().getSimpleName(),
                    exception
            );
            return ResultadoReprocessamentoPendencia.falha(pendenciaId);
        }
    }

    private ResultadoReprocessamentoPendencia reprocessarPendenciaEmTransacao(Long pendenciaId) {
        Optional<LocalidadePendente> pendenciaOptional = localidadePendenteRepository.findById(pendenciaId);
        if (pendenciaOptional.isEmpty()) {
            logger.warn(
                    "event=localidade_pendente_reprocessamento_falha pendenciaId={} motivo=PENDENCIA_NAO_ENCONTRADA",
                    pendenciaId
            );
            return ResultadoReprocessamentoPendencia.falha(pendenciaId);
        }

        LocalidadePendente pendencia = pendenciaOptional.get();
        logger.info(
                "event=localidade_pendente_reprocessamento_iniciado pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={} statusValidacao={}",
                pendencia.getId(),
                pendencia.getTipoRecurso(),
                pendencia.getRecursoId(),
                pendencia.getCampoAlvo(),
                pendencia.getStatusValidacao()
        );

        ResultadoLocalizacaoDono resultadoDono = localizarDono(pendencia);
        if (resultadoDono.resultado() != null) {
            return resultadoDono.resultado();
        }

        FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade resultadoTentativa =
                // O reprocessamento reaproveita o fluxo puro para não duplicar LocalidadePendente.
                fluxoResolucaoLocalidadeService.resolver(pendencia.getTextoOriginal());

        if (resultadoTentativa.resolvida()) {
            return aplicarLocalidadeResolvida(pendencia, resultadoDono.donoRecurso(), resultadoTentativa);
        }

        atualizarMotivoPendencia(pendencia, resultadoTentativa.motivoPendencia());
        localidadePendenteRepository.save(pendencia);
        logger.info(
                "event=localidade_pendente_reprocessamento_mantida pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={} textoHash={} motivoCategoria={} tentativasExecutadas={}",
                pendencia.getId(),
                pendencia.getTipoRecurso(),
                pendencia.getRecursoId(),
                pendencia.getCampoAlvo(),
                resultadoTentativa.textoHash(),
                resultadoTentativa.motivoPendencia().categoriaLog(),
                resultadoTentativa.tentativasExecutadas()
        );
        return ResultadoReprocessamentoPendencia.mantida(pendencia.getId());
    }

    private ResultadoReprocessamentoPendencia aplicarLocalidadeResolvida(
            LocalidadePendente pendencia,
            DonoRecurso donoRecurso,
            FluxoResolucaoLocalidadeService.ResultadoTentativaResolucaoLocalidade resultadoTentativa
    ) {
        donoRecurso.aplicarLocalidadeValidada(resultadoTentativa.localidadeValidada());
        localidadePendenteRepository.delete(pendencia);
        logger.info(
                "event=localidade_pendente_reprocessamento_resolvida pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={} textoHash={} origem={} nivel={} tentativasExecutadas={} acao=REMOVIDA",
                pendencia.getId(),
                pendencia.getTipoRecurso(),
                pendencia.getRecursoId(),
                pendencia.getCampoAlvo(),
                resultadoTentativa.textoHash(),
                resultadoTentativa.origemResolucao(),
                resultadoTentativa.nivelLocalidade(),
                resultadoTentativa.tentativasExecutadas()
        );
        return ResultadoReprocessamentoPendencia.resolvida(pendencia.getId());
    }

    private ResultadoLocalizacaoDono localizarDono(LocalidadePendente pendencia) {
        if (!pendencia.possuiDonoGenerico()) {
            logger.warn(
                    "event=localidade_pendente_reprocessamento_sem_dono pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={} motivo=DONO_NAO_INFORMADO",
                    pendencia.getId(),
                    pendencia.getTipoRecurso(),
                    pendencia.getRecursoId(),
                    pendencia.getCampoAlvo()
            );
            return ResultadoLocalizacaoDono.comResultado(
                    ResultadoReprocessamentoPendencia.semDono(pendencia.getId())
            );
        }

        if (pendencia.getCampoAlvo() != CampoLocalidadePendente.LOCALIDADE) {
            logger.warn(
                    "event=localidade_pendente_reprocessamento_tipo_recurso_nao_suportado pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={}",
                    pendencia.getId(),
                    pendencia.getTipoRecurso(),
                    pendencia.getRecursoId(),
                    pendencia.getCampoAlvo()
            );
            return ResultadoLocalizacaoDono.comResultado(
                    ResultadoReprocessamentoPendencia.tipoRecursoNaoSuportado(pendencia.getId())
            );
        }

        if (pendencia.getTipoRecurso() == null) {
            logger.warn(
                    "event=localidade_pendente_reprocessamento_tipo_recurso_nao_suportado pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={}",
                    pendencia.getId(),
                    pendencia.getTipoRecurso(),
                    pendencia.getRecursoId(),
                    pendencia.getCampoAlvo()
            );
            return ResultadoLocalizacaoDono.comResultado(
                    ResultadoReprocessamentoPendencia.tipoRecursoNaoSuportado(pendencia.getId())
            );
        }

        return switch (pendencia.getTipoRecurso()) {
            case EMPRESA -> empresaRepository.findById(pendencia.getRecursoId())
                    .map(this::criarDonoEmpresa)
                    .map(ResultadoLocalizacaoDono::comDono)
                    .orElseGet(() -> {
                        logger.warn(
                                "event=localidade_pendente_reprocessamento_sem_dono pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={} motivo=RECURSO_INEXISTENTE",
                                pendencia.getId(),
                                pendencia.getTipoRecurso(),
                                pendencia.getRecursoId(),
                                pendencia.getCampoAlvo()
                        );
                        return ResultadoLocalizacaoDono.comResultado(
                                ResultadoReprocessamentoPendencia.semDono(pendencia.getId())
                        );
                    });
            case OPORTUNIDADE_DE_EMPREGO -> oportunidadeDeEmpregoRepository.findById(pendencia.getRecursoId())
                    .map(this::criarDonoOportunidade)
                    .map(ResultadoLocalizacaoDono::comDono)
                    .orElseGet(() -> {
                        logger.warn(
                                "event=localidade_pendente_reprocessamento_sem_dono pendenciaId={} tipoRecurso={} recursoId={} campoAlvo={} motivo=RECURSO_INEXISTENTE",
                                pendencia.getId(),
                                pendencia.getTipoRecurso(),
                                pendencia.getRecursoId(),
                                pendencia.getCampoAlvo()
                        );
                        return ResultadoLocalizacaoDono.comResultado(
                                ResultadoReprocessamentoPendencia.semDono(pendencia.getId())
                        );
                    });
        };
    }

    private void atualizarMotivoPendencia(
            LocalidadePendente pendencia,
            MotivoPendenciaLocalidade motivoPendencia
    ) {
        pendencia.setMotivoPendencia(motivoPendencia.descricao());
    }

    private DonoRecurso criarDonoEmpresa(Empresa empresa) {
        return new DonoRecurso(localidade -> {
            empresa.definirLocalidadeValidada(localidade);
            empresaRepository.save(empresa);
        });
    }

    private DonoRecurso criarDonoOportunidade(OportunidadeDeEmprego oportunidadeDeEmprego) {
        return new DonoRecurso(localidade -> {
            oportunidadeDeEmprego.definirLocalidadeValidada(localidade);
            oportunidadeDeEmpregoRepository.save(oportunidadeDeEmprego);
        });
    }

    private record DonoRecurso(Consumer<Localidade> atualizador) {

        private void aplicarLocalidadeValidada(Localidade localidade) {
            atualizador.accept(localidade);
        }
    }

    private record ResultadoLocalizacaoDono(
            ResultadoReprocessamentoPendencia resultado,
            DonoRecurso donoRecurso
    ) {

        private static ResultadoLocalizacaoDono comResultado(ResultadoReprocessamentoPendencia resultado) {
            return new ResultadoLocalizacaoDono(resultado, null);
        }

        private static ResultadoLocalizacaoDono comDono(DonoRecurso donoRecurso) {
            return new ResultadoLocalizacaoDono(null, donoRecurso);
        }
    }

    /**
     * Estados técnicos possíveis ao fim de uma rodada de reprocessamento.
     */
    enum StatusReprocessamentoPendencia {
        RESOLVIDA,
        MANTIDA,
        SEM_DONO,
        TIPO_RECURSO_NAO_SUPORTADO,
        FALHA
    }

    /**
     * Resultado técnico do reprocessamento de uma pendência específica.
     */
    public record ResultadoReprocessamentoPendencia(
            Long pendenciaId,
            StatusReprocessamentoPendencia status
    ) {

        static ResultadoReprocessamentoPendencia resolvida(Long pendenciaId) {
            return new ResultadoReprocessamentoPendencia(
                    pendenciaId,
                    StatusReprocessamentoPendencia.RESOLVIDA
            );
        }

        static ResultadoReprocessamentoPendencia mantida(Long pendenciaId) {
            return new ResultadoReprocessamentoPendencia(
                    pendenciaId,
                    StatusReprocessamentoPendencia.MANTIDA
            );
        }

        static ResultadoReprocessamentoPendencia semDono(Long pendenciaId) {
            return new ResultadoReprocessamentoPendencia(
                    pendenciaId,
                    StatusReprocessamentoPendencia.SEM_DONO
            );
        }

        static ResultadoReprocessamentoPendencia tipoRecursoNaoSuportado(Long pendenciaId) {
            return new ResultadoReprocessamentoPendencia(
                    pendenciaId,
                    StatusReprocessamentoPendencia.TIPO_RECURSO_NAO_SUPORTADO
            );
        }

        static ResultadoReprocessamentoPendencia falha(Long pendenciaId) {
            return new ResultadoReprocessamentoPendencia(
                    pendenciaId,
                    StatusReprocessamentoPendencia.FALHA
            );
        }
    }
}
