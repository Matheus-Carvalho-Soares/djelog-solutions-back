package com.djelog.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "viagem_importacao_linha", uniqueConstraints =
        @UniqueConstraint(name = "uk_viagem_import_linha", columnNames = {"id_importacao", "numero_linha"}))
public class ImportacaoViagemLinha {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_importacao", nullable = false)
    private ImportacaoViagem importacao;
    @Column(name = "numero_linha", nullable = false)
    private int numeroLinha;
    @Column(name = "data_viagem") private LocalDate dataViagem;
    @Column(length = 20) private String placa;
    @Column(name = "empresa_original", length = 255) private String empresaOriginal;
    @Column(name = "inicio_frete", length = 120) private String inicioFrete;
    @Column(name = "parada_intermediaria", length = 120) private String paradaIntermediaria;
    @Column(name = "fim_frete", length = 120) private String fimFrete;
    @Column(name = "valor_frete", precision = 10, scale = 2) private BigDecimal valorFrete;
    @Column(precision = 6, scale = 2) private BigDecimal comissao;
    @Column(precision = 10, scale = 2) private BigDecimal abastecimento;
    @Column(precision = 10, scale = 2) private BigDecimal pedagio;
    @Column(name = "outras_despesas", precision = 10, scale = 2) private BigDecimal outrasDespesas;
    @Column(name = "descricao_outras", length = 500) private String descricaoOutras;
    @Column(columnDefinition = "TEXT") private String avisos;
    @Column(nullable = false, length = 30) private String situacao;
    @Column(columnDefinition = "TEXT") private String motivo;
    @Column(length = 64) private String assinatura;
    @Column(nullable = false) private boolean selecionada;
    @Column(name = "id_veiculo") private UUID veiculoId;
    @Column(name = "id_profissional") private UUID profissionalId;
    @Column(name = "id_empresa") private UUID empresaId;
    @Column(name = "status_viagem", length = 50) private String statusViagem;
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_viagem") private Viagem viagem;

    public UUID getId() { return id; }
    public ImportacaoViagem getImportacao() { return importacao; }
    public void setImportacao(ImportacaoViagem importacao) { this.importacao = importacao; }
    public int getNumeroLinha() { return numeroLinha; }
    public void setNumeroLinha(int numeroLinha) { this.numeroLinha = numeroLinha; }
    public LocalDate getDataViagem() { return dataViagem; }
    public void setDataViagem(LocalDate dataViagem) { this.dataViagem = dataViagem; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getEmpresaOriginal() { return empresaOriginal; }
    public void setEmpresaOriginal(String empresaOriginal) { this.empresaOriginal = empresaOriginal; }
    public String getInicioFrete() { return inicioFrete; }
    public void setInicioFrete(String inicioFrete) { this.inicioFrete = inicioFrete; }
    public String getParadaIntermediaria() { return paradaIntermediaria; }
    public void setParadaIntermediaria(String paradaIntermediaria) { this.paradaIntermediaria = paradaIntermediaria; }
    public String getFimFrete() { return fimFrete; }
    public void setFimFrete(String fimFrete) { this.fimFrete = fimFrete; }
    public BigDecimal getValorFrete() { return valorFrete; }
    public void setValorFrete(BigDecimal valorFrete) { this.valorFrete = valorFrete; }
    public BigDecimal getComissao() { return comissao; }
    public void setComissao(BigDecimal comissao) { this.comissao = comissao; }
    public BigDecimal getAbastecimento() { return abastecimento; }
    public void setAbastecimento(BigDecimal abastecimento) { this.abastecimento = abastecimento; }
    public BigDecimal getPedagio() { return pedagio; }
    public void setPedagio(BigDecimal pedagio) { this.pedagio = pedagio; }
    public BigDecimal getOutrasDespesas() { return outrasDespesas; }
    public void setOutrasDespesas(BigDecimal outrasDespesas) { this.outrasDespesas = outrasDespesas; }
    public String getDescricaoOutras() { return descricaoOutras; }
    public void setDescricaoOutras(String descricaoOutras) { this.descricaoOutras = descricaoOutras; }
    public String getAvisos() { return avisos; }
    public void setAvisos(String avisos) { this.avisos = avisos; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getAssinatura() { return assinatura; }
    public void setAssinatura(String assinatura) { this.assinatura = assinatura; }
    public boolean isSelecionada() { return selecionada; }
    public void setSelecionada(boolean selecionada) { this.selecionada = selecionada; }
    public UUID getVeiculoId() { return veiculoId; }
    public void setVeiculoId(UUID veiculoId) { this.veiculoId = veiculoId; }
    public UUID getProfissionalId() { return profissionalId; }
    public void setProfissionalId(UUID profissionalId) { this.profissionalId = profissionalId; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public String getStatusViagem() { return statusViagem; }
    public void setStatusViagem(String statusViagem) { this.statusViagem = statusViagem; }
    public Viagem getViagem() { return viagem; }
    public void setViagem(Viagem viagem) { this.viagem = viagem; }
}
