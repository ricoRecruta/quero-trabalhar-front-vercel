package com.QueroTrabalhar.domain.dtos.empresa;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmpresaResponseDTOTest {

    @Test
    void devePriorizarLocalidadeValidadaMesmoQuandoReceberPendenciaExplicita() {
        Pais pais = new Pais("Brasil", "BR");
        ReflectionTestUtils.setField(pais, "id", 1L);

        Empresa empresa = new Empresa("Empresa Horizonte", null, null, null, null, new Localidade(pais));
        ReflectionTestUtils.setField(empresa, "id", 10L);

        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Antigo",
                "Pendencia legada"
        );

        EmpresaResponseDTO dto = EmpresaResponseDTO.daEntidade(empresa, localidadePendente);

        assertAll(
                () -> assertEquals(10L, dto.id()),
                () -> assertEquals("VALIDADA", dto.statusLocalidade()),
                () -> assertEquals(1L, dto.paisId()),
                () -> assertEquals("Brasil", dto.pais()),
                () -> assertNull(dto.localidadeTextoOriginal()),
                () -> assertNull(dto.statusValidacaoLocalidade()),
                () -> assertNull(dto.motivoPendenciaLocalidade())
        );
    }

    @Test
    void deveExporPendenciaQuandoReceberFallbackExplicitoSemLocalidadeValidada() {
        Empresa empresa = new Empresa("Empresa Horizonte", null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", 11L);

        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario",
                "Localidade não encontrada"
        );

        EmpresaResponseDTO dto = EmpresaResponseDTO.daEntidade(empresa, localidadePendente);

        assertAll(
                () -> assertEquals(11L, dto.id()),
                () -> assertEquals("PENDENTE", dto.statusLocalidade()),
                () -> assertEquals("Vale Imaginario", dto.localidadeTextoOriginal()),
                () -> assertEquals(StatusValidacaoLocalidade.PENDENTE_VALIDACAO, dto.statusValidacaoLocalidade()),
                () -> assertEquals("Localidade não encontrada", dto.motivoPendenciaLocalidade()),
                () -> assertNull(dto.paisId()),
                () -> assertNull(dto.cidadeId())
        );
    }
}
