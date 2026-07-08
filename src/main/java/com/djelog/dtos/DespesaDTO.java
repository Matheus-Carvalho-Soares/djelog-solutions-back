package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class DespesaDTO {
    private UUID id;

    @Valid
    @NotNull(message = "Selecione uma viagem para a despesa.")
    private ViagemDTO viagem;

    @NotBlank(message = "Informe o nome da despesa.")
    @Size(max = 100, message = "Nome da despesa deve ter no maximo 100 caracteres.")
    private String nome;

    @Size(max = 2000, message = "Descricao deve ter no maximo 2000 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o valor da despesa.")
    @Min(value = 0, message = "Valor da despesa deve ser maior ou igual a zero.")
    private Integer valor;

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

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }
}
