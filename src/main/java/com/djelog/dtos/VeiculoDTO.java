package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class VeiculoDTO {
    private UUID id;

    @NotBlank
    @Size(max = 80)
    private String marca;

    @Min(1900)
    @Max(2100)
    private Integer ano;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9-]{7,10}$")
    private String placa;

    @Size(max = 120)
    private String nome;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal qtdPeso;

    @Valid
    @NotNull
    private ProfissionalDTO profissional;
    private Boolean status;

    public VeiculoDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getQtdPeso() {
        return qtdPeso;
    }

    public void setQtdPeso(BigDecimal qtdPeso) {
        this.qtdPeso = qtdPeso;
    }

    public ProfissionalDTO getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalDTO profissional) {
        this.profissional = profissional;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
