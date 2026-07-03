package com.djelog.services;

import com.djelog.dtos.EstadiaDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.entities.Estadia;
import com.djelog.entities.Viagem;
import com.djelog.repositories.EstadiaRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EstadiaService {

    private final EstadiaRepository estadiaRepository;
    private final ViagemRepository viagemRepository;

    public EstadiaService(EstadiaRepository estadiaRepository, ViagemRepository viagemRepository) {
        this.estadiaRepository = estadiaRepository;
        this.viagemRepository = viagemRepository;
    }

    public List<EstadiaDTO> findAll(UUID usuarioId) {
        return estadiaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EstadiaDTO findById(UUID id, UUID usuarioId) {
        Estadia estadia = estadiaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        return toDTO(estadia);
    }

    public List<EstadiaDTO> findByViagemId(UUID viagemId, UUID usuarioId) {
        return estadiaRepository.findByViagemIdAndUsuarioId(viagemId, usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EstadiaDTO create(EstadiaDTO estadiaDTO, UUID usuarioId) {
        Estadia estadia = toEntity(estadiaDTO, usuarioId);
        Estadia savedEstadia = estadiaRepository.save(estadia);
        return toDTO(savedEstadia);
    }

    public EstadiaDTO update(UUID id, EstadiaDTO estadiaDTO, UUID usuarioId) {
        Estadia existingEstadia = estadiaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);

        updateEntityFromDTO(existingEstadia, estadiaDTO, usuarioId);
        Estadia updatedEstadia = estadiaRepository.save(existingEstadia);
        return toDTO(updatedEstadia);
    }

    public void delete(UUID id, UUID usuarioId) {
        if (!estadiaRepository.existsByIdAndUsuarioId(id, usuarioId)) {
            throw new NoSuchElementException();
        }
        estadiaRepository.deleteById(id);
    }

    private EstadiaDTO toDTO(Estadia estadia) {
        EstadiaDTO dto = new EstadiaDTO();
        dto.setId(estadia.getId());
        dto.setDescricao(estadia.getDescricao());
        dto.setValor(estadia.getValor());

        if (estadia.getViagem() != null) {
            ViagemDTO viagemDTO = new ViagemDTO();
            viagemDTO.setId(estadia.getViagem().getId());
            viagemDTO.setInicioFrete(estadia.getViagem().getInicioFrete());
            viagemDTO.setFimFrete(estadia.getViagem().getFimFrete());
            dto.setViagem(viagemDTO);
        }

        return dto;
    }

    private Estadia toEntity(EstadiaDTO dto, UUID usuarioId) {
        Estadia estadia = new Estadia();
        estadia.setDescricao(dto.getDescricao());
        estadia.setValor(dto.getValor());

        if (dto.getViagem() == null || dto.getViagem().getId() == null) {
            throw new IllegalArgumentException("Viagem obrigatória");
        }

        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(dto.getViagem().getId(), usuarioId)
                .orElseThrow(NoSuchElementException::new);
        estadia.setViagem(viagem);

        return estadia;
    }

    private void updateEntityFromDTO(Estadia estadia, EstadiaDTO dto, UUID usuarioId) {
        estadia.setDescricao(dto.getDescricao());
        estadia.setValor(dto.getValor());

        if (dto.getViagem() == null || dto.getViagem().getId() == null) {
            throw new IllegalArgumentException("Viagem obrigatória");
        }

        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(dto.getViagem().getId(), usuarioId)
                .orElseThrow(NoSuchElementException::new);
        estadia.setViagem(viagem);
    }
}
