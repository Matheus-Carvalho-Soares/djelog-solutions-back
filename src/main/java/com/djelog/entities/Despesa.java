package com.djelog.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "despesas")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_viagem", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_despesas_viagem"))
    private Viagem viagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_despesas_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veiculo", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_despesas_veiculo"))
    private Veiculo veiculo;

    @Column(length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 40)
    private String categoria;

    @Column(name = "data_despesa")
    private LocalDate dataDespesa;

    @Column(length = 150)
    private String recebedor;

    @OneToMany(mappedBy = "despesa", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataVencimento asc, numeroParcela asc")
    private java.util.List<ParcelaDespesa> parcelas = new java.util.ArrayList<>();

    public Despesa() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Viagem getViagem() {
        return viagem;
    }

    public void setViagem(Viagem viagem) {
        this.viagem = viagem;
    }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public LocalDate getDataDespesa() { return dataDespesa; }
    public void setDataDespesa(LocalDate dataDespesa) { this.dataDespesa = dataDespesa; }
    public String getRecebedor() { return recebedor; }
    public void setRecebedor(String recebedor) { this.recebedor = recebedor; }
    public java.util.List<ParcelaDespesa> getParcelas() { return parcelas; }
    public void setParcelas(java.util.List<ParcelaDespesa> parcelas) {
        this.parcelas.clear();
        if (parcelas != null) parcelas.forEach(this::addParcela);
    }
    public void addParcela(ParcelaDespesa parcela) { parcela.setDespesa(this); this.parcelas.add(parcela); }

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
}
