package com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego;

import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OportunidadeDeEmpregoResponseDTOTest {

    @Test
    void devePriorizarLocalidadeValidadaMesmoQuandoReceberPendenciaExplicita() {
        Pais pais = new Pais("Brasil", "BR");
        ReflectionTestUtils.setField(pais, "id", 1L);
        Empresa empresa = new Empresa("Empresa Horizonte", null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", 30L);
        PerfilRecrutador recrutador = criarRecrutador(20L, "Marina Costa");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmprego(5L, "Backend");

        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                "Java 21 e Spring Boot",
                tipoDeEmprego,
                Modalidade.REMOTO,
                new Localidade(pais),
                recrutador,
                empresa
        );
        ReflectionTestUtils.setField(oportunidade, "id", 100L);

        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Antigo",
                "Pendencia legada"
        );

        OportunidadeDeEmpregoResponseDTO dto =
                OportunidadeDeEmpregoResponseDTO.daEntidade(oportunidade, localidadePendente);

        assertAll(
                () -> assertEquals(100L, dto.id()),
                () -> assertEquals("VALIDADA", dto.statusLocalidade()),
                () -> assertEquals(1L, dto.paisId()),
                () -> assertNull(dto.localidadeTextoOriginal()),
                () -> assertNull(dto.statusValidacaoLocalidade()),
                () -> assertNull(dto.motivoPendenciaLocalidade()),
                () -> assertEquals(20L, dto.recrutadorId()),
                () -> assertEquals("Marina Costa", dto.recrutadorNome()),
                () -> assertEquals(30L, dto.empresaId())
        );
    }

    @Test
    void deveExporPendenciaQuandoReceberFallbackExplicitoSemLocalidadeValidada() {
        PerfilRecrutador recrutador = criarRecrutador(21L, "Paula Mendes");
        TipoDeEmprego tipoDeEmprego = criarTipoDeEmprego(6L, "Tech Lead");
        OportunidadeDeEmprego oportunidade = new OportunidadeDeEmprego(
                "Lideranca tecnica",
                tipoDeEmprego,
                Modalidade.HIBRIDO,
                null,
                recrutador,
                null
        );
        ReflectionTestUtils.setField(oportunidade, "id", 101L);

        LocalidadePendente localidadePendente = LocalidadePendente.criarPendenteInformadaPeloUsuario(
                "Vale Imaginario",
                "Localidade não encontrada"
        );

        OportunidadeDeEmpregoResponseDTO dto =
                OportunidadeDeEmpregoResponseDTO.daEntidade(oportunidade, localidadePendente);

        assertAll(
                () -> assertEquals(101L, dto.id()),
                () -> assertEquals("PENDENTE", dto.statusLocalidade()),
                () -> assertEquals("Vale Imaginario", dto.localidadeTextoOriginal()),
                () -> assertEquals(StatusValidacaoLocalidade.PENDENTE_VALIDACAO, dto.statusValidacaoLocalidade()),
                () -> assertEquals("Localidade não encontrada", dto.motivoPendenciaLocalidade()),
                () -> assertEquals(21L, dto.recrutadorId()),
                () -> assertEquals("Paula Mendes", dto.recrutadorNome()),
                () -> assertNull(dto.empresaId())
        );
    }

    private PerfilRecrutador criarRecrutador(Long id, String nomeUsuario) {
        Usuario usuario = new Usuario(
                "12345678909",
                nomeUsuario,
                "83999999999",
                "recrutador" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa Legada");
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);

        return perfilRecrutador;
    }

    private TipoDeEmprego criarTipoDeEmprego(Long id, String titulo) {
        TipoDeEmprego tipoDeEmprego = TipoDeEmprego.criarTipoDeEmpregoAdmin(titulo, "Descricao");
        ReflectionTestUtils.setField(tipoDeEmprego, "id", id);
        return tipoDeEmprego;
    }
}
