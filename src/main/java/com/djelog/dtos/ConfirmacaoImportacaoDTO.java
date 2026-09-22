package com.djelog.dtos;

import java.util.List;

public record ConfirmacaoImportacaoDTO(boolean confirmarDuplicatasExatas, List<ConfirmacaoLinhaImportacaoDTO> linhas) { }
