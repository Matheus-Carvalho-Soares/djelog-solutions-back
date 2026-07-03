package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ViagemDTO {
    private UUID id;

    @Valid
    @NotNull
    private ProfissionalDTO profissional;

    @Valid
    @NotNull
    private EmpresaDTO empresa;

    @Valid
    @NotNull
    private VeiculoDTO veiculo;

    @NotBlank
    @Size(max = 120)
    private String inicioFrete;

    @Size(max = 120)
    private String fimFrete;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal valorFrete;

    @DecimalMin(value = "0.0")
    private BigDecimal comissao;

    @NotNull
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @NotBlank
    @Size(max = 50)
    private String status;

    public ViagemDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProfissionalDTO getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalDTO profissional) {
        this.profissional = profissional;
    }

    public EmpresaDTO getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaDTO empresa) {
        this.empresa = empresa;
    }

    public VeiculoDTO getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(VeiculoDTO veiculo) {
        this.veiculo = veiculo;
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

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getComissao() {
        return comissao;
    }

    public void setComissao(BigDecimal comissao) {
        this.comissao = comissao;
    }


    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
