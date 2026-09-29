package com.QueroTrabalhar.domain.entity.localidade;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "cidade",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_nome_estado", columnNames = {"nome", "estado_id"})
        }, indexes = {
        @Index(name = "idx_cidade_nome", columnList = "nome"), // Crucial para o autocomplete!
        @Index(name = "idx_cidade_estado", columnList = "estado_id")
})
public class Cidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    public Cidade(String nome, Estado estado) {
        this.nome = nome;
        this.estado = estado;
    }

    protected Cidade() {
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

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cidade cidade)) return false;
        return Objects.equals(getId(), cidade.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
