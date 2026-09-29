package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorEmpresaResponseDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilRecrutadorEmpresaFeatureServiceTest {

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
    void deveSolicitarVinculoComEmpresaExistenteDefinindoEmpresaVinculadaEStatusPendente() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);

        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(perfilRecrutadorRepository.save(perfilRecrutador)).thenReturn(perfilRecrutador);
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente.EMPRESA,
                empresa.getId(),
                com.QueroTrabalhar.domain.enums.CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.empty());

        PerfilRecrutadorEmpresaResponseDTO resposta =
                perfilRecrutadorService.solicitarVinculoEmpresa(empresa.getId());

        assertSame(empresa, perfilRecrutador.getEmpresaVinculada());
        assertEquals(StatusVinculoEmpresa.PENDENTE, perfilRecrutador.getStatusVinculoEmpresa());
        assertEquals(empresa.getId(), resposta.empresa().id());
        assertEquals(StatusVinculoEmpresa.PENDENTE, resposta.statusVinculoEmpresa());
        verify(perfilRecrutadorRepository).save(perfilRecrutador);
    }

    @Test
    void deveBloquearSolicitacaoSeJaExisteVinculoPendente() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.PENDENTE);

        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        assertThrows(
                BusinessRuleException.class,
                () -> perfilRecrutadorService.solicitarVinculoEmpresa(empresa.getId())
        );

        verify(perfilRecrutadorRepository, never()).save(any(PerfilRecrutador.class));
    }

    @Test
    void deveBloquearSolicitacaoSeJaExisteVinculoAprovado() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.APROVADO);

        when(usuarioAutenticadoService.obterPerfilRecrutadorAutenticado()).thenReturn(perfilRecrutador);
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        assertThrows(
                BusinessRuleException.class,
                () -> perfilRecrutadorService.solicitarVinculoEmpresa(empresa.getId())
        );

        verify(perfilRecrutadorRepository, never()).save(any(PerfilRecrutador.class));
    }

    @Test
    void deveAprovarVinculoPendenteComoAdmin() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.PENDENTE);

        when(perfilRecrutadorRepository.findById(perfilRecrutador.getId()))
                .thenReturn(Optional.of(perfilRecrutador));
        when(perfilRecrutadorRepository.save(perfilRecrutador)).thenReturn(perfilRecrutador);
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente.EMPRESA,
                empresa.getId(),
                com.QueroTrabalhar.domain.enums.CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.empty());

        PerfilRecrutadorEmpresaResponseDTO resposta =
                perfilRecrutadorService.aprovarVinculoEmpresaComoAdmin(perfilRecrutador.getId());

        assertEquals(StatusVinculoEmpresa.APROVADO, perfilRecrutador.getStatusVinculoEmpresa());
        assertEquals(StatusVinculoEmpresa.APROVADO, resposta.statusVinculoEmpresa());
        verify(perfilRecrutadorRepository).save(perfilRecrutador);
    }

    @Test
    void deveRecusarVinculoPendenteComoAdmin() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.PENDENTE);

        when(perfilRecrutadorRepository.findById(perfilRecrutador.getId()))
                .thenReturn(Optional.of(perfilRecrutador));
        when(perfilRecrutadorRepository.save(perfilRecrutador)).thenReturn(perfilRecrutador);
        when(localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente.EMPRESA,
                empresa.getId(),
                com.QueroTrabalhar.domain.enums.CampoLocalidadePendente.LOCALIDADE
        )).thenReturn(Optional.empty());

        PerfilRecrutadorEmpresaResponseDTO resposta =
                perfilRecrutadorService.recusarVinculoEmpresaComoAdmin(perfilRecrutador.getId());

        assertEquals(StatusVinculoEmpresa.RECUSADO, perfilRecrutador.getStatusVinculoEmpresa());
        assertEquals(StatusVinculoEmpresa.RECUSADO, resposta.statusVinculoEmpresa());
        verify(perfilRecrutadorRepository).save(perfilRecrutador);
    }

    @Test
    void deveBloquearAprovacaoSeRecrutadorNaoPossuirEmpresaVinculada() {
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);
        perfilRecrutador.setEmpresaVinculada(null);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.PENDENTE);

        when(perfilRecrutadorRepository.findById(perfilRecrutador.getId()))
                .thenReturn(Optional.of(perfilRecrutador));

        assertThrows(
                BusinessRuleException.class,
                () -> perfilRecrutadorService.aprovarVinculoEmpresaComoAdmin(perfilRecrutador.getId())
        );

        verify(perfilRecrutadorRepository, never()).save(any(PerfilRecrutador.class));
    }

    @Test
    void deveBloquearAprovacaoERecusaSeStatusNaoForPendente() {
        Empresa empresa = criarEmpresa(1L, "Empresa ACME");
        PerfilRecrutador perfilRecrutador = criarPerfilRecrutador(10L);
        perfilRecrutador.setEmpresaVinculada(empresa);
        perfilRecrutador.setStatusVinculoEmpresa(StatusVinculoEmpresa.APROVADO);

        when(perfilRecrutadorRepository.findById(perfilRecrutador.getId()))
                .thenReturn(Optional.of(perfilRecrutador));

        assertThrows(
                BusinessRuleException.class,
                () -> perfilRecrutadorService.aprovarVinculoEmpresaComoAdmin(perfilRecrutador.getId())
        );
        assertThrows(
                BusinessRuleException.class,
                () -> perfilRecrutadorService.recusarVinculoEmpresaComoAdmin(perfilRecrutador.getId())
        );

        verify(perfilRecrutadorRepository, never()).save(any(PerfilRecrutador.class));
    }

    private Empresa criarEmpresa(Long id, String nome) {
        Empresa empresa = new Empresa(nome, null, null, null, null, null);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }

    private PerfilRecrutador criarPerfilRecrutador(Long id) {
        PerfilRecrutador perfilRecrutador = new PerfilRecrutador(null, "Empresa antiga");
        ReflectionTestUtils.setField(perfilRecrutador, "id", id);
        return perfilRecrutador;
    }
}
