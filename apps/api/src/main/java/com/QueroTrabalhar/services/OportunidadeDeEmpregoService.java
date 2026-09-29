package com.QueroTrabalhar.services;

import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoFilterDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoPublicaResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoRequestDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeDeEmpregoResponseDTO;
import com.QueroTrabalhar.domain.dtos.oportunidadeDeEmprego.OportunidadeRecrutadorMeFilterDTO;
import com.QueroTrabalhar.domain.entity.Empresa;
import com.QueroTrabalhar.domain.entity.OportunidadeDeEmprego;
import com.QueroTrabalhar.domain.entity.PerfilRecrutador;
import com.QueroTrabalhar.domain.entity.TipoDeEmprego;
import com.QueroTrabalhar.domain.entity.localidade.Cidade;
import com.QueroTrabalhar.domain.entity.localidade.Estado;
import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.QueroTrabalhar.domain.entity.localidade.LocalidadePendente;
import com.QueroTrabalhar.domain.entity.localidade.Pais;
import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.Modalidade;
import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.repository.CidadeRepository;
import com.QueroTrabalhar.repository.EstadoRepository;
import com.QueroTrabalhar.repository.LocalidadePendenteRepository;
import com.QueroTrabalhar.repository.OportunidadeDeEmpregoRepository;
import com.QueroTrabalhar.repository.PaisRepository;
import com.QueroTrabalhar.repository.TipoDeEmpregoRepository;
import com.QueroTrabalhar.repository.specification.OportunidadeDeEmpregoSpecification;
import com.QueroTrabalhar.repository.specification.OportunidadeRecrutadorMeSpecification;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import com.QueroTrabalhar.services.exceptions.ObjectNotFoundException;
import com.QueroTrabalhar.services.localidade.LocalidadeResolucaoService;
import com.QueroTrabalhar.services.localidade.RegistroLocalidadePendenteService;
import com.QueroTrabalhar.services.localidade.ResultadoResolucaoLocalidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquestra o ciclo de vida público e autenticado das oportunidades no MVP.
 *
 * <p>Este serviço concentra três decisões de domínio importantes: a oportunidade sempre nasce no contexto do
 * recrutador autenticado; a associação opcional com empresa depende de {@code publicarComoEmpresa} e do vínculo já
 * aprovado; e a visibilidade pública da vaga depende exclusivamente de possuir localidade validada.</p>
 *
 * <p>Quando a localidade fica pendente, o recurso continua acessível ao dono em fluxos internos, inclusive em
 * listagens de {@code /me}, mas permanece fora do catálogo público. Nesta fase do MVP ainda não existe confirmação
 * manual da sugestão de localidade nem notificação automática avisando que a vaga foi ocultada por pendência.</p>
 */
@Service
public class OportunidadeDeEmpregoService {

    private final OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository;
    private final TipoDeEmpregoRepository tipoDeEmpregoRepository;
    private final PaisRepository paisRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;
    private final LocalidadePendenteRepository localidadePendenteRepository;
    private final LocalidadeResolucaoService localidadeResolucaoService;
    private final RegistroLocalidadePendenteService registroLocalidadePendenteService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public OportunidadeDeEmpregoService(
            OportunidadeDeEmpregoRepository oportunidadeDeEmpregoRepository,
            TipoDeEmpregoRepository tipoDeEmpregoRepository,
            PaisRepository paisRepository,
            EstadoRepository estadoRepository,
            CidadeRepository cidadeRepository,
            LocalidadePendenteRepository localidadePendenteRepository,
            LocalidadeResolucaoService localidadeResolucaoService,
            RegistroLocalidadePendenteService registroLocalidadePendenteService,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.oportunidadeDeEmpregoRepository = oportunidadeDeEmpregoRepository;
        this.tipoDeEmpregoRepository = tipoDeEmpregoRepository;
        this.paisRepository = paisRepository;
        this.estadoRepository = estadoRepository;
        this.cidadeRepository = cidadeRepository;
        this.localidadePendenteRepository = localidadePendenteRepository;
        this.localidadeResolucaoService = localidadeResolucaoService;
        this.registroLocalidadePendenteService = registroLocalidadePendenteService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    /**
     * Lista o catálogo público geral de oportunidades.
     */
    @Transactional(readOnly = true)
    public Page<OportunidadeDeEmpregoPublicaResponseDTO> listarOportunidadesDeEmprego(
            OportunidadeDeEmpregoFilterDTO filtro,
            Pageable pageable
    ) {
        return oportunidadeDeEmpregoRepository.findAll(
                OportunidadeDeEmpregoSpecification.comFiltros(filtro),
                pageable
        ).map(OportunidadeDeEmpregoPublicaResponseDTO::daEntidade);
    }

    /**
     * Busca uma oportunidade apenas quando ela já estiver liberada para exposição pública.
     */
    @Transactional(readOnly = true)
    public OportunidadeDeEmpregoPublicaResponseDTO buscarPorId(Long id) {
        return OportunidadeDeEmpregoPublicaResponseDTO.daEntidade(buscarEntidadePublicaDisponivelPorId(id));
    }

    /**
     * Lista as oportunidades do recrutador autenticado, inclusive as que estejam com localidade pendente.
     *
     * <p>O escopo do recrutador não vem do cliente. Ele é sempre derivado do contexto de autenticação, e o DTO de
     * resposta pode carregar informações internas de pendência para apoiar a gestão do próprio recurso.</p>
     */
    @Transactional(readOnly = true)
    public Page<OportunidadeDeEmpregoResponseDTO> listarOportunidadesDoRecrutadorAutenticado(
            OportunidadeRecrutadorMeFilterDTO filtro,
            Pageable pageable
    ) {
        PerfilRecrutador perfilRecrutador = obterPerfilRecrutadorAutenticado();
        Page<OportunidadeDeEmprego> oportunidades = oportunidadeDeEmpregoRepository.findAll(
                OportunidadeRecrutadorMeSpecification.comFiltros(perfilRecrutador.getId(), filtro),
                pageable
        );
        Map<Long, LocalidadePendente> pendenciasPorOportunidade =
                mapearPendenciasPorOportunidade(oportunidades.getContent());

        return oportunidades.map(oportunidade -> OportunidadeDeEmpregoResponseDTO.daEntidade(
                        oportunidade,
                        pendenciasPorOportunidade.get(oportunidade.getId())
                ));
    }

    /**
     * Cria uma oportunidade no contexto do recrutador autenticado.
     *
     * <p>O cliente não controla {@code recrutadorId}; esse vínculo é resolvido internamente a partir da sessão
     * autenticada. Quando {@code publicarComoEmpresa=true}, a vaga só é associada à empresa vinculada se o vínculo do
     * recrutador com essa empresa já estiver aprovado. Caso a localidade fique pendente, a vaga é persistida, mas
     * permanece fora dos endpoints públicos até resolução posterior.</p>
     */
    @Transactional
    public OportunidadeDeEmpregoResponseDTO criarOportunidadeDeEmprego(OportunidadeDeEmpregoRequestDTO dto) {
        PerfilRecrutador perfilRecrutador = obterPerfilRecrutadorAutenticado();
        TipoDeEmprego tipoDeEmprego = buscarTipoDeEmpregoValido(dto.tipoDeEmpregoId());
        Empresa empresaDaOportunidade = resolverEmpresaDaOportunidade(
                perfilRecrutador,
                dto.publicarComoEmpresa()
        );

        OportunidadeDeEmprego oportunidadeDeEmprego = new OportunidadeDeEmprego(
                dto.descricao(),
                tipoDeEmprego,
                dto.modalidade(),
                null,
                perfilRecrutador,
                empresaDaOportunidade
        );
        LocalidadePendente localidadePendente = definirLocalidadeDaOportunidade(oportunidadeDeEmprego, dto);

        perfilRecrutador.adicionarOportunidadePostada(oportunidadeDeEmprego);

        OportunidadeDeEmprego oportunidadeSalva = oportunidadeDeEmpregoRepository.save(oportunidadeDeEmprego);
        associarDonoGenericoDaPendenciaSeNecessario(localidadePendente, oportunidadeSalva.getId());

        return OportunidadeDeEmpregoResponseDTO.daEntidade(
                oportunidadeSalva,
                buscarPendenciaLocalidadeDaOportunidade(oportunidadeSalva)
        );
    }

    /**
     * Atualiza uma oportunidade do recrutador autenticado preservando o contexto original de publicação.
     *
     * <p>Nesta fase do MVP, o payload não converte uma vaga pessoal em vaga publicada como empresa nem faz o caminho
     * inverso. A atualização apenas altera os atributos editáveis do recurso já pertencente ao recrutador autenticado.</p>
     */
    @Transactional
    public OportunidadeDeEmpregoResponseDTO atualizarOportunidadeDeEmprego(
            Long id,
            OportunidadeDeEmpregoRequestDTO dto
    ) {
        PerfilRecrutador perfilRecrutador = obterPerfilRecrutadorAutenticado();
        OportunidadeDeEmprego oportunidadeDeEmprego = buscarEntidadePorId(id);

        validarDonoDaOportunidade(oportunidadeDeEmprego, perfilRecrutador);

        // Nesta fase, o contexto original de publicacao da oportunidade e preservado.
        oportunidadeDeEmprego.setDescricao(dto.descricao());
        oportunidadeDeEmprego.setTipoDeEmprego(buscarTipoDeEmpregoValido(dto.tipoDeEmpregoId()));
        oportunidadeDeEmprego.setModalidade(dto.modalidade());
        LocalidadePendente localidadePendente = definirLocalidadeDaOportunidade(oportunidadeDeEmprego, dto);

        OportunidadeDeEmprego oportunidadeSalva = oportunidadeDeEmpregoRepository.save(oportunidadeDeEmprego);
        associarDonoGenericoDaPendenciaSeNecessario(localidadePendente, oportunidadeSalva.getId());

        return OportunidadeDeEmpregoResponseDTO.daEntidade(
                oportunidadeSalva,
                buscarPendenciaLocalidadeDaOportunidade(oportunidadeSalva)
        );
    }

    /**
     * Remove uma oportunidade do recrutador autenticado.
     */
    @Transactional
    public void removerOportunidadeDeEmprego(Long id) {
        PerfilRecrutador perfilRecrutador = obterPerfilRecrutadorAutenticado();
        OportunidadeDeEmprego oportunidadeDeEmprego = buscarEntidadePorId(id);

        validarDonoDaOportunidade(oportunidadeDeEmprego, perfilRecrutador);

        oportunidadeDeEmpregoRepository.removerTodosInteressesDaVaga(id);
        oportunidadeDeEmpregoRepository.delete(oportunidadeDeEmprego);
    }

    private OportunidadeDeEmprego buscarEntidadePorId(Long id) {
        return oportunidadeDeEmpregoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Oportunidade de emprego não encontrada. ID: " + id));
    }

    private OportunidadeDeEmprego buscarEntidadePublicaPorId(Long id) {
        return oportunidadeDeEmpregoRepository.findByIdAndLocalidadePaisIsNotNull(id)
                .orElseThrow(() -> new ObjectNotFoundException("Oportunidade de emprego não encontrada. ID: " + id));
    }

    private OportunidadeDeEmprego buscarEntidadePublicaDisponivelPorId(Long id) {
        return oportunidadeDeEmpregoRepository.findByIdAndLocalidadePaisIsNotNull(id)
                .orElseThrow(() -> new ObjectNotFoundException("Oportunidade de emprego não encontrada. ID: " + id));
    }

    PerfilRecrutador obterPerfilRecrutadorAutenticado() {
        return usuarioAutenticadoService.obterPerfilRecrutadorAutenticado();
    }

    private TipoDeEmprego buscarTipoDeEmpregoValido(Long tipoDeEmpregoId) {
        TipoDeEmprego tipoDeEmprego = tipoDeEmpregoRepository.findById(tipoDeEmpregoId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Tipo de emprego não encontrado. ID: " + tipoDeEmpregoId
                ));

        if (!tipoDeEmprego.isAprovado()) {
            throw new BusinessRuleException(
                    "O tipo de emprego informado ainda não foi aprovado e não pode ser usado em oportunidades."
            );
        }

        return tipoDeEmprego;
    }

    /**
     * Decide entre a localidade estruturada e a resolução textual no cadastro ou atualização da vaga.
     *
     * <p>Se o cliente informar qualquer ID de localidade, o serviço prioriza a montagem a partir do catálogo interno.
     * Sem IDs, o texto livre é enviado ao resolvedor técnico. A ausência de ambos continua sendo erro de negócio.</p>
     */
    private LocalidadePendente definirLocalidadeDaOportunidade(
            OportunidadeDeEmprego oportunidadeDeEmprego,
            OportunidadeDeEmpregoRequestDTO dto
    ) {
        if (possuiLocalidadeEstruturadaPorIds(dto)) {
            oportunidadeDeEmprego.definirLocalidadeValidada(
                    montarLocalidade(dto.paisId(), dto.estadoId(), dto.cidadeId())
            );
            return null;
        }

        String localidadeTexto = normalizarCampoOpcional(dto.localidadeTexto());
        if (localidadeTexto != null) {
            return aplicarResultadoResolucaoLocalidade(
                    oportunidadeDeEmprego,
                    localidadeResolucaoService.resolver(localidadeTexto)
            );
        }

        throw new BusinessRuleException("A localidade da oportunidade é obrigatória.");
    }

    /**
     * Aplica o resultado da resolução textual preservando a regra de ocultação pública por pendência.
     */
    private LocalidadePendente aplicarResultadoResolucaoLocalidade(
            OportunidadeDeEmprego oportunidadeDeEmprego,
            ResultadoResolucaoLocalidade resultadoResolucao
    ) {
        if (resultadoResolucao.resolvida()) {
            oportunidadeDeEmprego.definirLocalidadeValidada(resultadoResolucao.localidadeValidada());
            return null;
        }

        limparLocalidadeValidada(oportunidadeDeEmprego);
        return resultadoResolucao.localidadePendente();
    }

    private void limparLocalidadeValidada(OportunidadeDeEmprego oportunidadeDeEmprego) {
        oportunidadeDeEmprego.setLocalizacao(null);
    }

    private void associarDonoGenericoDaPendenciaSeNecessario(
            LocalidadePendente localidadePendente,
            Long oportunidadeId
    ) {
        if (localidadePendente == null) {
            return;
        }

        registroLocalidadePendenteService.associarDonoGenerico(
                localidadePendente,
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                oportunidadeId,
                CampoLocalidadePendente.LOCALIDADE
        );
    }

    private LocalidadePendente buscarPendenciaLocalidadeDaOportunidade(OportunidadeDeEmprego oportunidadeDeEmprego) {
        if (oportunidadeDeEmprego.getLocalizacao() != null || oportunidadeDeEmprego.getId() == null) {
            return null;
        }

        return localidadePendenteRepository.findFirstByTipoRecursoAndRecursoIdAndCampoAlvoOrderByAtualizadaEmDescCriadaEmDesc(
                TipoRecursoLocalidadePendente.OPORTUNIDADE_DE_EMPREGO,
                oportunidadeDeEmprego.getId(),
                CampoLocalidadePendente.LOCALIDADE
        ).orElse(null);
    }

    /**
     * Consolida a pendência mais recente de cada oportunidade para enriquecer respostas internas do dono do recurso.
     */
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

    private boolean possuiLocalidadeEstruturadaPorIds(OportunidadeDeEmpregoRequestDTO dto) {
        return dto.paisId() != null || dto.estadoId() != null || dto.cidadeId() != null;
    }

    /**
     * Monta a localidade oficial da vaga validando a hierarquia país-estado-cidade no catálogo interno.
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

    /**
     * Decide se a vaga será pessoal ou publicada em nome da empresa vinculada ao recrutador.
     *
     * <p>Quando {@code publicarComoEmpresa} não é verdadeiro, a vaga permanece sem empresa associada. Quando é
     * verdadeiro, o serviço exige empresa vinculada e vínculo aprovado antes de permitir a associação.</p>
     */
    private Empresa resolverEmpresaDaOportunidade(
            PerfilRecrutador perfilRecrutador,
            Boolean publicarComoEmpresa
    ) {
        if (!Boolean.TRUE.equals(publicarComoEmpresa)) {
            return null;
        }

        if (perfilRecrutador.getEmpresaVinculada() == null) {
            throw new BusinessRuleException(
                    "Para publicar uma oportunidade em nome da empresa, o recrutador autenticado precisa possuir empresa vinculada."
            );
        }

        if (perfilRecrutador.getStatusVinculoEmpresa() != StatusVinculoEmpresa.APROVADO) {
            throw new BusinessRuleException(
                    "Para publicar uma oportunidade em nome da empresa, o vínculo com a empresa precisa estar aprovado."
            );
        }

        return perfilRecrutador.getEmpresaVinculada();
    }

    /**
     * Garante que somente o recrutador dono da vaga possa alterá-la ou removê-la.
     */
    private void validarDonoDaOportunidade(
            OportunidadeDeEmprego oportunidadeDeEmprego,
            PerfilRecrutador perfilRecrutadorAutenticado
    ) {
        if (!oportunidadeDeEmprego.getPerfilRecrutador().getId().equals(perfilRecrutadorAutenticado.getId())) {
            throw new BusinessRuleException(
                    "O recrutador autenticado não pode alterar uma oportunidade que pertence a outro perfil."
            );
        }
    }
}
