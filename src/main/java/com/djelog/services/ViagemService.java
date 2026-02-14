package com.djelog.services;

import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.entities.Viagem;
import com.djelog.repositories.ViagemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ViagemService {

    private final ViagemRepository viagemRepository;

    public ViagemService(ViagemRepository viagemRepository) {
        this.viagemRepository = viagemRepository;
    }

    public List<ViagemRelatorioDTO> findDadosByDataInicioFim(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Viagem> viagens = viagemRepository.findByPeriodoSobreposto(usuarioId, dataInicio, dataFim);
        return viagens.stream().map(this::toRelatorioDto).toList();
    }
    private ViagemRelatorioDTO toRelatorioDto(Viagem v) {
        // calcula e sobrescreve o valor de comissão na entidade
        BigDecimal comissaoCalculada = v.getValorFrete().multiply(new BigDecimal("0.10"));
        v.setComissao(comissaoCalculada);

        // usa o novo valor para o DTO
        ViagemRelatorioDTO dto = new ViagemRelatorioDTO(
                v.getDataInicio(),
                v.getDataFim(),
                v.getStatus(),
                v.getLocalizacaoFrete(),
                v.getValorFrete(),
                comissaoCalculada, // valor calculado
                v.getAbastecimento(),
                v.getDespesas(),
                v.getProfissional() != null ? v.getProfissional().getNome() : null,
                v.getEmpresa() != null ? v.getEmpresa().getNome() : null,
                v.getVeiculo() != null ? v.getVeiculo().getMarca() : null,
                v.getVeiculo() != null ? v.getVeiculo().getPlaca() : null
        );

        BigDecimal valorFrete = defaultZero(v.getValorFrete());
        BigDecimal comissao = defaultZero(comissaoCalculada);
        BigDecimal abastecimento = defaultZero(v.getAbastecimento());
        BigDecimal despesas = defaultZero(v.getDespesas());

        dto.setLucroLiquido(valorFrete.subtract(comissao.add(abastecimento).add(despesas)));

        return dto;
    }
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
