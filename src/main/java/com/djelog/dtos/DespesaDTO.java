package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class DespesaDTO {
    private UUID id;

    @Valid
    @NotNull
    private ViagemDTO viagem;

    @NotBlank
    @Size(max = 100)
    private String nome;

    @Size(max = 2000)
    private String descricao;

    @NotNull
    @Min(0)
    private Integer valor;

    public DespesaDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ViagemDTO getViagem() {
        return viagem;
    }

    public void setViagem(ViagemDTO viagem) {
        this.viagem = viagem;
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

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }
}
