package com.djelog.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ViagemRelatorioDTO {
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String status;
    private String inicioFrete;
    private String fimFrete;
    private BigDecimal valorFrete;
    private BigDecimal totalEstadias;
    private BigDecimal receitaTotal;
    private BigDecimal comissao;
    private List<DespesaDTO> despesas;
    private BigDecimal totalDespesas;
    private String profissionalNome;
    private String empresaNome;
    private String veiculoMarca;
    private String veiculoPlaca;
    private BigDecimal lucroLiquido;

    public ViagemRelatorioDTO(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String status,
            String inicioFrete,
            String fimFrete,
            BigDecimal valorFrete,
            BigDecimal comissao,
            List<DespesaDTO> despesas,
            String profissionalNome,
            String empresaNome,
            String veiculoMarca,
            String veiculoPlaca
    ) {
        this(
                dataInicio,
                dataFim,
                status,
                inicioFrete,
                fimFrete,
                valorFrete,
                BigDecimal.ZERO,
                valorFrete,
                comissao,
                despesas,
                profissionalNome,
                empresaNome,
                veiculoMarca,
                veiculoPlaca
        );
    }

    public ViagemRelatorioDTO(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String status,
            String inicioFrete,
            String fimFrete,
            BigDecimal valorFrete,
            BigDecimal totalEstadias,
            BigDecimal receitaTotal,
            BigDecimal comissao,
            List<DespesaDTO> despesas,
            String profissionalNome,
            String empresaNome,
            String veiculoMarca,
            String veiculoPlaca
    ) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.inicioFrete = inicioFrete;
        this.fimFrete = fimFrete;
        this.valorFrete = valorFrete;
        this.totalEstadias = defaultZero(totalEstadias);
        this.receitaTotal = defaultZero(receitaTotal);
        this.comissao = comissao;
        this.despesas = despesas;
        this.totalDespesas = calcularTotalDespesas(despesas);
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

    public BigDecimal getTotalEstadias() {
        return totalEstadias;
    }

    public void setTotalEstadias(BigDecimal totalEstadias) {
        this.totalEstadias = totalEstadias;
    }

    public BigDecimal getReceitaTotal() {
        return receitaTotal;
    }

    public void setReceitaTotal(BigDecimal receitaTotal) {
        this.receitaTotal = receitaTotal;
    }

    public BigDecimal getComissao() {
        return comissao;
    }

    public void setComissao(BigDecimal comissao) {
        this.comissao = comissao;
    }

    public List<DespesaDTO> getDespesas() {
        return despesas;
    }

    public void setDespesas(List<DespesaDTO> despesas) {
        this.despesas = despesas;
        this.totalDespesas = calcularTotalDespesas(despesas);
    }

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public void setTotalDespesas(BigDecimal totalDespesas) {
        this.totalDespesas = totalDespesas;
    }

    private BigDecimal calcularTotalDespesas(List<DespesaDTO> despesas) {
        if (despesas == null || despesas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return despesas.stream()
                .map(DespesaDTO::getValor)
                .filter(valor -> valor != null)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
