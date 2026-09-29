package com.QueroTrabalhar.services.localidade;

enum MotivoPendenciaLocalidade {
    LOCALIDADE_NAO_ENCONTRADA(
            "Localidade não encontrada na base interna ou na integração externa.",
            "LOCALIDADE_NAO_ENCONTRADA"
    ),
    FALHA_INTEGRACAO_EXTERNA(
            "Não foi possível validar a localidade na integração externa.",
            "FALHA_INTEGRACAO_EXTERNA"
    ),
    LOCALIDADE_AMBIGUA_BASE_INTERNA(
            "A localidade informada corresponde a múltiplas opções na base interna e precisa de confirmação do usuário.",
            "AMBIGUIDADE_BASE_INTERNA"
    ),
    LOCALIDADE_AMBIGUA_GOOGLE(
            "A localidade informada retornou múltiplas opções no Google Maps e precisa de confirmação do usuário.",
            "AMBIGUIDADE_GOOGLE_MAPS"
    );

    private final String descricao;
    private final String categoriaLog;

    MotivoPendenciaLocalidade(String descricao, String categoriaLog) {
        this.descricao = descricao;
        this.categoriaLog = categoriaLog;
    }

    String descricao() {
        return descricao;
    }

    String categoriaLog() {
        return categoriaLog;
    }
}
