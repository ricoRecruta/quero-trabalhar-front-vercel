package com.QueroTrabalhar.domain.entity;

import com.QueroTrabalhar.domain.entity.localidade.Localidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "preferencia")
public class Preferencia {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "perfil_candidato_id")
    @JsonIgnore
    private PerfilCandidato perfilCandidato;

    @ManyToMany
    @JoinTable(
            name = "candidato_tipo_emprego_interesse",
            joinColumns = @JoinColumn(name = "interesse_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_emprego_id")
    )
    private Set<TipoDeEmprego> tiposInteressados = new HashSet<>();

    @Column(nullable = false)
    private boolean querTrabalharRemoto;

    @ElementCollection
    @CollectionTable(name = "localizacao_cidade_interesse", joinColumns = @JoinColumn(name = "interesse_id"))
    private Set<Localidade> locaisDeInteresse = new HashSet<>();

    public Preferencia(PerfilCandidato perfilCandidato, boolean querTrabalharRemoto) {
        this.perfilCandidato = perfilCandidato;
        this.querTrabalharRemoto = querTrabalharRemoto;
    }

    protected Preferencia() {}

    public Long getId() { return id; }

    public PerfilCandidato getPerfilCandidato() { return perfilCandidato; }
    void setPerfilCandidato(PerfilCandidato perfilCandidato) { this.perfilCandidato = perfilCandidato; }

    public boolean isQuerTrabalharRemoto() { return querTrabalharRemoto; }
    public void setQuerTrabalharRemoto(boolean querTrabalharRemoto) { this.querTrabalharRemoto = querTrabalharRemoto; }

    public Set<Localidade> getLocaisDeInteresse() { return Collections.unmodifiableSet(this.locaisDeInteresse); }
    public void adicionarLocal(Localidade localidade) { this.locaisDeInteresse.add(localidade); }
    public void removerLocal(Localidade localidade) { this.locaisDeInteresse.remove(localidade); }

    public Set<TipoDeEmprego> getTiposInteressados() { return Collections.unmodifiableSet(this.tiposInteressados); }
    public void adicionarTipoInteresse(TipoDeEmprego tipo) { this.tiposInteressados.add(tipo); }
    public void removerTipoInteresse(TipoDeEmprego tipo) { this.tiposInteressados.remove(tipo); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Preferencia)) return false;
        Preferencia that = (Preferencia) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}