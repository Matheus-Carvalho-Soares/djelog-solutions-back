package com.djelog.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ViagemRelatorioDTO {
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String status;
    private String localizacaoFrete;
    private BigDecimal valorFrete;
    private BigDecimal comissao;
    private BigDecimal abastecimento;
    private BigDecimal despesas;
    private String profissionalNome;
    private String empresaNome;
    private String veiculoMarca;
    private String veiculoPlaca;
    private BigDecimal lucroLiquido;

    public ViagemRelatorioDTO(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String status,
            String localizacaoFrete,
            BigDecimal valorFrete,
            BigDecimal comissao,
            BigDecimal abastecimento,
            BigDecimal despesas,
            String profissionalNome,
            String empresaNome,
            String veiculoMarca,
            String veiculoPlaca
    ) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.localizacaoFrete = localizacaoFrete;
        this.valorFrete = valorFrete;
        this.comissao = comissao;
        this.abastecimento = abastecimento;
        this.despesas = despesas;
        this.profissionalNome = profissionalNome;
        this.empresaNome = empresaNome;
        this.veiculoMarca = veiculoMarca;
        this.veiculoPlaca = veiculoPlaca;
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

    public String getProfissionalNome() {
        return profissionalNome;
    }

    public void setProfissionalNome(String profissionalNome) {
        this.profissionalNome = profissionalNome;
    }

    public String getEmpresaNome() {
        return empresaNome;
    }

    public void setEmpresaNome(String empresaNome) {
        this.empresaNome = empresaNome;
    }

    public String getVeiculoMarca() {
        return veiculoMarca;
    }

    public void setVeiculoMarca(String veiculoMarca) {
        this.veiculoMarca = veiculoMarca;
    }

    public String getVeiculoPlaca() {
        return veiculoPlaca;
    }

    public void setVeiculoPlaca(String veiculoPlaca) {
        this.veiculoPlaca = veiculoPlaca;
    }

    public BigDecimal getLucroLiquido() {
        return lucroLiquido;
    }

    public void setLucroLiquido(BigDecimal lucroLiquido) {
        this.lucroLiquido = lucroLiquido;
    }
}
