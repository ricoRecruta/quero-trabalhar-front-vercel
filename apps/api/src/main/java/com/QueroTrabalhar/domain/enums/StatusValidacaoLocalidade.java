package com.QueroTrabalhar.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa o estado técnico de validação associado a uma localidade pendente.
 *
 * <p>Esses status não modelam um estado público normal do domínio. Eles existem para controlar a fila
 * de resolução e, quando necessário, permitir que o próprio dono do recurso veja a situação em fluxos
 * autenticados, como consultas do tipo {@code /me}.</p>
 *
 * <p>No MVP atual, os estados operacionais relevantes para a API são os que mantêm a pendência em aberto
 * ou registram falha de validação. Os demais valores permanecem reservados para evolução futura em fluxos
 * autenticados e não representam confirmação manual já disponível para uso público.</p>
 */
@Schema(
        name = "StatusValidacaoLocalidade",
        description = "Estado técnico da validação interna de uma localidade pendente. No MVP atual, os estados de confirmação ou recusa manual permanecem reservados para evolução futura em fluxos autenticados e não representam funcionalidade pública disponível."
)
public enum StatusValidacaoLocalidade {
    /** Pendência aberta aguardando nova tentativa automática ou tratamento futuro. */
    PENDENTE_VALIDACAO,
    /** Reservado para um fluxo pós-MVP em que uma sugestão será apresentada ao usuário. */
    SUGESTAO_ENCONTRADA,
    /** Reservado para confirmação manual pós-MVP da localidade sugerida. */
    CONFIRMADA_PELO_USUARIO,
    /** Reservado para recusa manual pós-MVP da localidade sugerida. */
    RECUSADA_PELO_USUARIO,
    /** Indica que a validação não avançou para uma localidade oficial confirmada. */
    FALHA_VALIDACAO
}
