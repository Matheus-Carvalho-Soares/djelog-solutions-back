package com.djelog.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ProfissionalDTO {
    private UUID id;

    @NotBlank(message = "Informe o nome do motorista.")
    @Size(max = 120, message = "Nome do motorista deve ter no maximo 120 caracteres.")
    private String nome;

    @Size(max = 30, message = "Telefone deve ter no maximo 30 caracteres.")
    private String telefone;
    private Boolean status;

    public ProfissionalDTO() {
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
