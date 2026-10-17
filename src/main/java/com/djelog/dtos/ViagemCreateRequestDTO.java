package com.djelog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Input contract for creating a trip and its initial freight receipt. */
public class ViagemCreateRequestDTO {
    @NotNull private UUID profissionalId;
    private UUID empresaId;
    @NotNull private UUID veiculoId;

    @NotBlank @Size(max = 120)
    private String inicioFrete;
    @Size(max = 120)
    private String fimFrete;
    @Size(max = 120)
    private String paradaIntermediaria;
    private Long inicioMunicipioCodigo;
    private Long inicioUfCodigo;
    private Long paradaMunicipioCodigo;
    private Long paradaUfCodigo;
    private Long fimMunicipioCodigo;
    private Long fimUfCodigo;

    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2)
    private BigDecimal valorFrete;
    @DecimalMin(value = "0.00") @Digits(integer = 3, fraction = 2)
    private BigDecimal comissao = new BigDecimal("15.00");
    @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2)
    private BigDecimal valorComissaoManual;

    @NotBlank @Pattern(regexp = "A_VISTA|ADIANTAMENTO")
    private String formaPagamentoFrete;
    @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2)
    private BigDecimal valorAdiantamento;
    @NotNull
    private LocalDate dataRecebimentoInicial;

    @NotNull
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    @NotBlank @Size(max = 50)
    private String status;

    public UUID getProfissionalId() { return profissionalId; }
    public void setProfissionalId(UUID profissionalId) { this.profissionalId = profissionalId; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public UUID getVeiculoId() { return veiculoId; }
    public void setVeiculoId(UUID veiculoId) { this.veiculoId = veiculoId; }
    public String getInicioFrete() { return inicioFrete; }
    public void setInicioFrete(String inicioFrete) { this.inicioFrete = inicioFrete; }
    public String getFimFrete() { return fimFrete; }
    public void setFimFrete(String fimFrete) { this.fimFrete = fimFrete; }
    public String getParadaIntermediaria() { return paradaIntermediaria; }
    public void setParadaIntermediaria(String paradaIntermediaria) { this.paradaIntermediaria = paradaIntermediaria; }
    public Long getInicioMunicipioCodigo() { return inicioMunicipioCodigo; }
    public void setInicioMunicipioCodigo(Long inicioMunicipioCodigo) { this.inicioMunicipioCodigo = inicioMunicipioCodigo; }
    public Long getInicioUfCodigo() { return inicioUfCodigo; }
    public void setInicioUfCodigo(Long inicioUfCodigo) { this.inicioUfCodigo = inicioUfCodigo; }
    public Long getParadaMunicipioCodigo() { return paradaMunicipioCodigo; }
    public void setParadaMunicipioCodigo(Long paradaMunicipioCodigo) { this.paradaMunicipioCodigo = paradaMunicipioCodigo; }
    public Long getParadaUfCodigo() { return paradaUfCodigo; }
    public void setParadaUfCodigo(Long paradaUfCodigo) { this.paradaUfCodigo = paradaUfCodigo; }
    public Long getFimMunicipioCodigo() { return fimMunicipioCodigo; }
    public void setFimMunicipioCodigo(Long fimMunicipioCodigo) { this.fimMunicipioCodigo = fimMunicipioCodigo; }
    public Long getFimUfCodigo() { return fimUfCodigo; }
    public void setFimUfCodigo(Long fimUfCodigo) { this.fimUfCodigo = fimUfCodigo; }
    public BigDecimal getValorFrete() { return valorFrete; }
    public void setValorFrete(BigDecimal valorFrete) { this.valorFrete = valorFrete; }
    public BigDecimal getComissao() { return comissao; }
    public void setComissao(BigDecimal comissao) { this.comissao = comissao; }
    public BigDecimal getValorComissaoManual() { return valorComissaoManual; }
    public void setValorComissaoManual(BigDecimal valorComissaoManual) { this.valorComissaoManual = valorComissaoManual; }
    public String getFormaPagamentoFrete() { return formaPagamentoFrete; }
    public void setFormaPagamentoFrete(String formaPagamentoFrete) { this.formaPagamentoFrete = formaPagamentoFrete; }
    public BigDecimal getValorAdiantamento() { return valorAdiantamento; }
    public void setValorAdiantamento(BigDecimal valorAdiantamento) { this.valorAdiantamento = valorAdiantamento; }
    public LocalDate getDataRecebimentoInicial() { return dataRecebimentoInicial; }
    public void setDataRecebimentoInicial(LocalDate dataRecebimentoInicial) { this.dataRecebimentoInicial = dataRecebimentoInicial; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
