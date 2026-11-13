package com.djelog.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ItemPagamentoLoteDTO(
        @NotNull UUID despesaId,
        @NotNull UUID parcelaId,
        @NotNull LocalDate dataPagamento
) {}
