package com.djelog.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "estadia")
public class Estadia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_viagem", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_despesas_viagem"))
    private Viagem viagem;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column
    private Integer valor;

    public Estadia() {
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }
}
