package com.djelog.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "viagem_comissionada")
public class ViagemComissionada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inicio_frete", length = 100)
    private String inicioFrete;

    @Column(name = "fim_frete", length = 100)
    private String fimFrete;

    @Column
    private Integer valor;

    @Column
    private Integer comissao;

    @Column
    private String descricao;

    public ViagemComissionada() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInicioFrete() {
        return inicioFrete;
    }

    public void setInicioFrete(String inicioFrete) {
        this.inicioFrete = inicioFrete;
    }

    public String getFimFrete() {
        return fimFrete;
    }

    public void setFimFrete(String fimFrete) {
        this.fimFrete = fimFrete;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public Integer getComissao() {
        return comissao;
    }

    public void setComissao(Integer comissao) {
        this.comissao = comissao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
