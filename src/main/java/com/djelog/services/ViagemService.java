package com.djelog.services;

import com.djelog.dtos.DespesaDTO;
import com.djelog.dtos.EmpresaDTO;
import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.entities.Empresa;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final DespesaRepository despesaRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final VeiculoRepository veiculoRepository;

    public ViagemService(
            ViagemRepository viagemRepository,
            DespesaRepository despesaRepository,
            EmpresaRepository empresaRepository,
            ProfissionalRepository profissionalRepository,
            VeiculoRepository veiculoRepository
    ) {
        this.viagemRepository = viagemRepository;
        this.despesaRepository = despesaRepository;
        this.empresaRepository = empresaRepository;
        this.profissionalRepository = profissionalRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<ViagemDTO> findAll(UUID usuarioId) {
        return viagemRepository.findByProfissional_Usuario_Id(usuarioId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ViagemDTO> findById(UUID id, UUID usuarioId) {
        return viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(this::toDTO);
    }

    public ViagemDTO create(Viagem viagem, UUID usuarioId) {
        setManagedRelationships(viagem, usuarioId);
        Viagem saved = viagemRepository.save(viagem);
        return toDTO(saved);
    }

    public Optional<ViagemDTO> update(UUID id, Viagem viagem, UUID usuarioId) {
        Empresa empresa = resolveEmpresa(viagem, usuarioId);
        Profissional profissional = resolveProfissional(viagem, usuarioId);
        Veiculo veiculo = resolveVeiculo(viagem, usuarioId);

        return viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(existing -> {
                    existing.setProfissional(profissional);
                    existing.setEmpresa(empresa);
                    existing.setVeiculo(veiculo);
                    existing.setInicioFrete(viagem.getInicioFrete());
                    existing.setFimFrete(viagem.getFimFrete());
                    existing.setValorFrete(viagem.getValorFrete());
                    existing.setComissao(viagem.getComissao());
                    existing.setDataInicio(viagem.getDataInicio());
                    existing.setDataFim(viagem.getDataFim());
                    existing.setStatus(viagem.getStatus());
                    Viagem saved = viagemRepository.save(existing);
                    return toDTO(saved);
                });
    }

    public void delete(UUID id, UUID usuarioId) {
        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        viagemRepository.delete(viagem);
    }

    @Transactional(readOnly = true)
    public List<ViagemRelatorioDTO> findDadosByDataInicioFim(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Viagem> viagens = viagemRepository.findByPeriodoSobreposto(usuarioId, dataInicio, dataFim);
        return viagens.stream().map(this::toRelatorioDto).toList();
    }

    private ViagemRelatorioDTO toRelatorioDto(Viagem v) {
        BigDecimal comissaoCalculada = v.getValorFrete().multiply(v.getComissao().divide(new BigDecimal(100)));

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

    private void setManagedRelationships(Viagem viagem, UUID usuarioId) {
        viagem.setEmpresa(resolveEmpresa(viagem, usuarioId));
        viagem.setProfissional(resolveProfissional(viagem, usuarioId));
        viagem.setVeiculo(resolveVeiculo(viagem, usuarioId));
    }

    private Empresa resolveEmpresa(Viagem viagem, UUID usuarioId) {
        if (viagem.getEmpresa() == null || viagem.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("Selecione uma empresa para a viagem.");
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
        return veiculoRepository.findByIdAndProfissional_Usuario_Id(viagem.getVeiculo().getId(), usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Veiculo nao encontrado para este usuario."));
    }

    private ViagemDTO toDTO(Viagem viagem) {
        ViagemDTO dto = new ViagemDTO();
        dto.setId(viagem.getId());
        dto.setInicioFrete(viagem.getInicioFrete());
        dto.setFimFrete(viagem.getFimFrete());
        dto.setValorFrete(viagem.getValorFrete());
        dto.setComissao(viagem.getComissao());
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
            veiculoDTO.setAno(viagem.getVeiculo().getAno());
            veiculoDTO.setPlaca(viagem.getVeiculo().getPlaca());
            veiculoDTO.setNome(viagem.getVeiculo().getNome());
            veiculoDTO.setQtdPeso(viagem.getVeiculo().getQtdPeso());
            veiculoDTO.setStatus(viagem.getVeiculo().getStatus());
            dto.setVeiculo(veiculoDTO);
        }

        return dto;
    }
}
