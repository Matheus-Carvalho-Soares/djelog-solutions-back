package com.djelog.services;

import com.djelog.dtos.DespesaDTO;
import com.djelog.dtos.RelatorioDetalhePageDTO;
import com.djelog.dtos.RelatorioAgrupadoDTO;
import com.djelog.dtos.RelatorioFiltro;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Set<String> VALID_STATUS = Set.of("EM_ANDAMENTO", "CONCLUIDA", "CANCELADA");
    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "grupoNome",
            "quantidadeViagens",
            "receitaTotal",
            "totalDespesas",
            "lucroLiquido",
            "margemLiquidaPercentual"
    );

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
        return buildRelatorioViagemItems(usuarioId, new RelatorioFiltro(
                dataInicio, dataFim, List.of(), List.of(), List.of(), List.of(), "grupoNome", "asc"
        )).stream()
                .map(RelatorioViagemItem::dto)
                .toList();
    }

    public List<RelatorioAgrupadoDTO> findFaturamentoPorVeiculo(
            UUID usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return findFaturamentoPorVeiculo(usuarioId, new RelatorioFiltro(
                dataInicio, dataFim, List.of(), List.of(), List.of(), List.of(), "receitaTotal", "desc"
        ));
    }

    public List<RelatorioAgrupadoDTO> findFaturamentoPorVeiculo(UUID usuarioId, RelatorioFiltro filtro) {
        RelatorioFiltro filtroNormalizado = normalizeFiltro(filtro);
        List<RelatorioViagemItem> items = buildRelatorioViagemItems(usuarioId, filtroNormalizado);
        Map<UUID, List<RelatorioViagemItem>> porVeiculo = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.viagem().getVeiculo().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return porVeiculo.values().stream()
                .map(this::toRelatorioPorVeiculo)
                .filter(item -> matchesBusca(item, filtroNormalizado))
                .sorted(comparatorFor(filtroNormalizado))
                .toList();
    }

    public List<RelatorioAgrupadoDTO> findFaturamentoPorProfissional(
            UUID usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return findFaturamentoPorProfissional(usuarioId, new RelatorioFiltro(
                dataInicio, dataFim, List.of(), List.of(), List.of(), List.of(), "lucroLiquido", "desc"
        ));
    }

    public List<RelatorioAgrupadoDTO> findFaturamentoPorProfissional(UUID usuarioId, RelatorioFiltro filtro) {
        RelatorioFiltro filtroNormalizado = normalizeFiltro(filtro);
        List<RelatorioViagemItem> items = buildRelatorioViagemItems(usuarioId, filtroNormalizado);
        Map<UUID, List<RelatorioViagemItem>> porProfissional = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.viagem().getProfissional().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return porProfissional.values().stream()
                .map(this::toRelatorioPorProfissional)
                .filter(item -> matchesBusca(item, filtroNormalizado))
                .sorted(comparatorFor(filtroNormalizado))
                .toList();
    }

    public RelatorioDetalhePageDTO findDetalhesPorVeiculo(
            UUID usuarioId,
            UUID veiculoId,
            RelatorioFiltro filtro,
            int page,
            int size
    ) {
        return findDetalhes(usuarioId, normalizeFiltro(filtro).comVeiculo(veiculoId), page, size);
    }

    public RelatorioDetalhePageDTO findDetalhesPorProfissional(
            UUID usuarioId,
            UUID profissionalId,
            RelatorioFiltro filtro,
            int page,
            int size
    ) {
        return findDetalhes(usuarioId, normalizeFiltro(filtro).comProfissional(profissionalId), page, size);
    }

    private RelatorioDetalhePageDTO findDetalhes(UUID usuarioId, RelatorioFiltro filtro, int page, int size) {
        validateFiltro(filtro);
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("A pagina deve ser maior ou igual a zero e o tamanho deve estar entre 1 e 100.");
        }

        List<ViagemRelatorioDTO> items = buildRelatorioViagemItems(usuarioId, filtro).stream()
                .map(RelatorioViagemItem::dto)
                .toList();
        long totalItems = items.size();
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / size);
        int start = Math.min(page * size, items.size());
        int end = Math.min(start + size, items.size());

        return new RelatorioDetalhePageDTO(items.subList(start, end), page, size, totalItems, totalPages);
    }

    private List<RelatorioViagemItem> buildRelatorioViagemItems(UUID usuarioId, RelatorioFiltro filtro) {
        validateFiltro(filtro);

        List<Viagem> viagens = viagemRepository.findByPeriodoSobrepostoAndFiltros(
                usuarioId,
                filtro.dataInicio(),
                filtro.dataFim(),
                safeIds(filtro.veiculoIds()),
                filtro.filtraVeiculos(),
                safeIds(filtro.profissionalIds()),
                filtro.filtraProfissionais(),
                safeIds(filtro.empresaIds()),
                filtro.filtraEmpresas(),
                safeStatus(filtro.status()),
                filtro.filtraStatus()
        );
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

        dto.setViagemId(viagem.getId());
        dto.setParadaIntermediaria(viagem.getParadaIntermediaria());
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

    private RelatorioFiltro normalizeFiltro(RelatorioFiltro filtro) {
        if (filtro != null) {
            return filtro;
        }
        throw new IllegalArgumentException("Informe os filtros do relatorio.");
    }

    private void validateFiltro(RelatorioFiltro filtro) {
        validatePeriodo(filtro.dataInicio(), filtro.dataFim());

        if (filtro.status().stream().anyMatch(status -> !VALID_STATUS.contains(status))) {
            throw new IllegalArgumentException("Situacao de viagem invalida.");
        }
        if (!VALID_SORT_FIELDS.contains(filtro.sortBy())) {
            throw new IllegalArgumentException("Campo de ordenacao invalido.");
        }
        if (!filtro.sortDirection().equals("asc") && !filtro.sortDirection().equals("desc")) {
            throw new IllegalArgumentException("Direcao de ordenacao invalida.");
        }
    }

    private Comparator<RelatorioAgrupadoDTO> comparatorFor(RelatorioFiltro filtro) {
        Comparator<RelatorioAgrupadoDTO> comparator = switch (filtro.sortBy()) {
            case "grupoNome" -> Comparator.comparing(
                    item -> defaultText(item.getGrupoNome()), String.CASE_INSENSITIVE_ORDER
            );
            case "quantidadeViagens" -> Comparator.comparing(
                    item -> defaultInteger(item.getQuantidadeViagens())
            );
            case "receitaTotal" -> Comparator.comparing(
                    item -> defaultDecimal(item.getReceitaTotal())
            );
            case "totalDespesas" -> Comparator.comparing(
                    item -> defaultDecimal(item.getTotalDespesas())
            );
            case "lucroLiquido" -> Comparator.comparing(
                    item -> defaultDecimal(item.getLucroLiquido())
            );
            case "margemLiquidaPercentual" -> Comparator.comparing(
                    item -> defaultDecimal(item.getMargemLiquidaPercentual())
            );
            default -> throw new IllegalArgumentException("Campo de ordenacao invalido.");
        };

        if ("desc".equals(filtro.sortDirection())) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(item -> defaultText(item.getGrupoNome()), String.CASE_INSENSITIVE_ORDER);
    }

    private boolean matchesBusca(RelatorioAgrupadoDTO item, RelatorioFiltro filtro) {
        if (!filtro.temBusca()) {
            return true;
        }
        String termo = filtro.busca().toLowerCase(java.util.Locale.ROOT);
        return defaultText(item.getGrupoNome()).toLowerCase(java.util.Locale.ROOT).contains(termo)
                || defaultText(item.getGrupoDetalhe()).toLowerCase(java.util.Locale.ROOT).contains(termo);
    }

    private List<UUID> safeIds(List<UUID> ids) {
        return ids.isEmpty() ? List.of(EMPTY_UUID) : ids;
    }

    private List<String> safeStatus(List<String> status) {
        return status.isEmpty() ? List.of("__NO_STATUS__") : status;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
