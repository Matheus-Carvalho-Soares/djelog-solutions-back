package com.djelog.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "Informe o nome da empresa.")
    @Size(max = 120, message = "Nome da empresa deve ter no maximo 120 caracteres.")
    private String nome;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Descricao deve ter no maximo 2000 caracteres.")
    private String descricao;

    @Column(name = "nome_contato", nullable = false, length = 100)
    @NotBlank(message = "Informe o nome do contato.")
    @Size(max = 100, message = "Nome do contato deve ter no maximo 100 caracteres.")
    private String nomeContato;

    @Column(name = "telefone_contato", length = 100)
    @Size(max = 30, message = "Telefone do contato deve ter no maximo 30 caracteres.")
    private String telefoneContato;

    @Column(name = "email_contato", length = 255)
    @Email(message = "Informe um email de contato valido.")
    @Size(max = 255, message = "Email de contato deve ter no maximo 255 caracteres.")
    private String emailContato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_empresa_usuario"))
    private Usuario usuario;

    public Empresa() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getNomeContato() {
        return nomeContato;
    }

    public void setNomeContato(String nomeContato) {
        this.nomeContato = nomeContato;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public String getEmailContato() {
        return emailContato;
    }

    public void setEmailContato(String emailContato) {
        this.emailContato = emailContato;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
