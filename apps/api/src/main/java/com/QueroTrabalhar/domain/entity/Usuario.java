package com.QueroTrabalhar.domain.entity;

import com.QueroTrabalhar.domain.dtos.usuario.UsuarioRequestDTO;
import com.QueroTrabalhar.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CPF
    @Column(unique = true)
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    //Talvez ser um Set<String> para dar a possibilidade de cadastrar mais de um número
    @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres.")
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é Obrigatório")
    @Column(unique = true)
    private String email;

    //Ver alguns constrains de senha para dar mais segurança (Letra maiúscula, Caractere especial, entre outros)
    @NotBlank(message = "Senha é Obrigatória")
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profiles")
    protected Set<Integer> profiles = new HashSet<>();
                                                          // Por algum motivo ta recomendando o fetch eager
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PerfilCandidato perfilCandidato;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PerfilRecrutador perfilRecrutador;

    @OneToMany(mappedBy = "usuarioIndicado", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Indicacao> indicacaoRecebidas = new ArrayList<>();

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Indicacao> indicacaoDadas = new ArrayList<>();

    public Usuario(String cpf, String nome, String telefone, String email, String senha) {
        this.cpf = cpf;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.senha = senha;
        addProfile(Role.USER);
    }

    public Usuario(UsuarioRequestDTO usuario) {
        this.cpf = usuario.cpf();
        this.nome = usuario.nome();
        this.telefone = usuario.telefone();
        this.email = usuario.email();
        this.senha = usuario.senha();
    }

    protected Usuario() {}

    public Long getId() { return id; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public PerfilCandidato getPerfilCandidato() { return perfilCandidato; }
    public PerfilRecrutador getPerfilRecrutador() { return perfilRecrutador; }

    public boolean ehCandidato() { return this.perfilCandidato != null; }
    public boolean ehRecrutador() { return this.perfilRecrutador != null; }

    public void adicionarPerfilCandidato(PerfilCandidato perfil) {
        this.perfilCandidato = perfil;
        perfil.setUsuario(this);
    }

    public void adicionarPerfilRecrutador(PerfilRecrutador perfil) {
        this.perfilRecrutador = perfil;
        perfil.setUsuario(this);
    }

    public void removerPerfilCandidato() {
        if (this.perfilCandidato != null) {
            this.perfilCandidato.setUsuario(null);
            this.perfilCandidato = null;
        }
    }

    public void removerPerfilRecrutador() {
        if (this.perfilRecrutador != null) {
            this.perfilRecrutador.setUsuario(null);
            this.perfilRecrutador = null;
        }
    }

    public List<Indicacao> getIndicacoesRecebidas() {
        return Collections.unmodifiableList(this.indicacaoRecebidas);
    }

    public void adicionarIndicacaoRecebida(Indicacao indicacao) {
        this.indicacaoRecebidas.add(indicacao);
        indicacao.setUsuarioIndicado(this);
    }

    public void removerIndicacaoRecebida(Indicacao indicacao) {
        this.indicacaoRecebidas.remove(indicacao);
    }

    public List<Indicacao> getIndicacoesDadas() {
        return Collections.unmodifiableList(this.indicacaoDadas);
    }

    public void adicionarIndicacaoDada(Indicacao indicacao) {
        this.indicacaoDadas.add(indicacao);
        indicacao.setAutor(this);
    }

    public void removerIndicacaoDada(Indicacao indicacao) {
        this.indicacaoDadas.remove(indicacao);
    }

    public Set<Role> getProfiles() {
        return this.profiles.stream().map(Role::toEnum).collect(Collectors.toSet());
    }

    public void addProfile(Role profile) {
        this.profiles.add(profile.getCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
