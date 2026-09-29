package com.QueroTrabalhar.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

@Entity
@Table(name = "experiencia_profissional")
public class ExperienciaProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_candidato_id", nullable = false)
    @JsonIgnore
    private PerfilCandidato perfilCandidato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_de_emprego_id", nullable = false)
    private TipoDeEmprego tipoDeEmprego;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column
    private LocalDate dataFim;

    public ExperienciaProfissional(PerfilCandidato perfilCandidato, TipoDeEmprego tipoDeEmprego, String descricao, LocalDate dataInicio, LocalDate dataFim) {
        this.perfilCandidato = perfilCandidato;
        this.tipoDeEmprego = tipoDeEmprego;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    protected ExperienciaProfissional() {}

    @AssertTrue(message = "A data de fim não pode ser anterior à data de início")
    public boolean isDataValida() {
        if (dataInicio == null || dataFim == null) return true;
        return !dataFim.isBefore(dataInicio);
    }

    public Long getId() { return id; }

    public PerfilCandidato getPerfilCandidato() { return perfilCandidato; }
    public void setPerfilCandidato(PerfilCandidato perfilCandidato) { this.perfilCandidato = perfilCandidato; }

    public TipoDeEmprego getTipoDeEmprego() { return tipoDeEmprego; }
    public void setTipoDeEmprego(TipoDeEmprego tipoDeEmprego) { this.tipoDeEmprego = tipoDeEmprego; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperienciaProfissional)) return false;
        ExperienciaProfissional that = (ExperienciaProfissional) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}