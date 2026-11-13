package com.djelog.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ControleFinanceiroMotoristaRequest(
        @NotNull LocalDate dataInicioControle,
        @NotNull @DecimalMin(value = "0.00") BigDecimal saldoInicialPendente
) {}
