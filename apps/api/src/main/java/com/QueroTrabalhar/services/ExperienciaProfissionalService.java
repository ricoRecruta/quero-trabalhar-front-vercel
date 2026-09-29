package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalFilterDTO;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalRequestDTO;
import com.QueroTrabalhar.domain.dtos.experienciaProfissional.ExperienciaProfissionalResponseDTO;
import com.QueroTrabalhar.domain.entity.ExperienciaProfissional;
import com.QueroTrabalhar.domain.entity.PerfilCandidato;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.repository.ExperienciaProfissionalRepository;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.repository.specification.ExperienciaProfissionalSpecification;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExperienciaProfissionalService {

    private final ExperienciaProfissionalRepository experienciaProfissionalRepository;
    private final TipoDeEmpregoRepository tipoDeEmpregoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ExperienciaProfissionalService(
            ExperienciaProfissionalRepository experienciaProfissionalRepository,
            TipoDeEmpregoRepository tipoDeEmpregoRepository,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.experienciaProfissionalRepository = experienciaProfissionalRepository;
        this.tipoDeEmpregoRepository = tipoDeEmpregoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public List<ExperienciaProfissionalResponseDTO> listarTodasAsExperienciaProfissionais() {
        PerfilCandidato perfilCandidato = obterPerfilCandidatoAutenticado();

        return perfilCandidato.getExperiencias().stream()
                .map(ExperienciaProfissionalResponseDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExperienciaProfissionalResponseDTO buscarMinhaExperienciaPorId(Long idExperiencia) {
        PerfilCandidato perfilCandidato = obterPerfilCandidatoAutenticado();
        ExperienciaProfissional experienciaProfissional = buscarExperienciaPorId(idExperiencia);

        validarDonoDaExperiencia(
                experienciaProfissional,
                perfilCandidato,
                "O candidato autenticado não pode acessar uma experiência que pertence a outro perfil."
        );

        return ExperienciaProfissionalResponseDTO.daEntidade(experienciaProfissional);
    }

    @Transactional
    public ExperienciaProfissionalResponseDTO adicionarMinhaExperienciaProfissional(
            ExperienciaProfissionalRequestDTO dto
    ) {
        PerfilCandidato perfilCandidato = obterPerfilCandidatoAutenticado();
        TipoDeEmprego tipoDeEmprego = buscarTipoDeEmpregoPorId(dto.tipoDeEmpregoId());

        ExperienciaProfissional novaExperiencia = new ExperienciaProfissional(
                perfilCandidato,
                tipoDeEmprego,
                dto.descricao(),
                dto.dataInicio(),
                dto.dataFim()
        );

        perfilCandidato.adicionarExperiencia(novaExperiencia);

        return ExperienciaProfissionalResponseDTO.daEntidade(
                experienciaProfissionalRepository.save(novaExperiencia)
        );
    }

    @Transactional
    public void deletarMinhaExperiencia(Long experienciaId) {
        PerfilCandidato perfilCandidato = obterPerfilCandidatoAutenticado();
        ExperienciaProfissional experienciaProfissional = buscarExperienciaPorId(experienciaId);

        validarDonoDaExperiencia(
                experienciaProfissional,
                perfilCandidato,
                "O candidato autenticado não pode remover uma experiência que pertence a outro perfil."
        );

        perfilCandidato.removerExperiencia(experienciaProfissional);
        experienciaProfissionalRepository.delete(experienciaProfissional);
    }

    @Transactional(readOnly = true)
    public Page<ExperienciaProfissionalResponseDTO> listarTodasExperienciasComoAdmin(
            ExperienciaProfissionalFilterDTO filtro,
            Pageable pageable
    ) {
        return experienciaProfissionalRepository.findAll(
                        ExperienciaProfissionalSpecification.comFiltros(filtro),
                        pageable
                )
                .map(ExperienciaProfissionalResponseDTO::daEntidade);
    }

    @Transactional(readOnly = true)
    public ExperienciaProfissionalResponseDTO buscarExperienciaPorIdComoAdmin(Long id) {
        return ExperienciaProfissionalResponseDTO.daEntidade(buscarExperienciaPorId(id));
    }

    @Transactional
    public ExperienciaProfissionalResponseDTO atualizarExperienciaComoAdmin(
            Long id,
            ExperienciaProfissionalRequestDTO dto
    ) {
        ExperienciaProfissional experienciaProfissional = buscarExperienciaPorId(id);

        experienciaProfissional.setTipoDeEmprego(buscarTipoDeEmpregoPorId(dto.tipoDeEmpregoId()));
        experienciaProfissional.setDescricao(dto.descricao());
        experienciaProfissional.setDataInicio(dto.dataInicio());
        experienciaProfissional.setDataFim(dto.dataFim());

        return ExperienciaProfissionalResponseDTO.daEntidade(
                experienciaProfissionalRepository.save(experienciaProfissional)
        );
    }

    @Transactional
    public void deletarExperienciaComoAdmin(Long idExperiencia) {
        ExperienciaProfissional experienciaProfissional = buscarExperienciaPorId(idExperiencia);

        if (experienciaProfissional.getPerfilCandidato() != null) {
            experienciaProfissional.getPerfilCandidato().removerExperiencia(experienciaProfissional);
        }

        experienciaProfissionalRepository.delete(experienciaProfissional);
    }

    private PerfilCandidato obterPerfilCandidatoAutenticado() {
        return usuarioAutenticadoService.obterPerfilCandidatoAutenticado();
    }

    private ExperienciaProfissional buscarExperienciaPorId(Long idExperiencia) {
        return experienciaProfissionalRepository.findById(idExperiencia)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Experiência não encontrada. ID: " + idExperiencia
                ));
    }

    private TipoDeEmprego buscarTipoDeEmpregoPorId(Long tipoDeEmpregoId) {
        return tipoDeEmpregoRepository.findById(tipoDeEmpregoId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Tipo de emprego não encontrado. ID: " + tipoDeEmpregoId
                ));
    }

    private void validarDonoDaExperiencia(
            ExperienciaProfissional experienciaProfissional,
            PerfilCandidato perfilCandidatoAutenticado,
            String mensagem
    ) {
        if (experienciaProfissional.getPerfilCandidato() == null
                || !experienciaProfissional.getPerfilCandidato().getId().equals(perfilCandidatoAutenticado.getId())) {
            throw new BusinessRuleException(mensagem);
        }
    }
}
