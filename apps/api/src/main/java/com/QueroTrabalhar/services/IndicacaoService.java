package com.QueroTrabalhar.services;


import com.QueroTrabalhar.domain.dtos.indicacao.IndicacaoRequestDTO;
import com.QueroTrabalhar.domain.dtos.indicacao.IndicacaoResponseDTO;
import com.QueroTrabalhar.domain.entity.Indicacao;
import com.QueroTrabalhar.domain.entity.Usuario;
import com.QueroTrabalhar.repository.IndicacaoRepository;
import com.QueroTrabalhar.repository.UsuarioRepository;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IndicacaoService {

    private final IndicacaoRepository indicacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public IndicacaoService(
            IndicacaoRepository indicacaoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.indicacaoRepository = indicacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public List<IndicacaoResponseDTO> listarIndicacoesDadasPeloUsuarioAutenticado() {
        Usuario autor = usuarioAutenticadoService.obterUsuarioAutenticado();

        return indicacaoRepository.findByAutorId(autor.getId()).stream()
                .map(IndicacaoResponseDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IndicacaoResponseDTO> listarIndicacoesRecebidasPeloUsuarioAutenticado() {
        Usuario usuarioAutenticado = usuarioAutenticadoService.obterUsuarioAutenticado();

        return indicacaoRepository.findByUsuarioIndicadoId(usuarioAutenticado.getId()).stream()
                .map(IndicacaoResponseDTO::daEntidade)
                .toList();
    }

    @Transactional
    public IndicacaoResponseDTO criarIndicacao(IndicacaoRequestDTO dto) {
        Usuario autor = usuarioAutenticadoService.obterUsuarioAutenticado();
        Usuario usuarioIndicado = usuarioRepository.findById(dto.usuarioIndicadoId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Usuário indicado não encontrado. ID: " + dto.usuarioIndicadoId()
                ));

        if (autor.getId().equals(usuarioIndicado.getId())) {
            throw new BusinessRuleException("Um usuário não pode indicar a si mesmo.");
        }

        Indicacao indicacao = new Indicacao(
                autor,
                usuarioIndicado,
                normalizarMensagem(dto.mensagem())
        );

        autor.adicionarIndicacaoDada(indicacao);
        usuarioIndicado.adicionarIndicacaoRecebida(indicacao);

        return IndicacaoResponseDTO.daEntidade(indicacaoRepository.save(indicacao));
    }

    @Transactional
    public void excluirIndicacaoDoUsuarioAutenticado(Long indicacaoId) {
        Usuario autor = usuarioAutenticadoService.obterUsuarioAutenticado();
        Indicacao indicacao = indicacaoRepository.findById(indicacaoId)
                .orElseThrow(() -> new ObjectNotFoundException("Indicação não encontrada. ID: " + indicacaoId));

        if (!indicacao.getAutor().getId().equals(autor.getId())) {
            throw new BusinessRuleException("Você só pode excluir indicações criadas por você.");
        }

        indicacao.getAutor().removerIndicacaoDada(indicacao);
        indicacao.getUsuarioIndicado().removerIndicacaoRecebida(indicacao);

        indicacaoRepository.delete(indicacao);
    }

    private String normalizarMensagem(String mensagem) {
        return mensagem.trim();
    }
}
