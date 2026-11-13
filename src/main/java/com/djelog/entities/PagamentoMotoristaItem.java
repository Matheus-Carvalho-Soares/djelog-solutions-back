package com.djelog.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pagamento_motorista_item")
public class PagamentoMotoristaItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pagamento_id", nullable = false)
    private PagamentoMotorista pagamento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viagem_id")
    private Viagem viagem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controle_id")
    private ControleFinanceiroMotorista controle;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    public UUID getId() { return id; }
    public PagamentoMotorista getPagamento() { return pagamento; }
    public void setPagamento(PagamentoMotorista pagamento) { this.pagamento = pagamento; }
    public Viagem getViagem() { return viagem; }
    public void setViagem(Viagem viagem) { this.viagem = viagem; }
    public ControleFinanceiroMotorista getControle() { return controle; }
    public void setControle(ControleFinanceiroMotorista controle) { this.controle = controle; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}
