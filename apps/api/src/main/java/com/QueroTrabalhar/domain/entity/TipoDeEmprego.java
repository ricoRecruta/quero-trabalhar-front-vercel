package com.QueroTrabalhar.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tipo_de_emprego", indexes = {
        @Index(name = "idx_titulo_emprego", columnList = "titulo") // Performance de busca
})
public class TipoDeEmprego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título não pode ser vazio")
    @Column(unique = true, nullable = false)
    private String titulo;

    @Lob //define como um objeto grande (texto longo)
    @NotBlank(message = "O tipo de emprego deve ter uma descrição")
    private String descricao;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean aprovado;

    private TipoDeEmprego(String titulo, String descricao, boolean aprovado) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.aprovado = aprovado;
    }

    public static TipoDeEmprego criarTipoDeEmpregoAdmin(String titulo, String descricao) {
        return new TipoDeEmprego(titulo, descricao, true);
    }

    public static TipoDeEmprego criarTipoDeEmpregoSugeridoPeloUsuario(String titulo, String descricao) {
        return new TipoDeEmprego(titulo, descricao, false);
    }

    protected TipoDeEmprego() {}

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isAprovado() { return aprovado; }


    public void setAprovado(boolean aprovado)  { this.aprovado = aprovado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoDeEmprego)) return false;
        TipoDeEmprego that = (TipoDeEmprego) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}