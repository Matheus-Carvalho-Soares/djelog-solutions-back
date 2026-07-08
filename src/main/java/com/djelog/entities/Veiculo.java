package com.djelog.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "veiculo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "Informe a marca do veiculo.")
    @Size(max = 80, message = "Marca deve ter no maximo 80 caracteres.")
    private String marca;

    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900.")
    @Max(value = 2100, message = "Ano deve ser menor ou igual a 2100.")
    private Integer ano;

    @Column(name = "placa", nullable = false, length = 10)
    @NotBlank(message = "Informe a placa do veiculo.")
    @Pattern(regexp = "^[A-Za-z0-9-]{7,10}$", message = "Placa deve ter de 7 a 10 caracteres, usando letras, numeros ou hifen.")
    private String placa;

    @Column(name = "nome")
    @Size(max = 120, message = "Nome do veiculo deve ter no maximo 120 caracteres.")
    private String nome;

    @Column(name = "qtd_peso", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero.")
    private BigDecimal qtdPeso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profissional", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_veiculo_profissional"))
    private Profissional profissional;

    private Boolean status;

    public Veiculo() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getQtdPeso() {
        return qtdPeso;
    }

    public void setQtdPeso(BigDecimal qtdPeso) {
        this.qtdPeso = qtdPeso;
    }
}
