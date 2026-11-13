package com.djelog.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pagamento_motorista")
public class PagamentoMotorista {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private String formaPagamento;
    @Column(length = 120)
    private String referencia;
    @Column(length = 500)
    private String observacao;
    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;
    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoMotoristaItem> itens = new ArrayList<>();

    @PrePersist void beforeInsert() { if (criadoEm == null) criadoEm = OffsetDateTime.now(); }
    public void addItem(PagamentoMotoristaItem item) { item.setPagamento(this); itens.add(item); }
    public UUID getId() { return id; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public List<PagamentoMotoristaItem> getItens() { return itens; }
}
