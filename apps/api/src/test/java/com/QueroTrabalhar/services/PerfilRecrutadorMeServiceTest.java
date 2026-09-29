package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorEmpresaResponseDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorResponseDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilRecrutadorMeServiceTest {

    @Mock
    private PerfilRecrutadorRepository perfilRecrutadorRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private LocalidadePendenteRepository localidadePendenteRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private PerfilRecrutadorService perfilRecrutadorService;

    @Test
    void deveRetornarDadosDoPerfilRecrutadorAutenticadoSemEmpresaVinculada() {
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutadorAutenticado(10L, "João Pessoa", "Empresa Legada");
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);

        PerfilRecrutadorResponseDTO resposta = perfilRecrutadorService.buscarMeuPerfil();

        assertAll(
                () -> assertEquals(10L, resposta.id()),
                () -> assertEquals("João Pessoa", resposta.nome()),
                () -> assertEquals("Empresa Legada", resposta.empresaLegada()),
                () -> assertNull(resposta.empresaVinculadaId()),
                () -> assertNull(resposta.empresaVinculadaNome()),
                () -> assertNull(resposta.statusVinculoEmpresa())
        );
        verifyNoInteractions(perfilRecrutadorRepository, empresaRepository);
    }

    @Test
    void deveRetornarDadosDoPerfilRecrutadorAutenticadoComEmpresaVinculadaAprovada() {
        Empresa empresa = criarEmpresa(30L, "Empresa Ágil");
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutadorAutenticado(10L, "Marina Araújo", "Empresa Legada");
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.APROVADO);
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);

        PerfilRecrutadorResponseDTO resposta = perfilRecrutadorService.buscarMeuPerfil();

        assertAll(
                () -> assertEquals(10L, resposta.id()),
                () -> assertEquals("Marina Araújo", resposta.nome()),
                () -> assertEquals("Empresa Legada", resposta.empresaLegada()),
                () -> assertEquals(30L, resposta.empresaVinculadaId()),
                () -> assertEquals("Empresa Ágil", resposta.empresaVinculadaNome()),
                () -> assertEquals(StatusVinculoEmpresa.APROVADO, resposta.statusVinculoEmpresa())
        );
        verifyNoInteractions(perfilRecrutadorRepository, empresaRepository);
    }

    @Test
    void deveDelegarAObtencaoDoPerfilAutenticadoParaUsuarioAutenticadoService() {
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutadorAutenticado(22L, "Carlos Lima", "Consultoria João Pessoa");
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);

        perfilRecrutadorService.buscarMeuPerfil();

        verify(usuarioAutenticadoService, only()).obterPerfilRecrutadorAutenticado();
    }

    @Test
    void deveBuscarMinhaEmpresaComSucesso() {
        Empresa empresa = criarEmpresa(90L, "Empresa Horizonte");
        empresa.setDescricao("Ecossistema de recrutamento");
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutadorAutenticado(23L, "Larissa Gomes", "Empresa Legada");
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.APROVADO);
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.EMPRESA,
                empresa.getId(),
                CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(java.util.Optional.empty());

        PerfilRecrutadorEmpresaResponseDTO resposta = perfilRecrutadorService.buscarMinhaEmpresa();

        assertNotSame(empresa, resposta.empresa());
        assertAll(
                () -> assertEquals(90L, resposta.empresa().id()),
                () -> assertEquals("Empresa Horizonte", resposta.empresa().nome()),
                () -> assertEquals("Ecossistema de recrutamento", resposta.empresa().descricao()),
                () -> assertEquals(StatusVinculoEmpresa.APROVADO, resposta.statusVinculoEmpresa())
        );
        verifyNoInteractions(perfilRecrutadorRepository, empresaRepository);
    }

    @Test
    void deveBloquearBuscarMinhaEmpresaQuandoNaoPossuirEmpresaVinculada() {
        PerfilRecrutador perfilRecrutador =
                criarPerfilRecrutadorAutenticado(24L, "Renato Lima", "Empresa Legada");
        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);

        assertThrows(BusinessRuleException.class, () -> perfilRecrutadorService.buscarMinhaEmpresa());

        verifyNoInteractions(perfilRecrutadorRepository, empresaRepository);
    }

    private PerfilRecrutador criarPerfilRecrutadorAutenticado(Long id, String nomeUsuario, String empresaLegada) {
        Usuario usuario = new Usuario(
                "12345678909",
                nomeUsuario,
                "83999999999",
                "recrutador" + id + "@teste.com",
                "Senha@123"
        );
        ReflectionTestUtils.setField(usuario, "id", id);

        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, empresaLegada);
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        usuario.adicionarPerfilRecrutador(perfilRecrutador);

        return perfilRecrutador;
    }

    private Empresa criarEmpresa(Long id, String nome) {
        Empresa empresa = new Empresa(nome, null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }
}
