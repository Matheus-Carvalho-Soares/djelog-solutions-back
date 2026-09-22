package com.djelog.dtos;

import java.util.UUID;

public record ImportacaoCatalogoDTO(UUID id, String nome, String identificador, UUID profissionalId) { }
