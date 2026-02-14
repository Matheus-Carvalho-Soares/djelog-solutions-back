package com.djelog.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ViagemDTO {
    private UUID id;
    private ProfissionalDTO profissional;
    private EmpresaDTO empresa;
    private VeiculoDTO veiculo;
    private String localizacaoFrete;
    private BigDecimal valorFrete;
    private BigDecimal comissao;
    private BigDecimal abastecimento;
    private BigDecimal despesas;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
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

    public String getLocalizacaoFrete() {
        return localizacaoFrete;
    }

    public void setLocalizacaoFrete(String localizacaoFrete) {
        this.localizacaoFrete = localizacaoFrete;
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

    public BigDecimal getAbastecimento() {
        return abastecimento;
    }

    public void setAbastecimento(BigDecimal abastecimento) {
        this.abastecimento = abastecimento;
    }

    public BigDecimal getDespesas() {
        return despesas;
    }

    public void setDespesas(BigDecimal despesas) {
        this.despesas = despesas;
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
