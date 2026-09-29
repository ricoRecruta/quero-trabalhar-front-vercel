package com.QueroTrabalhar.domain.entity;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Representa a empresa exibida no catálogo público e usada como contexto opcional de publicação de oportunidades.
 *
 * <p>No MVP, a empresa pode ser persistida já com uma localidade validada ou com a localidade ainda pendente de
 * resolução. Quando a localidade não é validada no cadastro, a empresa continua existindo internamente, mas fica
 * fora dos endpoints públicos até que uma localidade oficial seja associada ao recurso.</p>
 *
 * <p>O ownership formal da empresa por um usuário autenticado ainda não foi implementado e permanece como evolução
 * futura pós-MVP.</p>
 */
@Entity
@Table(
        name = "empresa",
        indexes = {
                @Index(name = "idx_empresa_nome", columnList = "nome")
        }
)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Column(length = 255)
    private String site;

    @Column(name = "email_publico", length = 150)
    private String emailPublico;

    @Column(name = "telefone_publico", length = 20)
    private String telefonePublico;

    @Embedded
    @AssociationOverrides({
            @AssociationOverride(name = "pais", joinColumns = @JoinColumn(name = "pais_id", nullable = true)),
            @AssociationOverride(name = "estado", joinColumns = @JoinColumn(name = "estado_id", nullable = true)),
            @AssociationOverride(name = "cidade", joinColumns = @JoinColumn(name = "cidade_id", nullable = true))
    })
    private Localidade localidade;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo;

    public Empresa(
            String nome,
            String descricao,
            String site,
            String emailPublico,
            String telefonePublico,
            Localidade localidade
    ) {
        this.nome = nome;
        this.descricao = descricao;
        this.site = site;
        this.emailPublico = emailPublico;
        this.telefonePublico = telefonePublico;
        this.ativo = true;

        if (localidade != null) {
            definirLocalidadeValidada(localidade);
        }
    }

    protected Empresa() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEmailPublico() {
        return emailPublico;
    }

    public void setEmailPublico(String emailPublico) {
        this.emailPublico = emailPublico;
    }

    public String getTelefonePublico() {
        return telefonePublico;
    }

    public void setTelefonePublico(String telefonePublico) {
        this.telefonePublico = telefonePublico;
    }

    public Localidade getLocalidade() {
        return localidade;
    }

    public void setLocalidade(Localidade localidade) {
        if (localidade != null) {
            definirLocalidadeValidada(localidade);
            return;
        }

        this.localidade = null;
    }

    /**
     * Registra uma localidade já validada para a empresa.
     *
     * <p>A decisão sobre usar catálogo estruturado ou gerar pendência acontece na camada de serviço. Quando a
     * entidade recebe uma localidade por este método, o valor já é tratado como oficial para fins de exposição
     * pública.</p>
     */
    public void definirLocalidadeValidada(Localidade localidade) {
        this.localidade = Objects.requireNonNull(localidade, "A localidade validada é obrigatória.");
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Empresa empresa)) {
            return false;
        }
        return id != null && id.equals(empresa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
