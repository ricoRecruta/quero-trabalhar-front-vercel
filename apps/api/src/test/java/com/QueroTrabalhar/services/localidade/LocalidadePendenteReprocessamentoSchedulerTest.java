package com.QueroTrabalhar.services.localidade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalidadePendenteReprocessamentoSchedulerTest {

    @Mock
    private LocalidadePendenteReprocessamentoService localidadePendenteReprocessamentoService;

    @Test
    void deveChamarServiceQuandoSchedulerEstiverHabilitado() {
        when(localidadePendenteReprocessamentoService.reprocessarPendenciasAbertas()).thenReturn(List.of());
        LocalidadePendenteReprocessamentoScheduler scheduler =
                new LocalidadePendenteReprocessamentoScheduler(localidadePendenteReprocessamentoService, true);

        scheduler.reprocessarPendenciasAbertas();

        verify(localidadePendenteReprocessamentoService).reprocessarPendenciasAbertas();
    }

    @Test
    void naoDeveChamarServiceQuandoSchedulerEstiverDesabilitado() {
        LocalidadePendenteReprocessamentoScheduler scheduler =
                new LocalidadePendenteReprocessamentoScheduler(localidadePendenteReprocessamentoService, false);

        scheduler.reprocessarPendenciasAbertas();

        verifyNoInteractions(localidadePendenteReprocessamentoService);
    }

    @Test
    void deveCapturarExcecaoDoServiceSemPropagar() {
        when(localidadePendenteReprocessamentoService.reprocessarPendenciasAbertas())
                .thenThrow(new RuntimeException("falha inesperada"));
        LocalidadePendenteReprocessamentoScheduler scheduler =
                new LocalidadePendenteReprocessamentoScheduler(localidadePendenteReprocessamentoService, true);

        assertDoesNotThrow(scheduler::reprocessarPendenciasAbertas);
        verify(localidadePendenteReprocessamentoService).reprocessarPendenciasAbertas();
    }
}
