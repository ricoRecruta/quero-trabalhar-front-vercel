package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.empresa.EmpresaFilterDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaRequestDTO;
import com.QueroTrabalhar.domain.dtos.empresa.EmpresaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadesDaEmpresaFilterDTO;
import com.QueroTrabalhar.domain.dtos.perfilRecrutador.RecrutadorDaEmpresaResponseDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EmpresaRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import com.QueroTrabalhar.repository.PerfilRecrutadorRepository;
import com.QueroTrabalhar.repository.specification.EmpresaSpecification;
import com.QueroTrabalhar.repository.specification.OportunidadeDeEmpregoSpecification;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import com.QueroTrabalhar.services.localidade.LocalidadeResolucaoService;
import com.QueroTrabalhar.services.localidade.RegistroLocalidadePendenteService;
import com.QueroTrabalhar.services.localidade.ResultadoResolucaoLocalidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Orquestra o cadastro autenticado e a leitura pública de empresas no MVP.
 *
 * <p>O serviço separa dois estados relevantes para a localidade da empresa. Recursos com localidade validada entram
 * no catálogo público e podem servir de contexto para navegação de oportunidades. Recursos cujo cadastro produz
 * pendência de localidade continuam acessíveis apenas no retorno interno do fluxo autenticado, com campos de apoio
 * ao status da validação. Nesta fase do MVP ainda não existe confirmação manual da sugestão de localidade nem
 * notificação automática informando que a empresa ficou oculta dos endpoints públicos.</p>
 *
 * <p>O cadastro de empresa ainda não estabelece ownership formal com um usuário específico. Essa governança fica como
 * evolução futura pós-MVP.</p>
 */
@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PerfilRecrutadorRepository perfilRecrutadorRepository;
    private final OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;
    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;
    private final LocalidadePendenteRepository localidadePendenteRepository;
    private final LocalidadeResolucaoService localidadeResolucaoService;
    private final RegistroLocalidadePendenteService registroLocalidadePendenteService;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            PerfilRecrutadorRepository perfilRecrutadorRepository,
            OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository,
            PaisRepository paisRepository,
            EstadoRepository estadoRepository,
            CidadeRepository cidadeRepository,
            LocalidadePendenteRepository localidadePendenteRepository,
            LocalidadeResolucaoService localidadeResolucaoService,
            RegistroLocalidadePendenteService registroLocalidadePendenteService
    ) {
        this.empresaRepository = empresaRepository;
        this.perfilRecrutadorRepository = perfilRecrutadorRepository;
        this.oportunidadeDeEmpregoRepository = oportunidadeDeEmpregoRepository;
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.cidadeRepository = cidadeRepository;
        this.localidadePendenteRepository = localidadePendenteRepository;
        this.localidadeResolucaoService = localidadeResolucaoService;
        this.registroLocalidadePendenteService = registroLocalidadePendenteService;
    }

    /**
     * Cria uma empresa em fluxo autenticado e resolve sua localidade no momento do cadastro.
     *
     * <p>A localidade pode chegar por IDs estruturados ou por texto livre. Quando a resolução consegue apontar uma
     * localidade oficial, a empresa já nasce apta para exposição pública. Quando a resolução gera pendência, a empresa
     * é persistida mesmo assim, mas a resposta usa o DTO interno para informar o status da localidade ao cliente
     * autenticado enquanto o recurso permanece oculto dos endpoints públicos.</p>
     */
    @Transactional
    public EmpresaResponseDTO criarEmpresa(EmpresaRequestDTO dto) {
        Empresa empresa = new Empresa(
                dto.nome().trim(),
                normalizarCampoOpcional(dto.descricao()),
                normalizarCampoOpcional(dto.site()),
                normalizarCampoOpcional(dto.emailPublico()),
                normalizarCampoOpcional(dto.telefonePublico()),
                null
        );
        LocalidadePendente localidadePendente = definirLocalidadeDaEmpresa(empresa, dto);

        Empresa empresaSalva = empresaRepository.save(empresa);
        associarDonoGenericoDaPendenciaSeNecessario(localidadePendente, empresaSalva.getId());

        return EmpresaResponseDTO.daEntidade(empresaSalva, buscarPendenciaLocalidadeDaEmpresa(empresaSalva));
    }

    /**
     * Lista apenas empresas publicamente disponíveis.
     *
     * <p>O filtro público sempre exclui empresas sem localidade validada, mesmo quando outros critérios são
     * informados.</p>
     */
    @Transactional(readOnly = true)
    public Page<EmpresaPublicaResponseDTO> listarEmpresas(EmpresaFilterDTO filtro, Pageable pageable) {
        return empresaRepository.findAll(EmpresaSpecification.comFiltros(filtro), pageable)
                .map(EmpresaPublicaResponseDTO::daEntidade);
    }

    /**
     * Busca uma empresa pelo identificador apenas se ela já estiver disponível para o catálogo público.
     */
    @Transactional(readOnly = true)
    public EmpresaPublicaResponseDTO buscarEmpresaPorId(Long id) {
        return EmpresaPublicaResponseDTO.daEntidade(buscarEntidadePublicaDisponivelPorId(id));
    }

    /**
     * Lista somente recrutadores aprovados de uma empresa já visível publicamente.
     *
     * <p>A consulta usa a visibilidade pública da empresa como pré-condição para evitar que um recurso ainda pendente
     * de localidade seja navegável por endpoints públicos auxiliares.</p>
     */
    @Transactional(readOnly = true)
    public List<RecrutadorDaEmpresaResponseDTO> listarRecrutadoresAprovadosDaEmpresa(Long empresaId) {
        buscarEntidadePublicaDisponivelPorId(empresaId);

        return perfilRecrutadorRepository
                .findByEmpresaVinculadaIdAndStatusVinculoEmpresa(empresaId, StatusVinculoEmpresa.APROVADO)
                .stream()
                .map(RecrutadorDaEmpresaResponseDTO::daEntidade)
                .sorted(Comparator.comparing(
                        RecrutadorDaEmpresaResponseDTO::nome,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();
    }

    /**
     * Lista oportunidades públicas vinculadas a uma empresa pública.
     *
     * <p>O escopo da empresa vem do parâmetro de caminho recebido pela camada HTTP. O filtro complementar não aceita
     * {@code empresaId} para evitar ambiguidade entre o contexto do path e filtros de query string.</p>
     */
    @Transactional(readOnly = true)
    public Page<OportunidadeDeEmpregoPublicaResponseDTO> listarOportunidadesDaEmpresa(
            Long empresaId,
            OportunidadesDaEmpresaFilterDTO filtro,
            Pageable pageable
    ) {
        buscarEntidadePublicaDisponivelPorId(empresaId);

        return oportunidadeDeEmpregoRepository.findAll(
                OportunidadeDeEmpregoSpecification.comFiltrosDaEmpresa(empresaId, filtro),
                pageable
        ).map(OportunidadeDeEmpregoPublicaResponseDTO::daEntidade);
    }

    private Empresa buscarEntidadePorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Empresa não encontrada. ID: " + id));
    }

    private Empresa buscarEntidadePublicaPorId(Long id) {
        return empresaRepository.findByIdAndLocalidadePaisIsNotNull(id)
                .orElseThrow(() -> new ObjectNotFoundException("Empresa não encontrada. ID: " + id));
    }

    private Empresa buscarEntidadePublicaDisponivelPorId(Long id) {
        return empresaRepository.findByIdAndLocalidadePaisIsNotNull(id)
                .orElseThrow(() -> new ObjectNotFoundException("Empresa não encontrada. ID: " + id));
    }

    /**
     * Decide entre o fluxo de localidade estruturada e o fluxo com resolução textual.
     *
     * <p>Se o cliente informar qualquer ID de localidade, o serviço trata a entrada como estruturada e exige a
     * hierarquia mínima coerente do catálogo interno. Sem IDs, o texto livre é enviado ao resolvedor técnico.
     * Ausência de ambos continua sendo erro de negócio no MVP.</p>
     */
    private LocalidadePendente definirLocalidadeDaEmpresa(Empresa empresa, EmpresaRequestDTO dto) {
        if (possuiLocalidadeEstruturadaPorIds(dto)) {
            empresa.definirLocalidadeValidada(montarLocalidade(dto.paisId(), dto.estadoId(), dto.cidadeId()));
            return null;
        }

        String localidadeTexto = normalizarCampoOpcional(dto.localidadeTexto());
        if (localidadeTexto != null) {
            return aplicarResultadoResolucaoLocalidade(empresa, localidadeResolucaoService.resolver(localidadeTexto));
        }

        throw new BusinessRuleException("A localidade da empresa é obrigatória.");
    }

    /**
     * Aplica o resultado da resolução textual preservando a regra de visibilidade pública.
     *
     * <p>Quando a resolução não valida a localidade, a empresa perde qualquer localidade oficial associada e passa a
     * depender de uma pendência interna. O MVP ainda não possui notificação automática para esse ocultamento.</p>
     */
    private LocalidadePendente aplicarResultadoResolucaoLocalidade(
            Empresa empresa,
            ResultadoResolucaoLocalidade resultadoResolucao
    ) {
        if (resultadoResolucao.resolvida()) {
            empresa.definirLocalidadeValidada(resultadoResolucao.localidadeValidada());
            return null;
        }

        limparLocalidadeValidada(empresa);
        return resultadoResolucao.localidadePendente();
    }

    private void limparLocalidadeValidada(Empresa empresa) {
        empresa.setLocalidade(null);
    }

    private void associarDonoGenericoDaPendenciaSeNecessario(LocalidadePendente localidadePendente, Long empresaId) {
        if (localidadePendente == null) {
            return;
        }

        registroLocalidadePendenteService.associarDonoGenerico(
                localidadePendente,
                TipoRecursoLocalidadePendente.EMPRESA,
                empresaId,
                CampoLocalidadePendente.LOCALIDADE
        );
    }

    private LocalidadePendente buscarPendenciaLocalidadeDaEmpresa(Empresa empresa) {
        if (empresa.getLocalidade() != null || empresa.getId() == null) {
            return null;
        }

        return localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.EMPRESA,
                empresa.getId(),
                CampoLocalidadePendente.LOCALIDADE
        ).orElse(null);
    }

    private boolean possuiLocalidadeEstruturadaPorIds(EmpresaRequestDTO dto) {
        return dto.paisId() != null || dto.estadoId() != null || dto.cidadeId() != null;
    }

    /**
     * Monta a localidade oficial da empresa validando a hierarquia país-estado-cidade no catálogo interno.
     */
    private Localidade montarLocalidade(Long paisId, Long estadoId, Long cidadeId) {
        if (cidadeId != null && estadoId == null) {
            throw new BusinessRuleException("Para informar uma cidade, o estado também deve ser informado.");
        }

        if (paisId == null) {
            throw new BusinessRuleException("O país é obrigatório quando a localidade for informada por IDs.");
        }

        Pais pais = paisRepository.findById(paisId)
                .orElseThrow(() -> new ObjectNotFoundException("País não encontrado. ID: " + paisId));

        if (estadoId == null) {
            return new Localidade(pais);
        }

        Estado estado = estadoRepository.findById(estadoId)
                .orElseThrow(() -> new ObjectNotFoundException("Estado não encontrado. ID: " + estadoId));

        if (!estado.getPais().getId().equals(pais.getId())) {
            throw new BusinessRuleException("O estado informado não pertence ao país informado.");
        }

        if (cidadeId == null) {
            return new Localidade(pais, estado);
        }

        Cidade cidade = cidadeRepository.findById(cidadeId)
                .orElseThrow(() -> new ObjectNotFoundException("Cidade não encontrada. ID: " + cidadeId));

        if (!cidade.getEstado().getId().equals(estado.getId())) {
            throw new BusinessRuleException("A cidade informada não pertence ao estado informado.");
        }

        return new Localidade(pais, estado, cidade);
    }

    private String normalizarCampoOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;
    }
}
