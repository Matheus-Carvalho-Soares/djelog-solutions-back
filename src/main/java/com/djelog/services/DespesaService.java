package com.djelog.services;

import com.djelog.dtos.DespesaDTO;
import com.djelog.dtos.ConfirmacaoPagamentoLoteDTO;
import com.djelog.dtos.ItemPagamentoLoteDTO;
import com.djelog.dtos.ParcelaDespesaDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.entities.Despesa;
import com.djelog.entities.ParcelaDespesa;
import com.djelog.entities.Usuario;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.UsuarioRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

@Service
@Transactional
public class DespesaService {
    private final DespesaRepository despesaRepository;
    private final ViagemRepository viagemRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public DespesaService(DespesaRepository despesaRepository, ViagemRepository viagemRepository,
                          VeiculoRepository veiculoRepository, UsuarioRepository usuarioRepository) {
        this.despesaRepository = despesaRepository;
        this.viagemRepository = viagemRepository;
        this.veiculoRepository = veiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DespesaDTO> findAll(UUID usuarioId) {
        return despesaRepository.findByUsuarioId(usuarioId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DespesaDTO findById(UUID id, UUID usuarioId) {
        return toDTO(despesaRepository.findByIdAndUsuarioId(id, usuarioId).orElseThrow(NoSuchElementException::new));
    }

    @Transactional(readOnly = true)
    public List<DespesaDTO> findByViagemId(UUID viagemId, UUID usuarioId) {
        if (!viagemRepository.existsByIdAndProfissional_Usuario_Id(viagemId, usuarioId)) {
            throw new NoSuchElementException("Viagem nao encontrada para este usuario.");
        }
        return despesaRepository.findByViagemIdAndUsuarioId(viagemId, usuarioId).stream().map(this::toDTO).toList();
    }

    public DespesaDTO create(DespesaDTO dto, UUID usuarioId) {
        Despesa despesa = new Despesa();
        applyDTO(despesa, dto, usuarioId);
        return toDTO(despesaRepository.save(despesa));
    }

    public DespesaDTO update(UUID id, DespesaDTO dto, UUID usuarioId) {
        Despesa despesa = despesaRepository.findByIdAndUsuarioId(id, usuarioId).orElseThrow(NoSuchElementException::new);
        applyDTO(despesa, dto, usuarioId);
        return toDTO(despesaRepository.save(despesa));
    }

    public List<DespesaDTO> confirmarPagamentosLote(ConfirmacaoPagamentoLoteDTO request, UUID usuarioId) {
        if (request == null || request.parcelas() == null || request.parcelas().isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos uma parcela para confirmar.");
        }
        Set<UUID> installmentIds = new HashSet<>();
        for (ItemPagamentoLoteDTO item : request.parcelas()) {
            if (item == null || item.despesaId() == null || item.parcelaId() == null || item.dataPagamento() == null) {
                throw new IllegalArgumentException("Informe a despesa, a parcela e a data de pagamento de cada item.");
            }
            if (!installmentIds.add(item.parcelaId())) throw new IllegalArgumentException("A mesma parcela foi selecionada mais de uma vez.");
        }

        Map<UUID, Despesa> expenses = new LinkedHashMap<>();
        request.parcelas().stream().map(ItemPagamentoLoteDTO::despesaId).distinct().sorted().forEach(id ->
                expenses.put(id, despesaRepository.findByIdAndUsuarioIdForUpdate(id, usuarioId)
                        .orElseThrow(NoSuchElementException::new)));

        Map<UUID, ParcelaDespesa> installments = new LinkedHashMap<>();
        for (ItemPagamentoLoteDTO item : request.parcelas()) {
            Despesa expense = expenses.get(item.despesaId());
            ParcelaDespesa parcel = expense.getParcelas().stream()
                    .filter(candidate -> item.parcelaId().equals(candidate.getId()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Uma parcela não pertence à despesa informada."));
            if (!"PENDENTE".equalsIgnoreCase(parcel.getStatusPagamento())) {
                throw new IllegalArgumentException("Uma ou mais parcelas já foram atualizadas. Recarregue a revisão e tente novamente.");
            }
            installments.put(item.parcelaId(), parcel);
        }

        request.parcelas().forEach(item -> {
            ParcelaDespesa parcel = installments.get(item.parcelaId());
            parcel.setStatusPagamento("PAGO");
            parcel.setDataPagamento(item.dataPagamento());
        });
        return expenses.values().stream().map(despesaRepository::save).map(this::toDTO).toList();
    }

    public void delete(UUID id, UUID usuarioId) {
        if (!despesaRepository.existsByIdAndUsuarioId(id, usuarioId)) throw new NoSuchElementException();
        despesaRepository.deleteById(id);
    }

    private void applyDTO(Despesa despesa, DespesaDTO dto, UUID usuarioId) {
        if (dto.getNome() == null || dto.getNome().isBlank()) throw new IllegalArgumentException("Informe o nome da despesa.");
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Informe um valor valido para a despesa.");
        Usuario owner = usuarioRepository.findById(usuarioId).orElseThrow(NoSuchElementException::new);
        Viagem viagem = null;
        if (dto.getViagem() != null && dto.getViagem().getId() != null) {
            viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(dto.getViagem().getId(), usuarioId)
                    .orElseThrow(() -> new AccessDeniedException("Viagem nao encontrada para este usuario."));
        }
        Veiculo veiculo = null;
        if (dto.getVeiculo() != null && dto.getVeiculo().getId() != null) {
            veiculo = veiculoRepository.findByIdAndUsuarioId(dto.getVeiculo().getId(), usuarioId)
                    .orElseThrow(() -> new AccessDeniedException("Veiculo nao encontrado para este usuario."));
        }
        if (viagem != null) {
            if (veiculo != null && !veiculo.getId().equals(viagem.getVeiculo().getId())) {
                throw new IllegalArgumentException("O veiculo da despesa deve ser o mesmo vinculado a viagem.");
            }
            veiculo = viagem.getVeiculo();
        }
        despesa.setUsuario(owner);
        despesa.setViagem(viagem);
        despesa.setVeiculo(veiculo);
        despesa.setNome(dto.getNome().trim());
        despesa.setDescricao(dto.getDescricao());
        despesa.setValor(dto.getValor());
        despesa.setCategoria(normalizeCategory(dto.getCategoria(), dto.getNome()));
        despesa.setDataDespesa(dto.getDataDespesa() != null ? dto.getDataDespesa()
                : despesa.getId() == null && viagem != null && viagem.getDataInicio() != null
                ? viagem.getDataInicio().toLocalDate() : null);
        despesa.setRecebedor(dto.getRecebedor() == null || dto.getRecebedor().isBlank()
                ? null : dto.getRecebedor().trim());

        List<ParcelaDespesaDTO> parcelDtos = dto.getParcelas();
        if (parcelDtos == null || parcelDtos.isEmpty()) {
            LocalDate dueDate = dto.getDataVencimento();
            if (dueDate == null && viagem != null && viagem.getDataInicio() != null) dueDate = viagem.getDataInicio().toLocalDate();
            if (dueDate == null) throw new IllegalArgumentException("Informe a data de vencimento da despesa.");
            ParcelaDespesaDTO single = new ParcelaDespesaDTO();
            single.setValor(dto.getValor());
            single.setDataVencimento(dueDate);
            single.setStatusPagamento(dto.getStatusPagamento() == null
                    ? viagem == null ? "PENDENTE" : "PAGO" : dto.getStatusPagamento());
            single.setDataPagamento(dto.getDataPagamento());
            single.setNumeroParcela(1);
            parcelDtos = List.of(single);
        }

        BigDecimal totalParcelas = BigDecimal.ZERO;
        List<ParcelaDespesa> parcelas = new ArrayList<>();
        int index = 1;
        for (ParcelaDespesaDTO parcelDto : parcelDtos) {
            if (parcelDto.getValor() == null || parcelDto.getValor().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("O valor de cada parcela deve ser maior ou igual a zero.");
            }
            if (parcelDto.getDataVencimento() == null) throw new IllegalArgumentException("Informe o vencimento de cada parcela.");
            String state = parcelDto.getStatusPagamento() == null
                    ? dto.getStatusPagamento() != null ? dto.getStatusPagamento().toUpperCase()
                    : viagem == null ? "PENDENTE" : "PAGO"
                    : parcelDto.getStatusPagamento().toUpperCase();
            if (!List.of("PENDENTE", "PAGO", "NAO_INFORMADO").contains(state)) throw new IllegalArgumentException("Situacao de pagamento invalida.");
            LocalDate paymentDate = parcelDto.getDataPagamento() != null ? parcelDto.getDataPagamento()
                    : dto.getDataPagamento() != null ? dto.getDataPagamento() : despesa.getDataDespesa();
            if ("PAGO".equals(state) && paymentDate == null) throw new IllegalArgumentException("Informe a data em que a parcela foi paga.");
            ParcelaDespesa parcel = new ParcelaDespesa();
            parcel.setValor(parcelDto.getValor());
            parcel.setDataVencimento(parcelDto.getDataVencimento());
            parcel.setStatusPagamento(state);
            parcel.setDataPagamento("PAGO".equals(state) ? paymentDate : null);
            parcel.setNumeroParcela(parcelDto.getNumeroParcela() == null ? index : parcelDto.getNumeroParcela());
            parcelas.add(parcel);
            totalParcelas = totalParcelas.add(parcel.getValor());
            index++;
        }
        if (totalParcelas.compareTo(dto.getValor()) != 0) throw new IllegalArgumentException("A soma das parcelas deve ser igual ao valor total da despesa.");
        despesa.setParcelas(parcelas);
    }

    private String normalizeCategory(String category, String name) {
        if (category != null && !category.isBlank()) return category.toUpperCase();
        String normalized = name.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("comiss")) return "COMISSAO";
        if (normalized.contains("pedag")) return "PEDAGIO";
        if (normalized.contains("abaste") || normalized.contains("combust")) return "COMBUSTIVEL";
        if (normalized.contains("pneu") || normalized.contains("freio") || normalized.contains("manuten")) return "MANUTENCAO";
        return "OUTROS";
    }

    private DespesaDTO toDTO(Despesa despesa) {
        DespesaDTO dto = new DespesaDTO();
        dto.setId(despesa.getId());
        dto.setNome(despesa.getNome());
        dto.setDescricao(despesa.getDescricao());
        dto.setValor(despesa.getValor());
        dto.setCategoria(despesa.getCategoria());
        dto.setDataDespesa(despesa.getDataDespesa());
        dto.setRecebedor(despesa.getRecebedor());
        if (!despesa.getParcelas().isEmpty()) {
            dto.setParcelas(despesa.getParcelas().stream().map(this::toDTO).toList());
            ParcelaDespesa first = despesa.getParcelas().get(0);
            dto.setDataVencimento(first.getDataVencimento());
            dto.setStatusPagamento(first.getStatusPagamento());
            dto.setDataPagamento(first.getDataPagamento());
        } else {
            LocalDate fallbackDate = despesa.getViagem() != null && despesa.getViagem().getDataInicio() != null
                    ? despesa.getViagem().getDataInicio().toLocalDate() : null;
            ParcelaDespesaDTO legacy = new ParcelaDespesaDTO();
            legacy.setValor(despesa.getValor() == null ? BigDecimal.ZERO : despesa.getValor());
            legacy.setDataVencimento(fallbackDate);
            legacy.setStatusPagamento("NAO_INFORMADO");
            legacy.setNumeroParcela(1);
            dto.setParcelas(List.of(legacy));
            dto.setDataVencimento(fallbackDate);
            dto.setStatusPagamento("NAO_INFORMADO");
        }
        if (despesa.getViagem() != null) {
            ViagemDTO trip = new ViagemDTO();
            trip.setId(despesa.getViagem().getId());
            trip.setInicioFrete(despesa.getViagem().getInicioFrete());
            trip.setFimFrete(despesa.getViagem().getFimFrete());
            dto.setViagem(trip);
        }
        if (despesa.getVeiculo() != null) {
            VeiculoDTO vehicle = new VeiculoDTO();
            vehicle.setId(despesa.getVeiculo().getId());
            vehicle.setMarca(despesa.getVeiculo().getMarca());
            vehicle.setTipo(despesa.getVeiculo().getTipo());
            vehicle.setModelo(despesa.getVeiculo().getModelo());
            vehicle.setAno(despesa.getVeiculo().getAno());
            vehicle.setPlaca(despesa.getVeiculo().getPlaca());
            vehicle.setNome(despesa.getVeiculo().getNome());
            dto.setVeiculo(vehicle);
        }
        return dto;
    }

    private ParcelaDespesaDTO toDTO(ParcelaDespesa parcel) {
        ParcelaDespesaDTO dto = new ParcelaDespesaDTO();
        dto.setId(parcel.getId());
        dto.setValor(parcel.getValor());
        dto.setDataVencimento(parcel.getDataVencimento());
        dto.setStatusPagamento(parcel.getStatusPagamento());
        dto.setDataPagamento(parcel.getDataPagamento());
        dto.setNumeroParcela(parcel.getNumeroParcela());
        return dto;
    }
}
