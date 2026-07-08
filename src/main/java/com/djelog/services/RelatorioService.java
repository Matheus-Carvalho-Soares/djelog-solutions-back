package com.djelog.services;

import com.djelog.dtos.DespesaDTO;
import com.djelog.dtos.RelatorioAgrupadoDTO;
import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.entities.Despesa;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.EstadiaRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final ViagemRepository viagemRepository;
    private final DespesaRepository despesaRepository;
    private final EstadiaRepository estadiaRepository;

    public RelatorioService(
            ViagemRepository viagemRepository,
            DespesaRepository despesaRepository,
            EstadiaRepository estadiaRepository
    ) {
        this.viagemRepository = viagemRepository;
        this.despesaRepository = despesaRepository;
        this.estadiaRepository = estadiaRepository;
    }

    public List<ViagemRelatorioDTO> findDadosByDataInicioFim(
            UUID usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return buildRelatorioViagemItems(usuarioId, dataInicio, dataFim).stream()
                .map(RelatorioViagemItem::dto)
                .toList();
    }

    public List<RelatorioAgrupadoDTO> findFaturamentoPorVeiculo(
            UUID usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        List<RelatorioViagemItem> items = buildRelatorioViagemItems(usuarioId, dataInicio, dataFim);
        Map<UUID, List<RelatorioViagemItem>> porVeiculo = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.viagem().getVeiculo().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return porVeiculo.values().stream()
                .map(this::toRelatorioPorVeiculo)
                .toList();
    }

    public List<RelatorioAgrupadoDTO> findFaturamentoPorProfissional(
            UUID usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        List<RelatorioViagemItem> items = buildRelatorioViagemItems(usuarioId, dataInicio, dataFim);
        Map<UUID, List<RelatorioViagemItem>> porProfissional = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.viagem().getProfissional().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return porProfissional.values().stream()
                .map(this::toRelatorioPorProfissional)
                .toList();
    }

    private List<RelatorioViagemItem> buildRelatorioViagemItems(
            UUID usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        validatePeriodo(dataInicio, dataFim);

        List<Viagem> viagens = viagemRepository.findByPeriodoSobreposto(usuarioId, dataInicio, dataFim);
        if (viagens.isEmpty()) {
            return List.of();
        }

        List<UUID> viagemIds = viagens.stream()
                .map(Viagem::getId)
                .toList();

        Map<UUID, List<DespesaDTO>> despesasPorViagem = findDespesasPorViagem(viagemIds);
        Map<UUID, BigDecimal> estadiasPorViagem = findTotalEstadiasPorViagem(viagemIds);

        return viagens.stream()
                .map(viagem -> toRelatorioViagemItem(viagem, despesasPorViagem, estadiasPorViagem))
                .toList();
    }

    private Map<UUID, List<DespesaDTO>> findDespesasPorViagem(List<UUID> viagemIds) {
        return despesaRepository.findByViagemIdsWithViagem(viagemIds).stream()
                .collect(Collectors.groupingBy(
                        despesa -> despesa.getViagem().getId(),
                        Collectors.mapping(this::toDespesaDTO, Collectors.toList())
                ));
    }

    private Map<UUID, BigDecimal> findTotalEstadiasPorViagem(List<UUID> viagemIds) {
        return estadiaRepository.findByViagemIdsWithViagem(viagemIds).stream()
                .collect(Collectors.groupingBy(
                        estadia -> estadia.getViagem().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                estadia -> integerToBigDecimal(estadia.getValor()),
                                BigDecimal::add
                        )
                ));
    }

    private RelatorioViagemItem toRelatorioViagemItem(
            Viagem viagem,
            Map<UUID, List<DespesaDTO>> despesasPorViagem,
            Map<UUID, BigDecimal> estadiasPorViagem
    ) {
        BigDecimal valorFrete = defaultZero(viagem.getValorFrete());
        BigDecimal totalEstadias = estadiasPorViagem.getOrDefault(viagem.getId(), BigDecimal.ZERO);
        BigDecimal receitaTotal = valorFrete.add(totalEstadias);
        BigDecimal comissaoCalculada = valorFrete.multiply(defaultZero(viagem.getComissao())).divide(CEM);
        List<DespesaDTO> despesas = despesasPorViagem.getOrDefault(viagem.getId(), List.of());

        ViagemRelatorioDTO dto = new ViagemRelatorioDTO(
                viagem.getDataInicio(),
                viagem.getDataFim(),
                viagem.getStatus(),
                viagem.getInicioFrete(),
                viagem.getFimFrete(),
                valorFrete,
                totalEstadias,
                receitaTotal,
                comissaoCalculada,
                despesas,
                viagem.getProfissional() != null ? viagem.getProfissional().getNome() : null,
                viagem.getEmpresa() != null ? viagem.getEmpresa().getNome() : null,
                viagem.getVeiculo() != null ? viagem.getVeiculo().getMarca() : null,
                viagem.getVeiculo() != null ? viagem.getVeiculo().getPlaca() : null
        );

        dto.setLucroLiquido(receitaTotal.subtract(comissaoCalculada.add(defaultZero(dto.getTotalDespesas()))));
        return new RelatorioViagemItem(viagem, dto);
    }

    private RelatorioAgrupadoDTO toRelatorioPorVeiculo(List<RelatorioViagemItem> items) {
        Veiculo veiculo = items.getFirst().viagem().getVeiculo();
        RelatorioTotals totals = sumTotals(items);

        return new RelatorioAgrupadoDTO(
                veiculo.getId(),
                textOrDefault(veiculo.getPlaca(), "Sem placa"),
                joinNonBlank(veiculo.getMarca(), veiculo.getNome()),
                items.size(),
                totals.valorFrete(),
                totals.totalEstadias(),
                totals.receitaTotal(),
                totals.comissao(),
                totals.totalDespesas(),
                totals.lucroLiquido()
        );
    }

    private RelatorioAgrupadoDTO toRelatorioPorProfissional(List<RelatorioViagemItem> items) {
        Profissional profissional = items.getFirst().viagem().getProfissional();
        RelatorioTotals totals = sumTotals(items);

        return new RelatorioAgrupadoDTO(
                profissional.getId(),
                profissional.getNome(),
                profissional.getTelefone(),
                items.size(),
                totals.valorFrete(),
                totals.totalEstadias(),
                totals.receitaTotal(),
                totals.comissao(),
                totals.totalDespesas(),
                totals.lucroLiquido()
        );
    }

    private RelatorioTotals sumTotals(List<RelatorioViagemItem> items) {
        return new RelatorioTotals(
                sum(items, dto -> dto.getValorFrete()),
                sum(items, dto -> dto.getTotalEstadias()),
                sum(items, dto -> dto.getReceitaTotal()),
                sum(items, dto -> dto.getComissao()),
                sum(items, dto -> dto.getTotalDespesas()),
                sum(items, dto -> dto.getLucroLiquido())
        );
    }

    private BigDecimal sum(List<RelatorioViagemItem> items, Function<ViagemRelatorioDTO, BigDecimal> mapper) {
        return items.stream()
                .map(RelatorioViagemItem::dto)
                .map(mapper)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DespesaDTO toDespesaDTO(Despesa despesa) {
        DespesaDTO dto = new DespesaDTO();
        dto.setId(despesa.getId());
        dto.setNome(despesa.getNome());
        dto.setDescricao(despesa.getDescricao());
        dto.setValor(despesa.getValor());
        return dto;
    }

    private void validatePeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Informe a data inicial e a data final.");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual a data final.");
        }
    }

    private BigDecimal integerToBigDecimal(Integer value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String joinNonBlank(String first, String second) {
        return Stream.of(first, second)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" - "));
    }

    private record RelatorioViagemItem(Viagem viagem, ViagemRelatorioDTO dto) {
    }

    private record RelatorioTotals(
            BigDecimal valorFrete,
            BigDecimal totalEstadias,
            BigDecimal receitaTotal,
            BigDecimal comissao,
            BigDecimal totalDespesas,
            BigDecimal lucroLiquido
    ) {
    }
}
