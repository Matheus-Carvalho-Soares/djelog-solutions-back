package com.djelog.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateDTO {

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
    private String nome;

    @Email(message = "Informe um email valido.")
    @NotBlank(message = "Informe seu email.")
    @Size(max = 255, message = "Email deve ter no maximo 255 caracteres.")
    private String email;

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
}
