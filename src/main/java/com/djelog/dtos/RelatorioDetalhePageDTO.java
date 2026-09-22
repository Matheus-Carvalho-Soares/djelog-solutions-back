package com.djelog.dtos;

import java.util.List;

public record RelatorioDetalhePageDTO(
        List<ViagemRelatorioDTO> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
    public RelatorioDetalhePageDTO {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
