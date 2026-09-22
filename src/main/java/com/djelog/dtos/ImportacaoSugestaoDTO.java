package com.djelog.dtos;

import java.time.LocalDate;
import java.util.UUID;

public record ImportacaoSugestaoDTO(UUID viagemId, LocalDate data, String placa, String motorista,
                                    String empresa, String rota, int pontuacao, String motivo) { }
