package com.djelog.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "viagem_comissionada")
public class ViagemComissionada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inicio_frete", length = 100)
    @NotBlank
    @Size(max = 100)
    private String inicioFrete;

    @Column(name = "fim_frete", length = 100)
    @Size(max = 100)
    private String fimFrete;

    @Column
    @NotNull
    @Min(0)
    private Integer valor;

    @Column
    @NotNull
    @Min(0)
    private Integer comissao;

    @Column
    @Size(max = 2000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_viagem_comissionada_usuario"))
    private Usuario usuario;

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
