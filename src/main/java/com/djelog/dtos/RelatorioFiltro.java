package com.djelog.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RelatorioFiltro(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        List<UUID> veiculoIds,
        List<UUID> profissionalIds,
        List<UUID> empresaIds,
        List<String> status,
        String sortBy,
        String sortDirection,
        String busca
) {
    public RelatorioFiltro {
        veiculoIds = immutableList(veiculoIds);
        profissionalIds = immutableList(profissionalIds);
        empresaIds = immutableList(empresaIds);
        status = immutableList(status);
        sortBy = sortBy == null || sortBy.isBlank() ? "receitaTotal" : sortBy;
        sortDirection = sortDirection == null || sortDirection.isBlank() ? "desc" : sortDirection;
        busca = busca == null ? "" : busca.trim();
    }

    public RelatorioFiltro(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            List<UUID> veiculoIds,
            List<UUID> profissionalIds,
            List<UUID> empresaIds,
            List<String> status,
            String sortBy,
            String sortDirection
    ) {
        this(dataInicio, dataFim, veiculoIds, profissionalIds, empresaIds, status, sortBy, sortDirection, "");
    }

    public boolean filtraVeiculos() {
        return !veiculoIds.isEmpty();
    }

    public boolean filtraProfissionais() {
        return !profissionalIds.isEmpty();
    }

    public boolean filtraEmpresas() {
        return !empresaIds.isEmpty();
    }

    public boolean filtraStatus() {
        return !status.isEmpty();
    }

    public boolean temBusca() {
        return !busca.isBlank();
    }

    public RelatorioFiltro comVeiculo(UUID veiculoId) {
        return new RelatorioFiltro(
                dataInicio,
                dataFim,
                List.of(veiculoId),
                profissionalIds,
                empresaIds,
                status,
                sortBy,
                sortDirection,
                busca
        );
    }

    public RelatorioFiltro comProfissional(UUID profissionalId) {
        return new RelatorioFiltro(
                dataInicio,
                dataFim,
                veiculoIds,
                List.of(profissionalId),
                empresaIds,
                status,
                sortBy,
                sortDirection,
                busca
        );
    }

    private static <T> List<T> immutableList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
