package com.djelog.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ImportacaoViagemDTO(
        UUID id, String nomeArquivo, String status, LocalDateTime criadoEm,
        List<ImportacaoLinhaDTO> linhas, List<ImportacaoCatalogoDTO> veiculos,
        List<ImportacaoCatalogoDTO> profissionais, List<ImportacaoCatalogoDTO> empresas
) { }
