package com.djelog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class RecebimentoFreteDTO {
    private UUID id;
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2)
    private BigDecimal valor;
    @NotNull
    private LocalDate dataRecebimento;
    private boolean inicial;
    private String observacao;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(LocalDate dataRecebimento) { this.dataRecebimento = dataRecebimento; }
    public boolean isInicial() { return inicial; }
    public void setInicial(boolean inicial) { this.inicial = inicial; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
