package com.QueroTrabalhar.services.localidade;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultadoResolucaoLocalidadeTest {

    @Test
    void deveCriarResultadoResolvidoComLocalidadeValidada() {
        // Arrange
        Localidade localidade = new Localidade(criarPais(1L, "Brasil", "BR"));

        // Act
        ResultadoResolucaoLocalidade resultado = ResultadoResolucaoLocalidade.resolvida(localidade);

        // Assert
        assertAll(
                () -> assertTrue(resultado.resolvida()),
                () -> assertFalse(resultado.pendente()),
                () -> assertSame(localidade, resultado.localidadeValidada()),
                () -> assertNull(resultado.localidadePendente())
        );
    }

    @Test
    void deveCriarResultadoPendenteComLocalidadePendente() {
        // Arrange
        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario("São José dos Campos", "Aguardando validação");

        // Act
        ResultadoResolucaoLocalidade resultado = ResultadoResolucaoLocalidade.pendente(localidadePendente);

        // Assert
        assertAll(
                () -> assertTrue(resultado.pendente()),
                () -> assertFalse(resultado.resolvida()),
                () -> assertSame(localidadePendente, resultado.localidadePendente()),
                () -> assertNull(resultado.localidadeValidada())
        );
    }

    @Test
    void deveBloquearResultadoComAmbosNulos() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> new ResultadoResolucaoLocalidade(null, null));
    }

    @Test
    void deveBloquearResultadoComAmbosPreenchidos() {
        // Arrange
        Localidade localidade = new Localidade(criarPais(1L, "Brasil", "BR"));
        LocalidadePendente localidadePendente =
                LocalidadePendente.criarPendenteInformadaPeloUsuario("Campina Grande", "Aguardando validação");

        // Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResultadoResolucaoLocalidade(localidade, localidadePendente)
        );
    }

    private Pais criarPais(Long id, String nome, String sigla) {
        Pais pais = new Pais(nome, sigla);
        ReflectionTestUtils.setField(pais, "id", id);
        return pais;
    }
}
