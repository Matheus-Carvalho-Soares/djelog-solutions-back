package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RegistrarPagamentoMotoristaRequest(
        @NotNull LocalDate dataPagamento,
        @NotBlank String formaPagamento,
        String referencia,
        String observacao,
        @NotEmpty List<@Valid Alocacao> alocacoes
) {
    public record Alocacao(UUID viagemId, Boolean saldoInicial, @NotNull BigDecimal valor) {}
}
