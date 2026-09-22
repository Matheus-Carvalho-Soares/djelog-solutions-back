package com.djelog.dtos;

import java.util.UUID;

public record ConfirmacaoLinhaImportacaoDTO(
        UUID linhaId, boolean incluir, UUID veiculoId, UUID profissionalId,
        UUID empresaId, String status
) { }
