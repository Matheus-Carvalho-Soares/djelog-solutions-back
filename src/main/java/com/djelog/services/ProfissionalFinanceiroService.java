package com.djelog.services;

import com.djelog.dtos.MotoristaFinanceiroDTO;
import com.djelog.dtos.RegistrarPagamentoMotoristaRequest;
import com.djelog.entities.PagamentoMotorista;
import com.djelog.entities.PagamentoMotoristaItem;
import com.djelog.entities.Profissional;
import com.djelog.entities.Viagem;
import com.djelog.repositories.PagamentoMotoristaItemRepository;
import com.djelog.repositories.PagamentoMotoristaRepository;
import com.djelog.repositories.ControleFinanceiroMotoristaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProfissionalFinanceiroService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final Set<String> PAYMENT_METHODS = Set.of("PIX", "TRANSFERENCIA", "DINHEIRO", "OUTRO");

    private final ProfissionalRepository profissionalRepository;
    private final ControleFinanceiroMotoristaRepository controleRepository;
    private final ViagemRepository viagemRepository;
    private final PagamentoMotoristaRepository pagamentoRepository;
    private final PagamentoMotoristaItemRepository itemRepository;

    public ProfissionalFinanceiroService(ProfissionalRepository profissionalRepository,
            ControleFinanceiroMotoristaRepository controleRepository, ViagemRepository viagemRepository,
            PagamentoMotoristaRepository pagamentoRepository,
            PagamentoMotoristaItemRepository itemRepository) {
        this.profissionalRepository = profissionalRepository;
        this.controleRepository = controleRepository;
        this.viagemRepository = viagemRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public MotoristaFinanceiroDTO consultar(UUID profissionalId, UUID usuarioId,
            LocalDate inicio, LocalDate fim, UUID veiculoId) {
        LocalDate hoje = LocalDate.now();
        LocalDate fimPeriodo = fim;
        LocalDate inicioPeriodo = inicio;
        if (inicioPeriodo != null && fimPeriodo != null && inicioPeriodo.isAfter(fimPeriodo))
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final.");
        Profissional profissional = profissionalRepository.findByIdAndUsuario_Id(profissionalId, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        // O filtro de período limita a lista da tela; o saldo disponível para
        // pagamento e os indicadores históricos continuam considerando todas as viagens.
        List<Viagem> viagens = viagemRepository.findFinanceiroTrips(profissionalId, usuarioId);
        Map<UUID, BigDecimal> pagoPorViagem = new HashMap<>();
        if (!viagens.isEmpty()) {
            itemRepository.sumByViagemIds(viagens.stream().map(Viagem::getId).toList())
                    .forEach(row -> pagoPorViagem.put((UUID) row[0], money((BigDecimal) row[1])));
        }
        List<Viagem> filtered = viagens.stream()
                .filter(v -> inicioPeriodo == null || !v.getDataInicio().toLocalDate().isBefore(inicioPeriodo))
                .filter(v -> fimPeriodo == null || !v.getDataInicio().toLocalDate().isAfter(fimPeriodo))
                .filter(v -> veiculoId == null || (v.getVeiculo() != null && veiculoId.equals(v.getVeiculo().getId())))
                .sorted(Comparator.comparing(Viagem::getDataInicio).reversed()).toList();

        BigDecimal released = sumCommission(filtered, this::isCompleted);
        BigDecimal projected = sumCommission(filtered, this::isActive);
        int completedCount = (int) filtered.stream().filter(this::isCompleted).count();
        BigDecimal currentWeek = viagens.stream().filter(this::isCompleted)
                .filter(v -> !v.getDataInicio().toLocalDate().isBefore(weekStart(hoje)))
                .filter(v -> !v.getDataInicio().toLocalDate().isAfter(hoje))
                .filter(v -> veiculoId == null || (v.getVeiculo() != null && veiculoId.equals(v.getVeiculo().getId())))
                .map(this::commission).reduce(ZERO, BigDecimal::add);
        BigDecimal weeklyAverage = weeklyAverage(viagens, weekStart(hoje), veiculoId);
        BigDecimal openBalance = viagens.stream().filter(this::isCompleted).filter(v -> !isCancelled(v))
                .filter(v -> veiculoId == null || (v.getVeiculo() != null && veiculoId.equals(v.getVeiculo().getId())))
                .map(v -> commission(v).subtract(pagoPorViagem.getOrDefault(v.getId(), ZERO)).max(ZERO))
                .reduce(ZERO, BigDecimal::add);

        List<MotoristaFinanceiroDTO.Semana> weeks = buildWeeks(viagens, pagoPorViagem, profissionalId,
                usuarioId, weekStart(hoje).minusWeeks(11), weekStart(hoje), veiculoId);
        List<PagamentoMotorista> paymentsInPeriod = pagamentoRepository.findDetailsByPeriod(
                profissionalId, usuarioId, inicioPeriodo, fimPeriodo);
        List<MotoristaFinanceiroDTO.Pagamento> pagamentos = paymentsInPeriod.stream()
                .map(payment -> toPaymentDto(payment, veiculoId)).filter(Objects::nonNull).toList();
        BigDecimal paidInPeriod = pagamentos.stream().map(MotoristaFinanceiroDTO.Pagamento::valor).reduce(ZERO, BigDecimal::add);
        BigDecimal legacyOpen = controleRepository.findByProfissional_IdAndUsuario_Id(profissionalId, usuarioId)
                .map(control -> money(control.getSaldoInicialPendente())
                        .subtract(money(itemRepository.sumByControleId(control.getId()))).max(ZERO))
                .orElse(ZERO);
        List<MotoristaFinanceiroDTO.Viagem> tripRows = filtered.stream().map(v -> toTripDto(v,
                pagoPorViagem.getOrDefault(v.getId(), ZERO))).toList();
        List<MotoristaFinanceiroDTO.VeiculoFiltro> vehicleOptions = viagens.stream().map(Viagem::getVeiculo)
                .filter(Objects::nonNull).collect(Collectors.toMap(v -> v.getId(), Function.identity(), (a, b) -> a))
                .values().stream().map(v -> new MotoristaFinanceiroDTO.VeiculoFiltro(v.getId(),
                        Optional.ofNullable(v.getPlaca()).orElse("Veículo") + " · " +
                                Optional.ofNullable(v.getModelo()).orElse(Optional.ofNullable(v.getMarca()).orElse(""))))
                .sorted(Comparator.comparing(MotoristaFinanceiroDTO.VeiculoFiltro::nome)).toList();

        return new MotoristaFinanceiroDTO(profissionalId, profissional.getNome(),
                inicioPeriodo, fimPeriodo, vehicleOptions,
                new MotoristaFinanceiroDTO.Resumo(money(currentWeek), weeklyAverage, money(openBalance),
                        money(paidInPeriod), money(released), money(projected), completedCount, money(legacyOpen)),
                weeks, tripRows, pagamentos);
    }

    @Transactional
    public UUID registrarPagamento(UUID profissionalId, UUID usuarioId, RegistrarPagamentoMotoristaRequest request) {
        if (!PAYMENT_METHODS.contains(request.formaPagamento()))
            throw new IllegalArgumentException("Selecione uma forma de pagamento válida.");
        if (request.dataPagamento().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("A data do pagamento não pode estar no futuro.");
        if (request.alocacoes() == null || request.alocacoes().isEmpty())
            throw new IllegalArgumentException("Selecione ao menos uma comissão para pagar.");
        Profissional profissional = profissionalRepository.findByIdAndUsuario_Id(profissionalId, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        Set<UUID> tripIds = new HashSet<>();
        for (var allocation : request.alocacoes()) {
            if (currency(allocation.valor()).signum() <= 0)
                throw new IllegalArgumentException("Cada valor alocado deve ser maior que zero.");
            if (allocation.viagemId() == null)
                throw new IllegalArgumentException("Cada item deve apontar para uma viagem.");
            if (!tripIds.add(allocation.viagemId()))
                throw new IllegalArgumentException("Uma viagem não pode aparecer mais de uma vez no mesmo pagamento.");
        }

        List<Viagem> lockedTrips = tripIds.stream().sorted().map(id -> viagemRepository
                .findByIdAndProfissional_Usuario_IdForUpdate(id, usuarioId).orElseThrow(NoSuchElementException::new)).toList();
        Map<UUID, Viagem> tripById = lockedTrips.stream().collect(Collectors.toMap(Viagem::getId, Function.identity()));
        BigDecimal total = ZERO;
        for (var allocation : request.alocacoes()) {
            BigDecimal amount = currency(allocation.valor());
            total = total.add(amount);
            Viagem viagem = tripById.get(allocation.viagemId());
            if (viagem == null || !profissionalId.equals(viagem.getProfissional().getId()))
                throw new IllegalArgumentException("A viagem informada não pertence a este motorista.");
            if (!isCompleted(viagem) || isCancelled(viagem))
                throw new IllegalArgumentException("Só viagens concluídas e não canceladas liberam comissão para pagamento.");
            BigDecimal alreadyPaid = itemRepository.sumByViagemId(viagem.getId());
            if (alreadyPaid.add(amount).compareTo(commission(viagem)) > 0)
                throw new IllegalArgumentException("O valor informado excede o saldo de comissão desta viagem.");
        }
        PagamentoMotorista pagamento = new PagamentoMotorista();
        pagamento.setProfissional(profissional);
        pagamento.setUsuario(profissional.getUsuario());
        pagamento.setDataPagamento(request.dataPagamento());
        pagamento.setFormaPagamento(request.formaPagamento());
        pagamento.setReferencia(clean(request.referencia(), 120));
        pagamento.setObservacao(clean(request.observacao(), 500));
        pagamento.setValor(money(total));
        for (var allocation : request.alocacoes()) {
            PagamentoMotoristaItem item = new PagamentoMotoristaItem();
            item.setValor(currency(allocation.valor()));
            item.setViagem(tripById.get(allocation.viagemId()));
            pagamento.addItem(item);
        }
        return pagamentoRepository.save(pagamento).getId();
    }

    private List<MotoristaFinanceiroDTO.Semana> buildWeeks(List<Viagem> viagens, Map<UUID, BigDecimal> paid,
            UUID profissionalId, UUID usuarioId, LocalDate firstWeek, LocalDate currentWeek, UUID vehicleId) {
        LocalDate today = LocalDate.now();
        List<MotoristaFinanceiroDTO.Semana> result = new ArrayList<>();
        List<PagamentoMotorista> payments = pagamentoRepository.findDetailsByPeriod(profissionalId, usuarioId,
                firstWeek, today);
        Map<LocalDate, BigDecimal> paidByDate = new HashMap<>();
        payments.forEach(payment -> payment.getItens().stream().filter(item -> vehicleId == null
                        ? item.getViagem() == null || item.getViagem().getVeiculo() != null
                        : item.getViagem() != null && item.getViagem().getVeiculo() != null
                                && vehicleId.equals(item.getViagem().getVeiculo().getId()))
                .forEach(item -> paidByDate.merge(weekStart(payment.getDataPagamento()), item.getValor(), BigDecimal::add)));
        for (int i = 0; i <= 11; i++) {
            LocalDate week = firstWeek.plusWeeks(i);
            BigDecimal generated = viagens.stream().filter(this::isCompleted).filter(v -> !isCancelled(v))
                    .filter(v -> weekStart(v.getDataInicio().toLocalDate()).equals(week))
                    .filter(v -> vehicleId == null || (v.getVeiculo() != null && vehicleId.equals(v.getVeiculo().getId())))
                    .map(this::commission).reduce(ZERO, BigDecimal::add);
            BigDecimal openToday = viagens.stream().filter(this::isCompleted).filter(v -> !isCancelled(v))
                    .filter(v -> weekStart(v.getDataInicio().toLocalDate()).equals(week))
                    .filter(v -> vehicleId == null || (v.getVeiculo() != null && vehicleId.equals(v.getVeiculo().getId())))
                    .map(v -> commission(v).subtract(paid.getOrDefault(v.getId(), ZERO)).max(ZERO))
                    .reduce(ZERO, BigDecimal::add);
            result.add(new MotoristaFinanceiroDTO.Semana(week, money(generated), money(paidByDate.getOrDefault(week, ZERO)), money(openToday)));
        }
        return result;
    }

    private BigDecimal weeklyAverage(List<Viagem> trips, LocalDate currentWeek, UUID vehicleId) {
        BigDecimal total = trips.stream().filter(this::isCompleted).filter(v -> !isCancelled(v))
                .filter(v -> weekStart(v.getDataInicio().toLocalDate()).isBefore(currentWeek)
                        && !weekStart(v.getDataInicio().toLocalDate()).isBefore(currentWeek.minusWeeks(12)))
                .filter(v -> vehicleId == null || (v.getVeiculo() != null && vehicleId.equals(v.getVeiculo().getId())))
                .map(this::commission).reduce(ZERO, BigDecimal::add);
        return total.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
    }

    private MotoristaFinanceiroDTO.Viagem toTripDto(Viagem viagem, BigDecimal paid) {
        BigDecimal commission = isCancelled(viagem) ? ZERO : commission(viagem);
        boolean completed = isCompleted(viagem);
        String vehicle = viagem.getVeiculo() == null ? "—" :
                (viagem.getVeiculo().getPlaca() == null ? "" : viagem.getVeiculo().getPlaca() + " · ") +
                        Optional.ofNullable(viagem.getVeiculo().getModelo()).orElse(viagem.getVeiculo().getMarca());
        return new MotoristaFinanceiroDTO.Viagem(viagem.getId(), viagem.getVeiculo() == null ? null : viagem.getVeiculo().getId(), viagem.getDataInicio().toLocalDate(),
                String.join(" → ", Optional.ofNullable(viagem.getInicioFrete()).orElse("Origem não informada"),
                        Optional.ofNullable(viagem.getFimFrete()).orElse("Destino não informado")),
                vehicle, viagem.getStatus(), money(viagem.getValorFrete()), commission,
                money(paid), completed ? money(commission.subtract(paid).max(ZERO)) : ZERO, !completed && isActive(viagem));
    }

    private MotoristaFinanceiroDTO.Pagamento toPaymentDto(PagamentoMotorista payment, UUID vehicleId) {
        List<PagamentoMotoristaItem> items = payment.getItens().stream().filter(i -> vehicleId == null
                || (i.getViagem() != null && i.getViagem().getVeiculo() != null
                        && vehicleId.equals(i.getViagem().getVeiculo().getId()))).toList();
        if (items.isEmpty()) return null;
        List<String> labels = items.stream().map(i -> i.getViagem() == null ? "Saldo anterior" :
                Optional.ofNullable(i.getViagem().getInicioFrete()).orElse("Origem") + " → " +
                        Optional.ofNullable(i.getViagem().getFimFrete()).orElse("Destino"))
                .distinct().toList();
        BigDecimal allocatedValue = items.stream().map(PagamentoMotoristaItem::getValor).reduce(ZERO, BigDecimal::add);
        return new MotoristaFinanceiroDTO.Pagamento(payment.getId(), payment.getDataPagamento(),
                money(allocatedValue), payment.getFormaPagamento(), payment.getReferencia(), payment.getObservacao(), labels);
    }

    private BigDecimal sumCommission(List<Viagem> trips, Function<Viagem, Boolean> predicate) {
        return money(trips.stream().filter(predicate::apply).map(this::commission).reduce(ZERO, BigDecimal::add));
    }
    private BigDecimal commission(Viagem viagem) {
        if (viagem.getValorComissaoManual() != null) return money(viagem.getValorComissaoManual());
        BigDecimal percentage = viagem.getComissao() == null ? new BigDecimal("15.00") : viagem.getComissao();
        BigDecimal freight = viagem.getValorFrete() == null ? ZERO : viagem.getValorFrete();
        return money(freight.multiply(percentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
    }
    private boolean isCompleted(Viagem viagem) { return "CONCLUIDA".equalsIgnoreCase(viagem.getStatus()); }
    private boolean isActive(Viagem viagem) { return "EM_ANDAMENTO".equalsIgnoreCase(viagem.getStatus()); }
    private boolean isCancelled(Viagem viagem) { return "CANCELADA".equalsIgnoreCase(viagem.getStatus()); }
    private LocalDate weekStart(LocalDate date) { return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); }
    private BigDecimal money(BigDecimal value) { return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal currency(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("Informe um valor válido.");
        try { return value.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException ex) { throw new IllegalArgumentException("Valores monetários devem ter no máximo duas casas decimais."); }
    }
    private String clean(String value, int max) { if (value == null || value.isBlank()) return null; return value.trim().substring(0, Math.min(value.trim().length(), max)); }
}
