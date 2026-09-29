package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeInteresseCandidatoFilterDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PerfilCandidatoRepository;
import com.QueroTrabalhar.repository.specification.OportunidadeInteresseCandidatoSpecification;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerfilCandidatoService {

    private final PerfilCandidatoRepository perfilCandidatoRepository;
    private final OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;
    private final LocalidadePendenteRepository localidadePendenteRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public PerfilCandidatoService(
            PerfilCandidatoRepository perfilCandidatoRepository,
            OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository,
            LocalidadePendenteRepository localidadePendenteRepository,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.perfilCandidatoRepository = perfilCandidatoRepository;
        this.oportunidadeDeEmpregoRepository = oportunidadeDeEmpregoRepository;
        this.localidadePendenteRepository = localidadePendenteRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public void demonstrarInteresseEmVaga(Long vagaId) {
        PerfilCandidato perfilCandidato = usuarioAutenticadoService.obterPerfilCandidatoAutenticado();
        OportunidadeDeEmprego vaga = buscarVagaPorId(vagaId);

        if (perfilCandidato.getVagasDeInteresse().contains(vaga)) {
            throw new BusinessRuleException("Você já demonstrou interesse nesta vaga.");
        }

        perfilCandidato.demonstrarInteresse(vaga);
        perfilCandidatoRepository.save(perfilCandidato);
    }

    @Transactional
    public void removerInteresseEmVaga(Long vagaId) {
        PerfilCandidato perfilCandidato = usuarioAutenticadoService.obterPerfilCandidatoAutenticado();
        OportunidadeDeEmprego vaga = buscarVagaPorId(vagaId);

        if (!perfilCandidato.getVagasDeInteresse().contains(vaga)) {
            throw new BusinessRuleException("Você ainda não demonstrou interesse nesta vaga.");
        }

        perfilCandidato.removerInteresse(vaga);
        perfilCandidatoRepository.save(perfilCandidato);
    }

    @Transactional(readOnly = true)
    public Page<OportunidadeDeEmpregoResponseDTO> listarMinhasVagasDeInteresse(
            OportunidadeInteresseCandidatoFilterDTO filtro,
            Pageable pageable
    ) {
        PerfilCandidato perfilCandidato = usuarioAutenticadoService.obterPerfilCandidatoAutenticado();
        Page<OportunidadeDeEmprego> vagasDeInteresse = oportunidadeDeEmpregoRepository.findAll(
                OportunidadeInteresseCandidatoSpecification.comFiltros(perfilCandidato.getId(), filtro),
                pageable
        );
        Map<Long, LocalidadePendente> pendenciasPorOportunidade =
                mapearPendenciasPorOportunidade(vagasDeInteresse.getContent());

        return vagasDeInteresse.map(vaga -> OportunidadeDeEmpregoResponseDTO.daEntidade(
                vaga,
                pendenciasPorOportunidade.get(vaga.getId())
        ));
    }

    private OportunidadeDeEmprego buscarVagaPorId(Long vagaId) {
        return oportunidadeDeEmpregoRepository.findById(vagaId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Oportunidade de emprego não encontrada. ID: " + vagaId
                ));
    }

    private Map<Long, LocalidadePendente> mapearPendenciasPorOportunidade(List<OportunidadeDeEmprego> oportunidades) {
        List<Long> oportunidadeIds = oportunidades.stream()
                .filter(oportunidade -> oportunidade.getLocalizacao() == null)
                .map(OportunidadeDeEmprego::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (oportunidadeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, LocalidadePendente> pendenciasPorOportunidade = new HashMap<>();
        localidadePendenteRepository.findByTipoRecursoAndCampoAlvoAndRecursoIdInOrderByRecursoIdAscAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                CampoLocalidadePendente.LOCALIDADE,
                oportunidadeIds
        ).forEach(pendencia -> pendenciasPorOportunidade.putIfAbsent(
                pendencia.getRecursoId(),
                pendencia
        ));

        return pendenciasPorOportunidade;
    }
}
