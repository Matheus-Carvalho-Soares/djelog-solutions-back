package com.djelog.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "viagem")
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profissional", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_viagem_profissional"))
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_viagem_empresa"))
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veiculo", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_viagem_veiculo"))
    private Veiculo veiculo;

    @Column(name = "localizacao_frete", nullable = false)
    private String localizacaoFrete;

    @Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete;

    @Column(precision = 10, scale = 2)
    private BigDecimal comissao;

    @Column(precision = 10, scale = 2)
    private BigDecimal abastecimento;

    @Column(precision = 10, scale = 2)
    private BigDecimal despesas;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(nullable = false, length = 50)
    private String status;

    public Viagem() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
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

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
