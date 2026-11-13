package com.djelog.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MotoristaFinanceiroDTO(
        UUID profissionalId,
        String nome,
        boolean controleInicializado,
        LocalDate dataInicioControle,
        BigDecimal saldoInicial,
        BigDecimal saldoInicialPago,
        BigDecimal saldoInicialRestante,
        LocalDate inicioPeriodo,
        LocalDate fimPeriodo,
        List<VeiculoFiltro> veiculos,
        Resumo resumo,
        List<Semana> semanas,
        List<Viagem> viagens,
        List<Pagamento> pagamentos
) {
    public record Resumo(BigDecimal comissaoSemanaAtual, BigDecimal mediaSemanal,
                         BigDecimal saldoEmAberto, BigDecimal pagoNoPeriodo,
                         BigDecimal comissaoLiberadaPeriodo, BigDecimal comissaoProjetadaPeriodo,
                         int viagensConcluidas) {}
    public record VeiculoFiltro(UUID id, String nome) {}
    public record Semana(LocalDate inicio, BigDecimal comissaoGerada, BigDecimal pago,
                         BigDecimal saldoAbertoAtual) {}
    public record Viagem(UUID id, UUID veiculoId, LocalDate data, String rota, String veiculo, String status,
                         BigDecimal frete, BigDecimal comissao, BigDecimal pago,
                         BigDecimal saldo, boolean previsao) {}
    public record Pagamento(UUID id, LocalDate data, BigDecimal valor, String forma,
                            String referencia, String observacao, List<String> viagens) {}
}
