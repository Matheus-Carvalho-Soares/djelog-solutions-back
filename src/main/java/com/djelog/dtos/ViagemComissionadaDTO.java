package com.djelog.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ViagemComissionadaDTO {
    private UUID id;

    @NotBlank(message = "Informe o inicio do frete.")
    @Size(max = 100, message = "Inicio do frete deve ter no maximo 100 caracteres.")
    private String inicioFrete;

    @Size(max = 100, message = "Fim do frete deve ter no maximo 100 caracteres.")
    private String fimFrete;

    @NotNull(message = "Informe o valor da viagem.")
    @Min(value = 0, message = "Valor da viagem deve ser maior ou igual a zero.")
    private Integer valor;

    @NotNull(message = "Informe a comissao.")
    @Min(value = 0, message = "Comissao deve ser maior ou igual a zero.")
    private Integer comissao;

    @Size(max = 2000, message = "Descricao deve ter no maximo 2000 caracteres.")
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
