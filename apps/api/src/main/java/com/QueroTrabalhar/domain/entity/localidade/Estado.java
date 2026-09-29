package com.QueroTrabalhar.domain.entity.localidade;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "estado",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_nome_pais", columnNames = {"nome", "pais_id"})
        }, indexes = {
        @Index(name = "idx_estado_pais", columnList = "pais_id")
})
public class Estado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 5) // Nem todo lugar do mundo usa sigla exata de 2 letras
    private String sigla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;

    public Estado(String nome, String sigla, Pais pais) {
        this.nome = nome;
        this.sigla = sigla;
        this.pais = pais;
    }

    protected Estado() {
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

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Estado estado)) return false;
        return Objects.equals(getId(), estado.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
