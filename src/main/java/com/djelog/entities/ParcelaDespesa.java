package com.djelog.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "despesa_parcela")
public class ParcelaDespesa {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "despesa_id", nullable = false, foreignKey = @ForeignKey(name = "fk_despesa_parcela_despesa"))
    private Despesa despesa;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "status_pagamento", nullable = false, length = 20)
    private String statusPagamento = "PENDENTE";

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela = 1;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Despesa getDespesa() { return despesa; }
    public void setDespesa(Despesa despesa) { this.despesa = despesa; }
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
