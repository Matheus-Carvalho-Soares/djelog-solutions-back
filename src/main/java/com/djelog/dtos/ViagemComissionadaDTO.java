package com.djelog.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ViagemComissionadaDTO {
    private UUID id;

    @NotBlank
    @Size(max = 100)
    private String inicioFrete;

    @Size(max = 100)
    private String fimFrete;

    @NotNull
    @Min(0)
    private Integer valor;

    @NotNull
    @Min(0)
    private Integer comissao;

    @Size(max = 2000)
    private String descricao;

    public ViagemComissionadaDTO() {
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInicioFrete() {
        return inicioFrete;
    }

    public void setInicioFrete(String inicioFrete) {
        this.inicioFrete = inicioFrete;
    }

    public String getFimFrete() {
        return fimFrete;
    }

    public void setFimFrete(String fimFrete) {
        this.fimFrete = fimFrete;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public Integer getComissao() {
        return comissao;
    }

    public void setComissao(Integer comissao) {
        this.comissao = comissao;
    }
}
