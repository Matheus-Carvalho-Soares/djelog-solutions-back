package com.djelog.services;

import com.djelog.dtos.ViagemComissionadaDTO;
import com.djelog.entities.Usuario;
import com.djelog.entities.ViagemComissionada;
import com.djelog.repositories.ViagemComissionadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ViagemComissionadaService {

    private final ViagemComissionadaRepository viagemComissionadaRepository;
    private final UsuarioService usuarioService;

    public ViagemComissionadaService(ViagemComissionadaRepository viagemComissionadaRepository, UsuarioService usuarioService) {
        this.viagemComissionadaRepository = viagemComissionadaRepository;
        this.usuarioService = usuarioService;
    }

    public List<ViagemComissionadaDTO> findAll(UUID usuarioId) {
        return viagemComissionadaRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ViagemComissionadaDTO findById(UUID id, UUID usuarioId) {
        ViagemComissionada viagemComissionada = viagemComissionadaRepository.findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        return toDTO(viagemComissionada);
    }

    public List<ViagemComissionadaDTO> findByInicioFrete(String inicioFrete, UUID usuarioId) {
        return viagemComissionadaRepository.findByInicioFreteContainingAndUsuario_Id(inicioFrete, usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ViagemComissionadaDTO> findByFimFrete(String fimFrete, UUID usuarioId) {
        return viagemComissionadaRepository.findByFimFreteContainingAndUsuario_Id(fimFrete, usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ViagemComissionadaDTO create(ViagemComissionadaDTO dto, UUID usuarioId) {
        Usuario usuario = usuarioService.findById(usuarioId);
        ViagemComissionada viagemComissionada = toEntity(dto);
        viagemComissionada.setUsuario(usuario);
        ViagemComissionada saved = viagemComissionadaRepository.save(viagemComissionada);
        return toDTO(saved);
    }

    public ViagemComissionadaDTO update(UUID id, ViagemComissionadaDTO dto, UUID usuarioId) {
        ViagemComissionada existing = viagemComissionadaRepository.findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);

        updateEntityFromDTO(existing, dto);
        ViagemComissionada updated = viagemComissionadaRepository.save(existing);
        return toDTO(updated);
    }

    public void delete(UUID id, UUID usuarioId) {
        if (!viagemComissionadaRepository.existsByIdAndUsuario_Id(id, usuarioId)) {
            throw new NoSuchElementException();
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
        dto.setDescricao(entity.getDescricao());
        return dto;
    }

    private ViagemComissionada toEntity(ViagemComissionadaDTO dto) {
        ViagemComissionada entity = new ViagemComissionada();
        entity.setInicioFrete(dto.getInicioFrete());
        entity.setFimFrete(dto.getFimFrete());
        entity.setValor(dto.getValor());
        entity.setComissao(dto.getComissao());
        entity.setDescricao(dto.getDescricao());
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
