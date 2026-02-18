package com.djelog.services;

import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.dtos.DespesaDTO;
import com.djelog.entities.Viagem;
import com.djelog.repositories.ViagemRepository;
import com.djelog.repositories.DespesaRepository;
import com.djelog.services.DespesaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final DespesaRepository despesaRepository;
    private final DespesaService despesaService;

    public ViagemService(ViagemRepository viagemRepository, DespesaRepository despesaRepository, DespesaService despesaService) {
        this.viagemRepository = viagemRepository;
        this.despesaRepository = despesaRepository;
        this.despesaService = despesaService;
    }

    public List<ViagemRelatorioDTO> findDadosByDataInicioFim(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Viagem> viagens = viagemRepository.findByPeriodoSobreposto(usuarioId, dataInicio, dataFim);
        return viagens.stream().map(this::toRelatorioDto).toList();
    }
    private ViagemRelatorioDTO toRelatorioDto(Viagem v) {
        // calcula e sobrescreve o valor de comissão na entidade
        BigDecimal comissaoCalculada = v.getValorFrete().multiply(new BigDecimal("0.10"));
        v.setComissao(comissaoCalculada);

        // busca as despesas relacionadas à viagem
        List<DespesaDTO> despesas = despesaRepository.findByViagemIdWithViagem(v.getId()).stream()
                .map(despesa -> {
                    DespesaDTO dto = new DespesaDTO();
                    dto.setId(despesa.getId());
                    dto.setNome(despesa.getNome());
                    dto.setDescricao(despesa.getDescricao());
                    dto.setValor(despesa.getValor());
                    return dto;
                })
                .toList();

        // usa o novo valor para o DTO
        ViagemRelatorioDTO dto = new ViagemRelatorioDTO(
                v.getDataInicio(),
                v.getDataFim(),
                v.getStatus(),
                v.getInicioFrete(),
                v.getFimFrete(),
                v.getValorFrete(),
                comissaoCalculada,
                despesas,
                v.getProfissional() != null ? v.getProfissional().getNome() : null,
                v.getEmpresa() != null ? v.getEmpresa().getNome() : null,
                v.getVeiculo() != null ? v.getVeiculo().getMarca() : null,
                v.getVeiculo() != null ? v.getVeiculo().getPlaca() : null
        );

        BigDecimal valorFrete = defaultZero(v.getValorFrete());
        BigDecimal comissao = defaultZero(comissaoCalculada);
        BigDecimal totalDespesas = defaultZero(dto.getTotalDespesas());

        dto.setLucroLiquido(valorFrete.subtract(comissao.add(totalDespesas)));

        return dto;
    }
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
