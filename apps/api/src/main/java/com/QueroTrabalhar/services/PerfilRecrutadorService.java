package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorEmpresaResponseDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.PerfilRecrutadorResponseDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilRecrutadorService {

    private final PerfilRecrutadorRepository perfilRecrutadorRepository;
    private final EmpresaRepository empresaRepository;
    private final LocalidadePendenteRepository localidadePendenteRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public PerfilRecrutadorService(
            PerfilRecrutadorRepository perfilRecrutadorRepository,
            EmpresaRepository empresaRepository,
            LocalidadePendenteRepository localidadePendenteRepository,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.perfilRecrutadorRepository = perfilRecrutadorRepository;
        this.empresaRepository = empresaRepository;
        this.localidadePendenteRepository = localidadePendenteRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public PerfilRecrutadorEmpresaResponseDTO solicitarVinculoEmpresa(Long empresaId) {
        PerfilRecrutador perfilRecrutador = usuarioAutenticadoService.obterPerfilRecrutadorAutenticado();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ObjectNotFoundException("Empresa não encontrada. ID: " + empresaId));

        validarSolicitacaoDeVinculo(perfilRecrutador);

        perfilRecrutador.solicitarVinculoEmpresa(empresa);

        PerfilRecrutador perfilRecrutadorSalvo = perfilRecrutadorRepository.save(perfilRecrutador);
        return PerfilRecrutadorEmpresaResponseDTO.daEntidade(
                perfilRecrutadorSalvo,
                buscarPendenciaLocalidadeDaEmpresa(perfilRecrutadorSalvo.getEmpresaVinculada())
        );
    }

    @Transactional(readOnly = true)
    public PerfilRecrutadorEmpresaResponseDTO buscarMinhaEmpresa() {
        PerfilRecrutador perfilRecrutador = usuarioAutenticadoService.obterPerfilRecrutadorAutenticado();

        if (perfilRecrutador.getEmpresaVinculada() == null || perfilRecrutador.getStatusVinculoEmpresa() == null) {
            throw new BusinessRuleException("O recrutador autenticado não possui empresa vinculada no momento.");
        }

        return PerfilRecrutadorEmpresaResponseDTO.daEntidade(
                perfilRecrutador,
                buscarPendenciaLocalidadeDaEmpresa(perfilRecrutador.getEmpresaVinculada())
        );
    }

    @Transactional(readOnly = true)
    public PerfilRecrutadorResponseDTO buscarMeuPerfil() {
        PerfilRecrutador perfilRecrutador = usuarioAutenticadoService.obterPerfilRecrutadorAutenticado();
        return PerfilRecrutadorResponseDTO.daEntidade(perfilRecrutador);
    }

    @Transactional
    public PerfilRecrutadorEmpresaResponseDTO aprovarVinculoEmpresaComoAdmin(Long recrutadorId) {
        PerfilRecrutador perfilRecrutador = buscarPerfilRecrutadorPorId(recrutadorId);

        validarVinculoPendenteParaAnalise(perfilRecrutador);
        perfilRecrutador.aprovarVinculoEmpresa();

        PerfilRecrutador perfilRecrutadorSalvo = perfilRecrutadorRepository.save(perfilRecrutador);
        return PerfilRecrutadorEmpresaResponseDTO.daEntidade(
                perfilRecrutadorSalvo,
                buscarPendenciaLocalidadeDaEmpresa(perfilRecrutadorSalvo.getEmpresaVinculada())
        );
    }

    @Transactional
    public PerfilRecrutadorEmpresaResponseDTO recusarVinculoEmpresaComoAdmin(Long recrutadorId) {
        PerfilRecrutador perfilRecrutador = buscarPerfilRecrutadorPorId(recrutadorId);

        validarVinculoPendenteParaAnalise(perfilRecrutador);
        perfilRecrutador.recusarVinculoEmpresa();

        PerfilRecrutador perfilRecrutadorSalvo = perfilRecrutadorRepository.save(perfilRecrutador);
        return PerfilRecrutadorEmpresaResponseDTO.daEntidade(
                perfilRecrutadorSalvo,
                buscarPendenciaLocalidadeDaEmpresa(perfilRecrutadorSalvo.getEmpresaVinculada())
        );
    }

    private void validarSolicitacaoDeVinculo(PerfilRecrutador perfilRecrutador) {
        StatusVinculoEmpresa statusVinculoEmpresa = perfilRecrutador.getStatusVinculoEmpresa();

        if (statusVinculoEmpresa == StatusVinculoEmpresa.PENDENTE
                || statusVinculoEmpresa == StatusVinculoEmpresa.APROVADO) {
            throw new BusinessRuleException(
                    "O recrutador autenticado já possui um vínculo pendente ou aprovado com uma empresa."
            );
        }
    }

    private PerfilRecrutador buscarPerfilRecrutadorPorId(Long recrutadorId) {
        return perfilRecrutadorRepository.findById(recrutadorId)
                .orElseThrow(() -> new ObjectNotFoundException("Perfil de recrutador não encontrado. ID: " + recrutadorId));
    }

    private void validarVinculoPendenteParaAnalise(PerfilRecrutador perfilRecrutador) {
        if (perfilRecrutador.getEmpresaVinculada() == null) {
            throw new BusinessRuleException("O recrutador informado não possui empresa vinculada.");
        }

        if (perfilRecrutador.getStatusVinculoEmpresa() != StatusVinculoEmpresa.PENDENTE) {
            throw new BusinessRuleException(
                    "O vínculo de empresa do recrutador informado precisa estar pendente para esta operação."
            );
        }
    }

    private LocalidadePendente buscarPendenciaLocalidadeDaEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getLocalidade() != null || empresa.getId() == null) {
            return null;
        }

        return localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.EMPRESA,
                empresa.getId(),
                CampoLocalidadePendente.LOCALIDADE
        ).orElse(null);
    }
}
