package com.djelog.services;

import com.djelog.dtos.EmpresaDTO;
import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.dtos.ViagemCreateRequestDTO;
import com.djelog.entities.Empresa;
import com.djelog.entities.Profissional;
import com.djelog.entities.RecebimentoFrete;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.repositories.RecebimentoFreteRepository;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.PagamentoMotoristaItemRepository;
import com.djelog.exceptions.ConflictException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final VeiculoRepository veiculoRepository;
    private final RecebimentoFreteRepository recebimentoRepository;
    private final DespesaRepository despesaRepository;
    private final PagamentoMotoristaItemRepository pagamentoMotoristaItemRepository;

    @Autowired
    public ViagemService(
            ViagemRepository viagemRepository,
            EmpresaRepository empresaRepository,
            ProfissionalRepository profissionalRepository,
            VeiculoRepository veiculoRepository,
            RecebimentoFreteRepository recebimentoRepository,
            DespesaRepository despesaRepository,
            PagamentoMotoristaItemRepository pagamentoMotoristaItemRepository
    ) {
        this.viagemRepository = viagemRepository;
        this.empresaRepository = empresaRepository;
        this.profissionalRepository = profissionalRepository;
        this.veiculoRepository = veiculoRepository;
        this.recebimentoRepository = recebimentoRepository;
        this.despesaRepository = despesaRepository;
        this.pagamentoMotoristaItemRepository = pagamentoMotoristaItemRepository;
    }

    public ViagemService(ViagemRepository viagemRepository, EmpresaRepository empresaRepository,
            ProfissionalRepository profissionalRepository, VeiculoRepository veiculoRepository,
            RecebimentoFreteRepository recebimentoRepository, DespesaRepository despesaRepository) {
        this(viagemRepository, empresaRepository, profissionalRepository, veiculoRepository,
                recebimentoRepository, despesaRepository, null);
    }

    @Transactional(readOnly = true)
    public List<ViagemDTO> findAll(UUID usuarioId) {
        List<Viagem> viagens = viagemRepository.findByProfissional_Usuario_Id(usuarioId);
        if (viagens.isEmpty()) return List.of();
        Map<UUID, ReceiptTotals> totals = new HashMap<>();
        recebimentoRepository.totalsByViagemIds(viagens.stream().map(Viagem::getId).toList()).forEach(row ->
                totals.put((UUID) row[0], new ReceiptTotals((BigDecimal) row[1], (BigDecimal) row[2])));
        return viagens.stream().map(trip -> toDTO(trip, totals.getOrDefault(trip.getId(), ReceiptTotals.ZERO))).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ViagemDTO> findById(UUID id, UUID usuarioId) {
        return viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(this::toDTO);
    }

    public ViagemDTO create(ViagemCreateRequestDTO request, UUID usuarioId) {
        Viagem viagem = new Viagem();
        viagem.setProfissional(profissionalRepository.findByIdAndUsuario_Id(request.getProfissionalId(), usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Motorista nao encontrado para este usuario.")));
        viagem.setVeiculo(resolveTruck(request.getVeiculoId(), usuarioId));
        if (request.getEmpresaId() != null) {
            viagem.setEmpresa(empresaRepository.findByIdAndUsuario_Id(request.getEmpresaId(), usuarioId)
                    .orElseThrow(() -> new AccessDeniedException("Empresa nao encontrada para este usuario.")));
        }
        viagem.setInicioFrete(request.getInicioFrete().trim());
        viagem.setFimFrete(request.getFimFrete());
        viagem.setParadaIntermediaria(request.getParadaIntermediaria());
        viagem.setInicioMunicipioCodigo(request.getInicioMunicipioCodigo());
        viagem.setInicioUfCodigo(request.getInicioUfCodigo());
        viagem.setParadaMunicipioCodigo(request.getParadaMunicipioCodigo());
        viagem.setParadaUfCodigo(request.getParadaUfCodigo());
        viagem.setFimMunicipioCodigo(request.getFimMunicipioCodigo());
        viagem.setFimUfCodigo(request.getFimUfCodigo());
        viagem.setValorFrete(request.getValorFrete());
        viagem.setComissao(request.getComissao());
        viagem.setValorComissaoManual(request.getValorComissaoManual());
        viagem.setPercentualAdiantamento(new BigDecimal("70.00"));
        viagem.setDataInicio(request.getDataInicio());
        viagem.setDataFim(request.getDataFim());
        viagem.setStatus(request.getStatus());
        applyFinancialDefaults(viagem, true);
        enforceOwnerCommissionExemption(viagem);

        String formaPagamento = request.getFormaPagamentoFrete() == null ? "ADIANTAMENTO"
                : request.getFormaPagamentoFrete().toUpperCase(Locale.ROOT);
        BigDecimal valorInicial;
        if ("A_VISTA".equals(formaPagamento)) {
            valorInicial = viagem.getValorFrete();
        } else {
            valorInicial = request.getValorAdiantamento() == null
                    ? viagem.getValorFrete().multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP)
                    : request.getValorAdiantamento().setScale(2, RoundingMode.UNNECESSARY);
            if (valorInicial.compareTo(BigDecimal.ZERO) <= 0 || valorInicial.compareTo(viagem.getValorFrete()) > 0) {
                throw new IllegalArgumentException("O adiantamento deve ser maior que zero e nao pode ultrapassar o valor do frete.");
            }
        }

        Viagem saved = viagemRepository.save(viagem);
        RecebimentoFrete recebimentoInicial = new RecebimentoFrete();
        recebimentoInicial.setViagem(saved);
        recebimentoInicial.setValor(valorInicial);
        recebimentoInicial.setDataRecebimento(request.getDataRecebimentoInicial() == null
                ? LocalDate.now() : request.getDataRecebimentoInicial());
        recebimentoInicial.setInicial(true);
        recebimentoRepository.save(recebimentoInicial);
        return toDTO(saved);
    }

    public Optional<ViagemDTO> update(UUID id, Viagem viagem, UUID usuarioId) {
        Empresa empresa = resolveEmpresa(viagem, usuarioId);
        Profissional profissional = resolveProfissional(viagem, usuarioId);
        Veiculo veiculo = resolveVeiculo(viagem, usuarioId);
        viagem.setProfissional(profissional);
        viagem.setVeiculo(veiculo);
        enforceOwnerCommissionExemption(viagem);

        return viagemRepository.findByIdAndProfissional_Usuario_IdForUpdate(id, usuarioId)
                .map(existing -> {
                    applyFinancialDefaults(viagem, false);
                    BigDecimal totalRecebido = recebimentoRepository.sumByViagemId(existing.getId());
                    if (viagem.getValorFrete() == null || viagem.getValorFrete().compareTo(totalRecebido) < 0) {
                        throw new IllegalArgumentException("O frete nao pode ficar abaixo do valor ja recebido nesta viagem.");
                    }
                    BigDecimal comissaoPaga = pagoAoMotorista(existing.getId());
                    if (comissaoPaga.signum() > 0) {
                        if (valorComissao(viagem).compareTo(comissaoPaga) < 0)
                            throw new IllegalArgumentException("A comissao nao pode ficar abaixo do valor ja pago ao motorista.");
                        if (!existing.getProfissional().getId().equals(profissional.getId()))
                            throw new IllegalArgumentException("Nao e possivel trocar o motorista depois de registrar pagamento de comissao.");
                        if (!existing.getDataInicio().toLocalDate().equals(viagem.getDataInicio().toLocalDate()))
                            throw new IllegalArgumentException("Nao e possivel alterar a data da viagem depois de registrar pagamento de comissao.");
                        if ("CANCELADA".equalsIgnoreCase(viagem.getStatus()))
                            throw new IllegalArgumentException("Nao e possivel cancelar uma viagem com comissao paga sem reconciliar o pagamento.");
                    }
                    existing.setProfissional(profissional);
                    existing.setEmpresa(empresa);
                    existing.setVeiculo(veiculo);
                    existing.setInicioFrete(viagem.getInicioFrete());
                    existing.setFimFrete(viagem.getFimFrete());
                    existing.setParadaIntermediaria(viagem.getParadaIntermediaria());
                    existing.setInicioMunicipioCodigo(viagem.getInicioMunicipioCodigo());
                    existing.setInicioUfCodigo(viagem.getInicioUfCodigo());
                    existing.setParadaMunicipioCodigo(viagem.getParadaMunicipioCodigo());
                    existing.setParadaUfCodigo(viagem.getParadaUfCodigo());
                    existing.setFimMunicipioCodigo(viagem.getFimMunicipioCodigo());
                    existing.setFimUfCodigo(viagem.getFimUfCodigo());
                    existing.setValorFrete(viagem.getValorFrete());
                    existing.setComissao(viagem.getComissao());
                    existing.setValorComissaoManual(viagem.getValorComissaoManual());
                    existing.setPercentualAdiantamento(viagem.getPercentualAdiantamento());
                    existing.setDataInicio(viagem.getDataInicio());
                    existing.setDataFim(viagem.getDataFim());
                    existing.setStatus(viagem.getStatus());
                    Viagem saved = viagemRepository.save(existing);
                    return toDTO(saved);
                });
    }

    public void delete(UUID id, UUID usuarioId) {
        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_IdForUpdate(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        if (recebimentoRepository.existsByViagemId(id) || despesaRepository.existsByViagemIdAndUsuarioId(id, usuarioId)) {
            throw new ConflictException("Esta viagem possui recebimentos ou despesas vinculados. Remova os vínculos antes de excluir ou mantenha a viagem como cancelada.");
        }
        if (pagoAoMotorista(viagem.getId()).signum() > 0)
            throw new ConflictException("Nao e possivel excluir uma viagem com pagamento de comissao vinculado.");
        viagemRepository.delete(viagem);
    }

    private BigDecimal pagoAoMotorista(UUID viagemId) {
        return pagamentoMotoristaItemRepository == null ? BigDecimal.ZERO
                : Optional.ofNullable(pagamentoMotoristaItemRepository.sumByViagemId(viagemId)).orElse(BigDecimal.ZERO);
    }

    private BigDecimal valorComissao(Viagem viagem) {
        if (viagem.getValorComissaoManual() != null) return viagem.getValorComissaoManual();
        BigDecimal percentual = viagem.getComissao() == null ? new BigDecimal("15.00") : viagem.getComissao();
        BigDecimal frete = viagem.getValorFrete() == null ? BigDecimal.ZERO : viagem.getValorFrete();
        return frete.multiply(percentual).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private void setManagedRelationships(Viagem viagem, UUID usuarioId) {
        viagem.setEmpresa(resolveEmpresa(viagem, usuarioId));
        viagem.setProfissional(resolveProfissional(viagem, usuarioId));
        viagem.setVeiculo(resolveVeiculo(viagem, usuarioId));
    }

    private void applyFinancialDefaults(Viagem viagem, boolean creating) {
        if (viagem.getComissao() == null) viagem.setComissao(new BigDecimal("15.00"));
        if (viagem.getPercentualAdiantamento() == null) viagem.setPercentualAdiantamento(new BigDecimal("70.00"));
        BigDecimal advance = viagem.getPercentualAdiantamento();
        if (advance.compareTo(BigDecimal.ZERO) < 0 || advance.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("O percentual de adiantamento deve ficar entre 0 e 100.");
        }
        if (creating && viagem.getValorComissaoManual() == null && viagem.getValorFrete() != null) {
            viagem.setValorComissaoManual(null);
        }
    }

    private Empresa resolveEmpresa(Viagem viagem, UUID usuarioId) {
        if (viagem.getEmpresa() == null || viagem.getEmpresa().getId() == null) {
            return null;
        }
        return empresaRepository.findByIdAndUsuario_Id(viagem.getEmpresa().getId(), usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Empresa nao encontrada para este usuario."));
    }

    private Profissional resolveProfissional(Viagem viagem, UUID usuarioId) {
        if (viagem.getProfissional() == null || viagem.getProfissional().getId() == null) {
            throw new IllegalArgumentException("Selecione um motorista para a viagem.");
        }
        return profissionalRepository.findByIdAndUsuario_Id(viagem.getProfissional().getId(), usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Motorista nao encontrado para este usuario."));
    }

    private Veiculo resolveVeiculo(Viagem viagem, UUID usuarioId) {
        if (viagem.getVeiculo() == null || viagem.getVeiculo().getId() == null) {
            throw new IllegalArgumentException("Selecione um veiculo para a viagem.");
        }
        return resolveTruck(viagem.getVeiculo().getId(), usuarioId);
    }

    private Veiculo resolveTruck(UUID veiculoId, UUID usuarioId) {
        Veiculo veiculo = veiculoRepository.findByIdAndUsuarioId(veiculoId, usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Veiculo nao encontrado para este usuario."));
        if (!"CAMINHAO".equalsIgnoreCase(veiculo.getTipo() == null ? "CAMINHAO" : veiculo.getTipo())) {
            throw new IllegalArgumentException("Viagens devem ser vinculadas a um caminhao, nao a uma carreta.");
        }
        return veiculo;
    }

    private void enforceOwnerCommissionExemption(Viagem viagem) {
        if (viagem.getVeiculo() != null && viagem.getVeiculo().getProprietario() != null
                && viagem.getProfissional() != null
                && viagem.getProfissional().getId().equals(viagem.getVeiculo().getProprietario().getId())) {
            viagem.setValorComissaoManual(BigDecimal.ZERO.setScale(2));
        }
    }

    private ViagemDTO toDTO(Viagem viagem) {
        BigDecimal initial = recebimentoRepository.sumInicialByViagemId(viagem.getId());
        BigDecimal received = recebimentoRepository.sumByViagemId(viagem.getId());
        return toDTO(viagem, new ReceiptTotals(received, initial));
    }

    private ViagemDTO toDTO(Viagem viagem, ReceiptTotals totals) {
        ViagemDTO dto = new ViagemDTO();
        dto.setId(viagem.getId());
        dto.setInicioFrete(viagem.getInicioFrete());
        dto.setFimFrete(viagem.getFimFrete());
        dto.setParadaIntermediaria(viagem.getParadaIntermediaria());
        dto.setInicioMunicipioCodigo(viagem.getInicioMunicipioCodigo());
        dto.setInicioUfCodigo(viagem.getInicioUfCodigo());
        dto.setParadaMunicipioCodigo(viagem.getParadaMunicipioCodigo());
        dto.setParadaUfCodigo(viagem.getParadaUfCodigo());
        dto.setFimMunicipioCodigo(viagem.getFimMunicipioCodigo());
        dto.setFimUfCodigo(viagem.getFimUfCodigo());
        dto.setValorFrete(viagem.getValorFrete());
        dto.setComissao(viagem.getComissao());
        BigDecimal valorComissao = viagem.getValorComissaoManual() != null
                ? viagem.getValorComissaoManual()
                : viagem.getValorFrete().multiply(viagem.getComissao() == null ? new BigDecimal("15") : viagem.getComissao())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        dto.setValorComissao(valorComissao);
        dto.setValorComissaoManual(viagem.getValorComissaoManual());
        BigDecimal percentualAdiantamento = viagem.getPercentualAdiantamento() == null ? new BigDecimal("70") : viagem.getPercentualAdiantamento();
        BigDecimal valorAdiantamento = totals.initial();
        dto.setPercentualAdiantamento(percentualAdiantamento);
        dto.setValorAdiantamento(valorAdiantamento);
        dto.setValorSaldo(viagem.getValorFrete().subtract(valorAdiantamento).max(BigDecimal.ZERO));
        BigDecimal valorRecebido = totals.received();
        dto.setValorRecebido(valorRecebido);
        dto.setSaldoPendente(viagem.getValorFrete().subtract(valorRecebido).max(BigDecimal.ZERO));
        dto.setDataInicio(viagem.getDataInicio());
        dto.setDataFim(viagem.getDataFim());
        dto.setStatus(viagem.getStatus());

        if (viagem.getProfissional() != null) {
            ProfissionalDTO profissionalDTO = new ProfissionalDTO();
            profissionalDTO.setId(viagem.getProfissional().getId());
            profissionalDTO.setNome(viagem.getProfissional().getNome());
            profissionalDTO.setTelefone(viagem.getProfissional().getTelefone());
            profissionalDTO.setStatus(viagem.getProfissional().getStatus());
            dto.setProfissional(profissionalDTO);
        }

        if (viagem.getEmpresa() != null) {
            EmpresaDTO empresaDTO = new EmpresaDTO();
            empresaDTO.setId(viagem.getEmpresa().getId());
            empresaDTO.setNome(viagem.getEmpresa().getNome());
            empresaDTO.setDescricao(viagem.getEmpresa().getDescricao());
            empresaDTO.setNomeContato(viagem.getEmpresa().getNomeContato());
            empresaDTO.setTelefoneContato(viagem.getEmpresa().getTelefoneContato());
            empresaDTO.setEmailContato(viagem.getEmpresa().getEmailContato());
            dto.setEmpresa(empresaDTO);
        }

        if (viagem.getVeiculo() != null) {
            VeiculoDTO veiculoDTO = new VeiculoDTO();
            veiculoDTO.setId(viagem.getVeiculo().getId());
            veiculoDTO.setMarca(viagem.getVeiculo().getMarca());
            veiculoDTO.setTipo(viagem.getVeiculo().getTipo());
            veiculoDTO.setModelo(viagem.getVeiculo().getModelo());
            veiculoDTO.setAno(viagem.getVeiculo().getAno());
            veiculoDTO.setPlaca(viagem.getVeiculo().getPlaca());
            veiculoDTO.setNome(viagem.getVeiculo().getNome());
            veiculoDTO.setQtdPeso(viagem.getVeiculo().getQtdPeso());
            veiculoDTO.setStatus(viagem.getVeiculo().getStatus());
            veiculoDTO.setCaminhaoId(viagem.getVeiculo().getCaminhao() == null ? null : viagem.getVeiculo().getCaminhao().getId());
            if (viagem.getVeiculo().getProprietario() != null) {
                ProfissionalDTO ownerDTO = new ProfissionalDTO();
                ownerDTO.setId(viagem.getVeiculo().getProprietario().getId());
                ownerDTO.setNome(viagem.getVeiculo().getProprietario().getNome());
                ownerDTO.setStatus(viagem.getVeiculo().getProprietario().getStatus());
                veiculoDTO.setProprietario(ownerDTO);
            }
            dto.setVeiculo(veiculoDTO);
        }

        return dto;
    }

    private record ReceiptTotals(BigDecimal received, BigDecimal initial) {
        private static final ReceiptTotals ZERO = new ReceiptTotals(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
