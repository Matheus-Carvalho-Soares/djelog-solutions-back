package com.djelog.dtos;

import java.util.List;
import java.util.UUID;

public record ResultadoImportacaoDTO(UUID importacaoId, int criadas, int ignoradas, List<String> avisos) { }
