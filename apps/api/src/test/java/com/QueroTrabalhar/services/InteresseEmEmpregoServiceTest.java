package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.Preferencia;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.InteresseEmEmpregoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteresseEmEmpregoServiceTest {

    @Mock
    private InteresseEmEmpregoRepository interesseEmEmpregoRepository;

    @InjectMocks
    private InteresseEmEmpregoService interesseEmEmpregoService;

    @Test
    void deveListarInteressesEmEmpregos() {
        // Arrange
        Preferencia primeiraPreferencia = criarPreferencia(1L, true);
        Preferencia segundaPreferencia = criarPreferencia(2L, false);
        when(interesseEmEmpregoRepository.findAll()).thenReturn(List.of(primeiraPreferencia, segundaPreferencia));

        // Act
        List<Preferencia> resposta = interesseEmEmpregoService.ListarInteressesEmEmpregos();

        // Assert
        assertEquals(2, resposta.size());
        assertSame(primeiraPreferencia, resposta.get(0));
        assertSame(segundaPreferencia, resposta.get(1));
        verify(interesseEmEmpregoRepository).findAll();
    }

    @Test
    void deveSalvarInteresseEmEmpregoComSucesso() {
        // Arrange
        Preferencia preferencia = criarPreferencia(3L, true);
        when(interesseEmEmpregoRepository.save(preferencia)).thenReturn(preferencia);

        // Act
        Preferencia resposta = interesseEmEmpregoService.salvarInteresseEmEmprego(preferencia);

        // Assert
        assertSame(preferencia, resposta);
        verify(interesseEmEmpregoRepository).save(preferencia);
    }

    @Test
    void deveDeletarInteresseEmEmpregoComSucesso() {
        // Arrange
        Long id = 4L;

        // Act
        interesseEmEmpregoService.deletarInteresseEmEmprego(id);

        // Assert
        verify(interesseEmEmpregoRepository).deleteById(id);
    }

    @Test
    void devePropagarExcecaoAoDeletarInteresseInexistenteConformeContratoAtual() {
        // Arrange
        Long id = 5L;
        doThrow(new EmptyResultDataAccessException(1))
                .when(interesseEmEmpregoRepository)
                .deleteById(id);

        // Act / Assert
        assertThrows(
                EmptyResultDataAccessException.class,
                () -> interesseEmEmpregoService.deletarInteresseEmEmprego(id)
        );

        verify(interesseEmEmpregoRepository).deleteById(id);
    }

    private Preferencia criarPreferencia(Long id, boolean querTrabalharRemoto) {
        Usuario usuario = new Usuario(
                "12345678909",
                "Candidato " + id,
                "83999999999",
                "candidato" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilCandidato perfilCandidato = new PerfilCandidato(usuario);
        ReflectionTestUtils.setField(perfilCandidato, "id", id);
        usuario.adicionarPerfilCandidato(perfilCandidato);

        Preferencia preferencia = new Preferencia(perfilCandidato, querTrabalharRemoto);
        ReflectionTestUtils.setField(preferencia, "id", id);
        perfilCandidato.definirInteresse(preferencia);
        return preferencia;
    }
}
