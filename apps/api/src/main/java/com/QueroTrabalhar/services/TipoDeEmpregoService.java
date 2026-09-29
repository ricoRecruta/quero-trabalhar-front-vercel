package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.tipoDeEmprego.TipoDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.repository.specification.TipoDeEmpregoSpecification;
import com.QueroTrabalhar.services.exceptions.DataIntegrityViolationException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipoDeEmpregoService {

    @Autowired
    private TipoDeEmpregoRepository tipoDeEmpregoRepository;

    public List<TipoDeEmpregoResponseDTO> listarAprovados() {
        return tipoDeEmpregoRepository.findByAprovadoTrue().stream()
                .map(TipoDeEmpregoResponseDTO::daEntidade)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TipoDeEmpregoResponseDTO> listarNaoAprovados(
            TipoDeEmpregoFilterDTO filtro,
            Pageable pageable
    ) {
        return tipoDeEmpregoRepository.findAll(
                TipoDeEmpregoSpecification.comFiltros(filtro),
                pageable
        ).map(TipoDeEmpregoResponseDTO::daEntidade);
    }

    public TipoDeEmpregoResponseDTO buscarPorId(Long id) {
        return TipoDeEmpregoResponseDTO.daEntidade(tipoDeEmpregoRepository
                .findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("Tipo de emprego não encontrado, id: "+ id))
        );
    }

    public TipoDeEmpregoResponseDTO buscarAprovadoPorId(Long id) {
        TipoDeEmprego tipoDeEmprego = tipoDeEmpregoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Tipo de emprego aprovado não encontrado. ID: " + id));

        if (!tipoDeEmprego.isAprovado()) {
            throw new ObjectNotFoundException("Tipo de emprego aprovado não encontrado. ID: " + id);
        }

        return TipoDeEmpregoResponseDTO.daEntidade(tipoDeEmprego);
    }

    public TipoDeEmpregoResponseDTO criarNoCatalogo(TipoDeEmpregoRequestDTO tipoDeEmprego) {
        if(tipoDeEmpregoRepository.existsByTitulo(tipoDeEmprego.titulo()))
            throw new DataIntegrityViolationException("Já existe um tipo de emprego com o título: "+ tipoDeEmprego.titulo());

        TipoDeEmprego tipoDeEmpregoCriar = TipoDeEmprego
                .criarTipoDeEmpregoAdmin(
                        tipoDeEmprego.titulo()
                        ,tipoDeEmprego.descricao());

        tipoDeEmpregoRepository.save(tipoDeEmpregoCriar);

        return TipoDeEmpregoResponseDTO.daEntidade(tipoDeEmpregoCriar);
    }

    public TipoDeEmpregoResponseDTO sugerirNoCatalogo(TipoDeEmpregoRequestDTO tipoDeEmprego) {
        if(tipoDeEmpregoRepository.existsByTitulo(tipoDeEmprego.titulo()))
            throw new DataIntegrityViolationException("Já existe um tipo de emprego com o título: "+ tipoDeEmprego.titulo());

        TipoDeEmprego tipoDeEmpregoSugerir = TipoDeEmprego
                .criarTipoDeEmpregoSugeridoPeloUsuario(
                        tipoDeEmprego.titulo()
                        ,tipoDeEmprego.descricao());

        tipoDeEmpregoRepository.save(tipoDeEmpregoSugerir);

        return TipoDeEmpregoResponseDTO.daEntidade(tipoDeEmpregoSugerir);
    }

    public TipoDeEmpregoResponseDTO aprovarSugestao(Long id, String tituloCorrigido, String descricao) {
        TipoDeEmprego pendenteAprovar = tipoDeEmpregoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Tipo de emprego não encontrado, id: " + id));

        // O Admin pode corrigir erros de português antes de aprovar
        pendenteAprovar.setTitulo(tituloCorrigido);
        pendenteAprovar.setDescricao(descricao);
        pendenteAprovar.setAprovado(true);

        tipoDeEmpregoRepository.save(pendenteAprovar);

        return TipoDeEmpregoResponseDTO.daEntidade(tipoDeEmpregoRepository.save(pendenteAprovar));
    }

    @Transactional
    public void aprovarEmLote(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new DataIntegrityViolationException("A lista de IDs não pode estar vazia.");
        }

        int registrosAtualizados = tipoDeEmpregoRepository.aprovarEmLote(ids);

        if (registrosAtualizados == 0) {
            throw new DataIntegrityViolationException("Nenhum tipo de emprego encontrado para os IDs informados.");
        }
    }

    public void deletar(Long id) {
        if(!tipoDeEmpregoRepository.existsById(id))
            throw new ObjectNotFoundException("Não existe um tipo de emprego com o id: "+id);
        try{
            tipoDeEmpregoRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e){
            throw new DataIntegrityViolationException("Não é possível deletar o Tipo de Emprego(id: "+id+") pois existem vagas associadas a ele.");
        }
    }
}
