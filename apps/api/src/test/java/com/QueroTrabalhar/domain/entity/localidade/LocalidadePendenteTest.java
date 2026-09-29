package com.QueroTrabalhar.domain.entity.localidade;

import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.OrigemLocalidade;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalidadePendenteTest {

    @Test
    void deveCriarLocalidadePendenteInformadaPeloUsuario() {
        String textoOriginal = "Sao Jose dos Campos";
        String motivoPendencia = "Localidade aguardando validacao manual";

        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario(textoOriginal, motivoPendencia);

        assertAll(
                () -> assertEquals("Sao Jose dos Campos", localidadePendente.getTextoOriginal()),
                () -> assertEquals("Localidade aguardando validacao manual", localidadePendente.getMotivoPendencia()),
                () -> assertEquals(StatusValidacaoLocalidade.PENDENTE_VALIDACAO, localidadePendente.getStatusValidacao()),
                () -> assertEquals(OrigemLocalidade.USUARIO, localidadePendente.getOrigem()),
                () -> assertNull(localidadePendente.getTipoRecurso()),
                () -> assertNull(localidadePendente.getRecursoId()),
                () -> assertNull(localidadePendente.getCampoAlvo()),
                () -> assertNotNull(localidadePendente.getCriadaEm()),
                () -> assertNotNull(localidadePendente.getAtualizadaEm())
        );
    }

    @Test
    void deveCriarLocalidadePendenteComDonoGenerico() {
        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Campina Grande",
                "Aguardando confirmacao",
                TipoRecursoLocalidadePendente.EMPRESA,
                10L,
                CampoLocalidadePendente.LOCALIDADE
        );

        assertAll(
                () -> assertTrue(localidadePendente.possuiDonoGenerico()),
                () -> assertEquals(TipoRecursoLocalidadePendente.EMPRESA, localidadePendente.getTipoRecurso()),
                () -> assertEquals(10L, localidadePendente.getRecursoId()),
                () -> assertEquals(CampoLocalidadePendente.LOCALIDADE, localidadePendente.getCampoAlvo())
        );
    }

    @Test
    void deveNormalizarTextoOriginalComTrim() {
        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario("  Belo Horizonte  ", "Motivo valido");

        assertEquals("Belo Horizonte", localidadePendente.getTextoOriginal());
    }

    @Test
    void deveNormalizarMotivoPendenciaComTrim() {
        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario(
                        "Campina Grande",
                        "  Nao foi possivel validar automaticamente a cidade informada.  "
                );

        assertEquals(
                "Nao foi possivel validar automaticamente a cidade informada.",
                localidadePendente.getMotivoPendencia()
        );
    }

    @Test
    void deveConverterMotivoPendenciaVazioParaNull() {
        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario("Joao Pessoa", "");

        assertNull(localidadePendente.getMotivoPendencia());
    }

    @Test
    void deveBloquearTextoOriginalNulo() {
        assertThrows(
                BusinessRuleException.class,
                () -> LocalidadePendente.criarPendenteInformadaPeloUsuario(null, "Motivo opcional")
        );
    }

    @Test
    void deveBloquearTextoOriginalVazio() {
        assertThrows(
                BusinessRuleException.class,
                () -> LocalidadePendente.criarPendenteInformadaPeloUsuario("   ", "Motivo opcional")
        );
    }

    @Test
    void deveBloquearStatusNuloNoConstrutor() {
        assertThrows(
                BusinessRuleException.class,
                () -> new LocalidadePendente("Recife", null, OrigemLocalidade.USUARIO, "Motivo opcional")
        );
    }

    @Test
    void deveBloquearOrigemNulaNoConstrutor() {
        assertThrows(
                BusinessRuleException.class,
                () -> new LocalidadePendente(
                        "Recife",
                        StatusValidacaoLocalidade.PENDENTE_VALIDACAO,
                        null,
                        "Motivo opcional"
                )
        );
    }

    @Test
    void deveBloquearDonoGenericoIncompletoNoConstrutor() {
        assertThrows(
                BusinessRuleException.class,
                () -> new LocalidadePendente(
                        "Recife",
                        StatusValidacaoLocalidade.PENDENTE_VALIDACAO,
                        OrigemLocalidade.USUARIO,
                        "Motivo opcional",
                        TipoRecursoLocalidadePendente.EMPRESA,
                        null,
                        CampoLocalidadePendente.LOCALIDADE
                )
        );
    }

    @Test
    void deveBloquearDonoGenericoComRecursoIdZeroOuNegativo() {
        assertAll(
                () -> assertThrows(
                        BusinessRuleException.class,
                        () -> new LocalidadePendente(
                                "Recife",
                                StatusValidacaoLocalidade.PENDENTE_VALIDACAO,
                                OrigemLocalidade.USUARIO,
                                "Motivo opcional",
                                TipoRecursoLocalidadePendente.EMPRESA,
                                0L,
                                CampoLocalidadePendente.LOCALIDADE
                        )
                ),
                () -> assertThrows(
                        BusinessRuleException.class,
                        () -> new LocalidadePendente(
                                "Recife",
                                StatusValidacaoLocalidade.PENDENTE_VALIDACAO,
                                OrigemLocalidade.USUARIO,
                                "Motivo opcional",
                                TipoRecursoLocalidadePendente.EMPRESA,
                                -1L,
                                CampoLocalidadePendente.LOCALIDADE
                        )
                )
        );
    }

    @Test
    void deveAtualizarTextoOriginalComNormalizacao() {
        LocalidadePendente localidadePendente = criarLocalidadePendenteValida();

        localidadePendente.setTextoOriginal("  Mogi das Cruzes  ");

        assertEquals("Mogi das Cruzes", localidadePendente.getTextoOriginal());
    }

    @Test
    void deveBloquearSetTextoOriginalVazio() {
        LocalidadePendente localidadePendente = criarLocalidadePendenteValida();

        assertThrows(
                BusinessRuleException.class,
                () -> localidadePendente.setTextoOriginal("")
        );
    }

    @Test
    void devePermitirAssociarDonoGenericoDepoisDaCriacao() {
        LocalidadePendente localidadePendente = criarLocalidadePendenteValida();

        localidadePendente.definirDonoGenerico(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                25L,
                CampoLocalidadePendente.LOCALIDADE
        );

        assertAll(
                () -> assertEquals(TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO, localidadePendente.getTipoRecurso()),
                () -> assertEquals(25L, localidadePendente.getRecursoId()),
                () -> assertEquals(CampoLocalidadePendente.LOCALIDADE, localidadePendente.getCampoAlvo())
        );
    }

    private LocalidadePendente criarLocalidadePendenteValida() {
        return new LocalidadePendente(
                "Curitiba",
                StatusValidacaoLocalidade.PENDENTE_VALIDACAO,
                OrigemLocalidade.USUARIO,
                "Aguardando analise"
        );
    }
}
