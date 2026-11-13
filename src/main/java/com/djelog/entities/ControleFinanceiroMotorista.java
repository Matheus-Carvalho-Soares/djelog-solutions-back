package com.djelog.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "controle_financeiro_motorista")
public class ControleFinanceiroMotorista {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false, unique = true)
    private Profissional profissional;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "data_inicio_controle", nullable = false)
    private LocalDate dataInicioControle;
    @Column(name = "saldo_inicial_pendente", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoInicialPendente = BigDecimal.ZERO;
    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @PrePersist void beforeInsert() { if (criadoEm == null) criadoEm = OffsetDateTime.now(); }
    public UUID getId() { return id; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDate getDataInicioControle() { return dataInicioControle; }
    public void setDataInicioControle(LocalDate dataInicioControle) { this.dataInicioControle = dataInicioControle; }
    public BigDecimal getSaldoInicialPendente() { return saldoInicialPendente; }
    public void setSaldoInicialPendente(BigDecimal saldoInicialPendente) { this.saldoInicialPendente = saldoInicialPendente; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
