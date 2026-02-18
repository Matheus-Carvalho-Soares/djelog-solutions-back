package com.djelog.services;

import com.djelog.dtos.ViagemComissionadaDTO;
import com.djelog.entities.ViagemComissionada;
import com.djelog.repositories.ViagemComissionadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ViagemComissionadaService {

    private final ViagemComissionadaRepository viagemComissionadaRepository;

    public ViagemComissionadaService(ViagemComissionadaRepository viagemComissionadaRepository) {
        this.viagemComissionadaRepository = viagemComissionadaRepository;
    }

    public List<ViagemComissionadaDTO> findAll() {
        return viagemComissionadaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ViagemComissionadaDTO findById(UUID id) {
        ViagemComissionada viagemComissionada = viagemComissionadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem Comissionada não encontrada"));
        return toDTO(viagemComissionada);
    }

    public List<ViagemComissionadaDTO> findByInicioFrete(String inicioFrete) {
        return viagemComissionadaRepository.findByInicioFreteContaining(inicioFrete).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ViagemComissionadaDTO> findByFimFrete(String fimFrete) {
        return viagemComissionadaRepository.findByFimFreteContaining(fimFrete).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ViagemComissionadaDTO create(ViagemComissionadaDTO dto) {
        ViagemComissionada viagemComissionada = toEntity(dto);
        ViagemComissionada saved = viagemComissionadaRepository.save(viagemComissionada);
        return toDTO(saved);
    }

    public ViagemComissionadaDTO update(UUID id, ViagemComissionadaDTO dto) {
        ViagemComissionada existing = viagemComissionadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem Comissionada não encontrada"));

        updateEntityFromDTO(existing, dto);
        ViagemComissionada updated = viagemComissionadaRepository.save(existing);
        return toDTO(updated);
    }

    public void delete(UUID id) {
        if (!viagemComissionadaRepository.existsById(id)) {
            throw new RuntimeException("Viagem Comissionada não encontrada");
        }
        viagemComissionadaRepository.deleteById(id);
    }

    private ViagemComissionadaDTO toDTO(ViagemComissionada entity) {
        ViagemComissionadaDTO dto = new ViagemComissionadaDTO();
        dto.setId(entity.getId());
        dto.setInicioFrete(entity.getInicioFrete());
        dto.setFimFrete(entity.getFimFrete());
        dto.setValor(entity.getValor());
        dto.setComissao(entity.getComissao());
        return dto;
    }

    private ViagemComissionada toEntity(ViagemComissionadaDTO dto) {
        ViagemComissionada entity = new ViagemComissionada();
        entity.setId(dto.getId());
        entity.setInicioFrete(dto.getInicioFrete());
        entity.setFimFrete(dto.getFimFrete());
        entity.setValor(dto.getValor());
        entity.setComissao(dto.getComissao());
        return entity;
    }

    private void updateEntityFromDTO(ViagemComissionada entity, ViagemComissionadaDTO dto) {
        entity.setInicioFrete(dto.getInicioFrete());
        entity.setFimFrete(dto.getFimFrete());
        entity.setValor(dto.getValor());
        entity.setComissao(dto.getComissao());
        entity.setDescricao(dto.getDescricao());
    }
}
