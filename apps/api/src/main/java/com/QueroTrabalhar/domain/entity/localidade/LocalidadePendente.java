package com.QueroTrabalhar.domain.entity.localidade;

import com.QueroTrabalhar.domain.enums.CampoLocalidadePendente;
import com.QueroTrabalhar.domain.enums.OrigemLocalidade;
import com.QueroTrabalhar.domain.enums.StatusValidacaoLocalidade;
import com.QueroTrabalhar.domain.enums.TipoRecursoLocalidadePendente;
import com.QueroTrabalhar.services.exceptions.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma localidade que ainda não pôde ser promovida para uma {@link Localidade} validada.
 *
 * <p>Esta entidade existe como fila técnica temporária de resolução. Ela preserva o texto informado
 * no cadastro quando o sistema não consegue confirmar, com segurança, uma localidade oficial no banco.
 * Enquanto a pendência existir, o recurso associado não deve ser tratado como pronto para exposição
 * em endpoints públicos.</p>
 *
 * <p>O vínculo com o recurso dono é mantido de forma genérica por tipo, identificador e campo alvo.
 * Essa escolha evita acoplar {@code Empresa} e {@code OportunidadeDeEmprego} a um estado transitório
 * do fluxo de validação e permite reprocessar a fila sem depender de referência direta dessas entidades
 * para a pendência.</p>
 *
 * <p>No MVP a pendência ainda não oferece confirmação manual de sugestão, notificação automática ao
 * dono do recurso nem ownership formal de empresa. Esses comportamentos permanecem como evolução futura.</p>
 */
@Entity
@Table(
        name = "localidade_pendente",
        indexes = {
                @Index(name = "idx_localidade_pendente_status", columnList = "status_validacao"),
                @Index(name = "idx_localidade_pendente_origem", columnList = "origem"),
                @Index(
                        name = "idx_localidade_pendente_recurso_campo",
                        columnList = "tipo_recurso,recurso_id,campo_alvo"
                )
        }
)
public class LocalidadePendente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texto_original", nullable = false, length = 255)
    private String textoOriginal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_validacao", nullable = false, length = 50)
    private StatusValidacaoLocalidade statusValidacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemLocalidade origem;

    @Column(name = "motivo_pendencia", length = 500)
    private String motivoPendencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", length = 50)
    private TipoRecursoLocalidadePendente tipoRecurso;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "campo_alvo", length = 50)
    private CampoLocalidadePendente campoAlvo;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    public LocalidadePendente(
            String textoOriginal,
            StatusValidacaoLocalidade statusValidacao,
            OrigemLocalidade origem,
            String motivoPendencia
    ) {
        this(textoOriginal, statusValidacao, origem, motivoPendencia, null, null, null);
    }

    public LocalidadePendente(
            String textoOriginal,
            StatusValidacaoLocalidade statusValidacao,
            OrigemLocalidade origem,
            String motivoPendencia,
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    ) {
        this.textoOriginal = normalizarTextoObrigatorio(textoOriginal);
        this.statusValidacao = validarStatus(statusValidacao);
        this.origem = validarOrigem(origem);
        this.motivoPendencia = normalizarTextoOpcional(motivoPendencia);
        validarDonoGenerico(tipoRecurso, recursoId, campoAlvo);
        this.tipoRecurso = tipoRecurso;
        this.recursoId = recursoId;
        this.campoAlvo = campoAlvo;

        LocalDateTime agora = LocalDateTime.now();
        this.criadaEm = agora;
        this.atualizadaEm = agora;
    }

    protected LocalidadePendente() {
    }

    /**
     * Cria uma pendência aberta apenas com o texto informado pelo usuário.
     *
     * <p>Esta sobrecarga atende fluxos em que a pendência ainda não precisa estar associada a um recurso
     * específico, mantendo a localidade validada como única fonte oficial do domínio.</p>
     */
    public static LocalidadePendente criarPendenteInformadaPeloUsuario(String textoOriginal, String motivoPendencia) {
        return criarPendenteInformadaPeloUsuario(textoOriginal, motivoPendencia, null, null, null);
    }

    /**
     * Cria uma pendência aberta já vinculada ao recurso dono por identificador genérico.
     *
     * <p>O uso do trio {@code tipoRecurso}/{@code recursoId}/{@code campoAlvo} permite reprocessar a fila
     * e informar o próprio dono em fluxos autenticados, como endpoints {@code /me}, sem transformar a
     * pendência em relacionamento permanente do domínio público.</p>
     */
    public static LocalidadePendente criarPendenteInformadaPeloUsuario(
            String textoOriginal,
            String motivoPendencia,
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    ) {
        return new LocalidadePendente(
                textoOriginal,
                StatusValidacaoLocalidade.PENDENTE_VALIDACAO,
                OrigemLocalidade.USUARIO,
                motivoPendencia,
                tipoRecurso,
                recursoId,
                campoAlvo
        );
    }

    @PrePersist
    protected void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (criadaEm == null) {
            criadaEm = agora;
        }
        if (atualizadaEm == null) {
            atualizadaEm = agora;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        atualizadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public void setTextoOriginal(String textoOriginal) {
        this.textoOriginal = normalizarTextoObrigatorio(textoOriginal);
    }

    public StatusValidacaoLocalidade getStatusValidacao() {
        return statusValidacao;
    }

    public void setStatusValidacao(StatusValidacaoLocalidade statusValidacao) {
        this.statusValidacao = validarStatus(statusValidacao);
    }

    public OrigemLocalidade getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemLocalidade origem) {
        this.origem = validarOrigem(origem);
    }

    public String getMotivoPendencia() {
        return motivoPendencia;
    }

    public void setMotivoPendencia(String motivoPendencia) {
        this.motivoPendencia = normalizarTextoOpcional(motivoPendencia);
    }

    public TipoRecursoLocalidadePendente getTipoRecurso() {
        return tipoRecurso;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public CampoLocalidadePendente getCampoAlvo() {
        return campoAlvo;
    }

    /**
     * Indica se a pendência já conhece o recurso responsável pelo texto que falhou na validação.
     */
    public boolean possuiDonoGenerico() {
        return tipoRecurso != null && recursoId != null && campoAlvo != null;
    }

    /**
     * Define o recurso dono da pendência sem introduzir referência direta da entidade de negócio para esta fila.
     */
    public void definirDonoGenerico(
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    ) {
        validarDonoGenerico(tipoRecurso, recursoId, campoAlvo);
        this.tipoRecurso = tipoRecurso;
        this.recursoId = recursoId;
        this.campoAlvo = campoAlvo;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalidadePendente that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    private static String normalizarTextoObrigatorio(String textoOriginal) {
        String textoNormalizado = normalizarTextoOpcional(textoOriginal);
        if (textoNormalizado == null) {
            throw new BusinessRuleException("O texto original da localidade é obrigatório.");
        }
        return textoNormalizado;
    }

    private static String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;
    }

    private static StatusValidacaoLocalidade validarStatus(StatusValidacaoLocalidade statusValidacao) {
        if (statusValidacao == null) {
            throw new BusinessRuleException("O status de validação da localidade é obrigatório.");
        }
        return statusValidacao;
    }

    private static OrigemLocalidade validarOrigem(OrigemLocalidade origem) {
        if (origem == null) {
            throw new BusinessRuleException("A origem da localidade é obrigatória.");
        }
        return origem;
    }

    private static void validarDonoGenerico(
            TipoRecursoLocalidadePendente tipoRecurso,
            Long recursoId,
            CampoLocalidadePendente campoAlvo
    ) {
        boolean todosNulos = tipoRecurso == null && recursoId == null && campoAlvo == null;
        boolean todosPreenchidos = tipoRecurso != null && recursoId != null && campoAlvo != null;

        if (!todosNulos && !todosPreenchidos) {
            throw new BusinessRuleException(
                    "O dono genérico da localidade pendente deve ser informado de forma completa."
            );
        }

        if (todosPreenchidos && recursoId <= 0) {
            throw new BusinessRuleException(
                    "O ID do recurso dono da localidade pendente deve ser positivo."
            );
        }
    }
}
