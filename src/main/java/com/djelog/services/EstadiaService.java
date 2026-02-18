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

    public List<EstadiaDTO> findAll() {
        return estadiaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EstadiaDTO findById(UUID id) {
        Estadia estadia = estadiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estadia não encontrada"));
        return toDTO(estadia);
    }

    public List<EstadiaDTO> findByViagemId(UUID viagemId) {
        return estadiaRepository.findByViagemIdWithViagem(viagemId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EstadiaDTO create(EstadiaDTO estadiaDTO) {
        Estadia estadia = toEntity(estadiaDTO);
        Estadia savedEstadia = estadiaRepository.save(estadia);
        return toDTO(savedEstadia);
    }

    public EstadiaDTO update(UUID id, EstadiaDTO estadiaDTO) {
        Estadia existingEstadia = estadiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estadia não encontrada"));

        updateEntityFromDTO(existingEstadia, estadiaDTO);
        Estadia updatedEstadia = estadiaRepository.save(existingEstadia);
        return toDTO(updatedEstadia);
    }

    public void delete(UUID id) {
        if (!estadiaRepository.existsById(id)) {
            throw new RuntimeException("Estadia não encontrada");
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

    private Estadia toEntity(EstadiaDTO dto) {
        Estadia estadia = new Estadia();
        estadia.setId(dto.getId());
        estadia.setDescricao(dto.getDescricao());
        estadia.setValor(dto.getValor());
        
        if (dto.getViagem() != null && dto.getViagem().getId() != null) {
            Viagem viagem = viagemRepository.findById(dto.getViagem().getId())
                    .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));
            estadia.setViagem(viagem);
        }
        
        return estadia;
    }

    private void updateEntityFromDTO(Estadia estadia, EstadiaDTO dto) {
        estadia.setDescricao(dto.getDescricao());
        estadia.setValor(dto.getValor());
        
        if (dto.getViagem() != null && dto.getViagem().getId() != null) {
            Viagem viagem = viagemRepository.findById(dto.getViagem().getId())
                    .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));
            estadia.setViagem(viagem);
        }
    }
}
