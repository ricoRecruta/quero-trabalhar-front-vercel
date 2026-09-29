package com.QueroTrabalhar.domain.entity;

import com.QueroTrabalhar.domain.enums.StatusVinculoEmpresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "perfil_recrutador")
public class PerfilRecrutador {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_vinculada_id")
    @JsonIgnore
    private Empresa empresaVinculada;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_vinculo_empresa", length = 20)
    private StatusVinculoEmpresa statusVinculoEmpresa;

    @OneToMany(mappedBy = "perfilRecrutador", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OportunidadeDeEmprego> oportunidadesPostadas = new ArrayList<>();

    public PerfilRecrutador(Usuario usuario, String empresa) {
        this.usuario = usuario;
        this.empresa = empresa;
    }

    protected PerfilRecrutador() {
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public Empresa getEmpresaVinculada() {
        return empresaVinculada;
    }

    public void setEmpresaVinculada(Empresa empresaVinculada) {
        this.empresaVinculada = empresaVinculada;
    }

    public StatusVinculoEmpresa getStatusVinculoEmpresa() {
        return statusVinculoEmpresa;
    }

    public void setStatusVinculoEmpresa(StatusVinculoEmpresa statusVinculoEmpresa) {
        this.statusVinculoEmpresa = statusVinculoEmpresa;
    }

    public void solicitarVinculoEmpresa(Empresa empresaVinculada) {
        this.empresaVinculada = empresaVinculada;
        this.statusVinculoEmpresa = StatusVinculoEmpresa.PENDENTE;
    }

    public void aprovarVinculoEmpresa() {
        this.statusVinculoEmpresa = StatusVinculoEmpresa.APROVADO;
    }

    public void recusarVinculoEmpresa() {
        this.statusVinculoEmpresa = StatusVinculoEmpresa.RECUSADO;
    }

    public List<OportunidadeDeEmprego> getOportunidadesPostadas() {
        return Collections.unmodifiableList(this.oportunidadesPostadas);
    }

    public void adicionarOportunidadePostada(OportunidadeDeEmprego vaga) {
        this.oportunidadesPostadas.add(vaga);
        vaga.setPerfilRecrutador(this);
    }

    public void removerOportunidadePostada(OportunidadeDeEmprego vaga) {
        this.oportunidadesPostadas.remove(vaga);
        vaga.setPerfilRecrutador(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PerfilRecrutador that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
