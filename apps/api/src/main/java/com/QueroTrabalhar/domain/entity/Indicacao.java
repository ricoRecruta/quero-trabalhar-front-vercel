package com.QueroTrabalhar.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "indicacao")
public class Indicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_indicado_id", nullable = false)
    private Usuario usuarioIndicado;

    @Column(nullable = false, length = 500)
    private String mensagem;

    public Indicacao(Usuario autor, Usuario usuarioIndicado, String mensagem) {
        this.autor = autor;
        this.usuarioIndicado = usuarioIndicado;
        this.mensagem = mensagem;
    }

    protected Indicacao() {}

    public Long getId() {
        return id;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Usuario getUsuarioIndicado() {
        return usuarioIndicado;
    }

    public void setUsuarioIndicado(Usuario usuarioIndicado) {
        this.usuarioIndicado = usuarioIndicado;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Indicacao)) return false;
        Indicacao that = (Indicacao) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}