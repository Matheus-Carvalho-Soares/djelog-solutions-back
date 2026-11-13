package com.djelog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ConfirmacaoPagamentoLoteDTO(@NotEmpty @Valid List<ItemPagamentoLoteDTO> parcelas) {}
