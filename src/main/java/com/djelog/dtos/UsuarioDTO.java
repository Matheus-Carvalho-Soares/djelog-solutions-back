package com.djelog.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class UsuarioDTO {
    
    private UUID id;

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
    private String nome;

    @Email(message = "Informe um email valido.")
    @NotBlank(message = "Informe seu email.")
    @Size(max = 255, message = "Email deve ter no maximo 255 caracteres.")
    private String email;

    @NotBlank(message = "Informe uma senha.")
    @Size(min = 10, max = 128, message = "Senha deve ter entre 10 e 128 caracteres.")
    private String senha;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

}
