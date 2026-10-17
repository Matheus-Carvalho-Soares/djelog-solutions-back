package com.djelog.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ParcelaDespesaDTO {
    private UUID id;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private String statusPagamento;
    private LocalDate dataPagamento;
    private Integer numeroParcela;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public Integer getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(Integer numeroParcela) { this.numeroParcela = numeroParcela; }
}
