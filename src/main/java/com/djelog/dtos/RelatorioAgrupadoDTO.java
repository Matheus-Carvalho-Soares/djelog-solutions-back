package com.djelog.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class RelatorioAgrupadoDTO {
    private UUID grupoId;
    private String grupoNome;
    private String grupoDetalhe;
    private Integer quantidadeViagens;
    private BigDecimal valorFrete;
    private BigDecimal totalEstadias;
    private BigDecimal receitaTotal;
    private BigDecimal comissao;
    private BigDecimal totalDespesas;
    private BigDecimal lucroLiquido;

    public RelatorioAgrupadoDTO() {
    }

    public RelatorioAgrupadoDTO(
            UUID grupoId,
            String grupoNome,
            String grupoDetalhe,
            Integer quantidadeViagens,
            BigDecimal valorFrete,
            BigDecimal totalEstadias,
            BigDecimal receitaTotal,
            BigDecimal comissao,
            BigDecimal totalDespesas,
            BigDecimal lucroLiquido
    ) {
        this.grupoId = grupoId;
        this.grupoNome = grupoNome;
        this.grupoDetalhe = grupoDetalhe;
        this.quantidadeViagens = quantidadeViagens;
        this.valorFrete = valorFrete;
        this.totalEstadias = totalEstadias;
        this.receitaTotal = receitaTotal;
        this.comissao = comissao;
        this.totalDespesas = totalDespesas;
        this.lucroLiquido = lucroLiquido;
    }

    public UUID getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(UUID grupoId) {
        this.grupoId = grupoId;
    }

    public String getGrupoNome() {
        return grupoNome;
    }

    public void setGrupoNome(String grupoNome) {
        this.grupoNome = grupoNome;
    }

    public String getGrupoDetalhe() {
        return grupoDetalhe;
    }

    public void setGrupoDetalhe(String grupoDetalhe) {
        this.grupoDetalhe = grupoDetalhe;
    }

    public Integer getQuantidadeViagens() {
        return quantidadeViagens;
    }

    public void setQuantidadeViagens(Integer quantidadeViagens) {
        this.quantidadeViagens = quantidadeViagens;
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

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public void setTotalDespesas(BigDecimal totalDespesas) {
        this.totalDespesas = totalDespesas;
    }

    public BigDecimal getLucroLiquido() {
        return lucroLiquido;
    }

    public void setLucroLiquido(BigDecimal lucroLiquido) {
        this.lucroLiquido = lucroLiquido;
    }
}
