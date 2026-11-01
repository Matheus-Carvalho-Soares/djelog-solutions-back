package com.djelog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;

public class DespesaDTO {
    private UUID id;

    private ViagemDTO viagem;

    private VeiculoDTO veiculo;

    @NotBlank(message = "Informe o nome da despesa.")
    @Size(max = 100, message = "Nome da despesa deve ter no maximo 100 caracteres.")
    private String nome;

    @Size(max = 2000, message = "Descricao deve ter no maximo 2000 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o valor da despesa.")
    @DecimalMin(value = "0.0", message = "Valor da despesa deve ser maior ou igual a zero.")
    private BigDecimal valor;

    private String categoria;
    private LocalDate dataDespesa;
    @Size(max = 150, message = "Recebedor deve ter no maximo 150 caracteres.")
    private String recebedor;
    private List<ParcelaDespesaDTO> parcelas;
    private LocalDate dataVencimento;
    private String statusPagamento;
    private LocalDate dataPagamento;

    public DespesaDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ViagemDTO getViagem() {
        return viagem;
    }

    public void setViagem(ViagemDTO viagem) {
        this.viagem = viagem;
    }

    public VeiculoDTO getVeiculo() { return veiculo; }
    public void setVeiculo(VeiculoDTO veiculo) { this.veiculo = veiculo; }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public LocalDate getDataDespesa() { return dataDespesa; }
    public void setDataDespesa(LocalDate dataDespesa) { this.dataDespesa = dataDespesa; }
    public String getRecebedor() { return recebedor; }
    public void setRecebedor(String recebedor) { this.recebedor = recebedor; }
    public List<ParcelaDespesaDTO> getParcelas() { return parcelas; }
    public void setParcelas(List<ParcelaDespesaDTO> parcelas) { this.parcelas = parcelas; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
}
