package com.djelog.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recebimento_frete")
public class RecebimentoFrete {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viagem_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recebimento_frete_viagem"))
    private Viagem viagem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;

    @Column(nullable = false)
    private boolean inicial;

    @Column(length = 500)
    private String observacao;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Viagem getViagem() { return viagem; }
    public void setViagem(Viagem viagem) { this.viagem = viagem; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(LocalDate dataRecebimento) { this.dataRecebimento = dataRecebimento; }
    public boolean isInicial() { return inicial; }
    public void setInicial(boolean inicial) { this.inicial = inicial; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
