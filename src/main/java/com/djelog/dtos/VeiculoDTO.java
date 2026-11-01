package com.djelog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class VeiculoDTO {
    private UUID id;

    @Size(max = 80, message = "Marca deve ter no maximo 80 caracteres.")
    private String marca;

    private String tipo = "CAMINHAO";
    @Size(max = 80, message = "Modelo deve ter no maximo 80 caracteres.")
    private String modelo;
    private UUID caminhaoId;

    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900.")
    @Max(value = 2100, message = "Ano deve ser menor ou igual a 2100.")
    private Integer ano;

    @NotBlank(message = "Informe a placa do veiculo.")
    @Pattern(regexp = "^[A-Za-z0-9-]{7,10}$", message = "Placa deve ter de 7 a 10 caracteres, usando letras, numeros ou hifen.")
    private String placa;

    @Size(max = 120, message = "Nome do veiculo deve ter no maximo 120 caracteres.")
    private String nome;

    @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero.")
    private BigDecimal qtdPeso;

    private ProfissionalDTO profissional;
    private ProfissionalDTO proprietario;
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

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public UUID getCaminhaoId() { return caminhaoId; }
    public void setCaminhaoId(UUID caminhaoId) { this.caminhaoId = caminhaoId; }

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

    public ProfissionalDTO getProprietario() { return proprietario; }
    public void setProprietario(ProfissionalDTO proprietario) { this.proprietario = proprietario; }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
