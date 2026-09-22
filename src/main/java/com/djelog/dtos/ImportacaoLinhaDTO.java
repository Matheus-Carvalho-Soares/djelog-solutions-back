package com.djelog.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ImportacaoLinhaDTO(
        UUID id, int numeroLinha, LocalDate data, String placa, String empresaOriginal,
        String inicioFrete, String paradaIntermediaria, String fimFrete, BigDecimal valorFrete,
        BigDecimal comissao, BigDecimal abastecimento, BigDecimal pedagio, BigDecimal outrasDespesas,
        String descricaoOutras, UUID veiculoId, UUID profissionalId, UUID empresaId, String statusViagem,
        String situacao, String motivo, boolean selecionada, List<String> avisos,
        List<ImportacaoSugestaoDTO> semelhantes
) { }
