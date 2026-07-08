package com.djelog.services;

import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ProfissionalRepository profissionalRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ProfissionalRepository profissionalRepository) {
        this.veiculoRepository = veiculoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> findAll(UUID usuarioId) {
        return veiculoRepository.findByProfissional_Usuario_Id(usuarioId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<VeiculoDTO> findById(UUID id, UUID usuarioId) {
        return veiculoRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(this::toDTO);
    }

    public VeiculoDTO create(Veiculo veiculo, UUID usuarioId) {
        veiculo.setProfissional(resolveProfissional(veiculo, usuarioId));
        Veiculo saved = veiculoRepository.save(veiculo);
        return toDTO(saved);
    }

    public Optional<VeiculoDTO> update(UUID id, Veiculo veiculo, UUID usuarioId) {
        Profissional profissional = resolveProfissional(veiculo, usuarioId);

        return veiculoRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(existing -> {
                    existing.setMarca(veiculo.getMarca());
                    existing.setAno(veiculo.getAno());
                    existing.setProfissional(profissional);
                    existing.setPlaca(veiculo.getPlaca());
                    existing.setNome(veiculo.getNome());
                    existing.setQtdPeso(veiculo.getQtdPeso());
                    existing.setStatus(veiculo.getStatus());
                    Veiculo saved = veiculoRepository.save(existing);
                    return toDTO(saved);
                });
    }

    public void delete(UUID id, UUID usuarioId) {
        Veiculo veiculo = veiculoRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        veiculoRepository.delete(veiculo);
    }

    private Profissional resolveProfissional(Veiculo veiculo, UUID usuarioId) {
        if (veiculo.getProfissional() == null || veiculo.getProfissional().getId() == null) {
            throw new IllegalArgumentException("Selecione um motorista para o veiculo.");
        }
        return profissionalRepository.findByIdAndUsuario_Id(veiculo.getProfissional().getId(), usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Motorista nao encontrado para este usuario."));
    }

    private VeiculoDTO toDTO(Veiculo veiculo) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setId(veiculo.getId());
        dto.setMarca(veiculo.getMarca());
        dto.setAno(veiculo.getAno());
        dto.setPlaca(veiculo.getPlaca());
        dto.setNome(veiculo.getNome());
        dto.setQtdPeso(veiculo.getQtdPeso());
        dto.setStatus(veiculo.getStatus());

        if (veiculo.getProfissional() != null) {
            ProfissionalDTO profissionalDTO = new ProfissionalDTO();
            profissionalDTO.setId(veiculo.getProfissional().getId());
            profissionalDTO.setNome(veiculo.getProfissional().getNome());
            profissionalDTO.setTelefone(veiculo.getProfissional().getTelefone());
            profissionalDTO.setStatus(veiculo.getProfissional().getStatus());
            dto.setProfissional(profissionalDTO);
        }

        return dto;
    }
}
