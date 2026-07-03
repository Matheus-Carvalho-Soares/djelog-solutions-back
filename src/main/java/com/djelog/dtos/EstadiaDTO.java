package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class EstadiaDTO {
    private UUID id;

    @Valid
    @NotNull
    private ViagemDTO viagem;

    @Size(max = 2000)
    private String descricao;

    @NotNull
    @Min(0)
    private Integer valor;

    public EstadiaDTO() {
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
