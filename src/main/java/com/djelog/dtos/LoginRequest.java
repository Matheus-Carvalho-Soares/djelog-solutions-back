package com.djelog.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @Email(message = "Informe um email valido.")
    @NotBlank(message = "Informe seu email.")
    @Size(max = 255, message = "Email deve ter no maximo 255 caracteres.")
    private String email;

    @NotBlank(message = "Informe sua senha.")
    @Size(min = 8, max = 128, message = "Senha deve ter entre 8 e 128 caracteres.")
    private String senha;

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
