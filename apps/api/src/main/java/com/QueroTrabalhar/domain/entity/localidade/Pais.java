package com.QueroTrabalhar.domain.entity.localidade;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "pais")
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nome;

    @Column(length = 2, unique = true, nullable = false)
    private String sigla;

    public Pais(String nome, String sigla) {
        this.nome = nome;
        this.sigla = sigla;
    }

    protected Pais() {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pais pais)) return false;
        return Objects.equals(getId(), pais.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
