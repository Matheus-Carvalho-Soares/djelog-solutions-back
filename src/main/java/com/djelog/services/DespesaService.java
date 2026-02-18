package com.djelog.services;

import com.djelog.dtos.DespesaDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.entities.Despesa;
import com.djelog.entities.Viagem;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final ViagemRepository viagemRepository;

    public DespesaService(DespesaRepository despesaRepository, ViagemRepository viagemRepository) {
        this.despesaRepository = despesaRepository;
        this.viagemRepository = viagemRepository;
    }

    public List<DespesaDTO> findAll() {
        return despesaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DespesaDTO findById(UUID id) {
        Despesa despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));
        return toDTO(despesa);
    }

    public List<DespesaDTO> findByViagemId(UUID viagemId) {
        return despesaRepository.findByViagemIdWithViagem(viagemId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DespesaDTO create(DespesaDTO despesaDTO) {
        Despesa despesa = toEntity(despesaDTO);
        Despesa savedDespesa = despesaRepository.save(despesa);
        return toDTO(savedDespesa);
    }

    public DespesaDTO update(UUID id, DespesaDTO despesaDTO) {
        Despesa existingDespesa = despesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        updateEntityFromDTO(existingDespesa, despesaDTO);
        Despesa updatedDespesa = despesaRepository.save(existingDespesa);
        return toDTO(updatedDespesa);
    }

    public void delete(UUID id) {
        if (!despesaRepository.existsById(id)) {
            throw new RuntimeException("Despesa não encontrada");
        }
        despesaRepository.deleteById(id);
    }

    private DespesaDTO toDTO(Despesa despesa) {
        DespesaDTO dto = new DespesaDTO();
        dto.setId(despesa.getId());
        dto.setNome(despesa.getNome());
        dto.setDescricao(despesa.getDescricao());
        dto.setValor(despesa.getValor());
        
        if (despesa.getViagem() != null) {
            ViagemDTO viagemDTO = new ViagemDTO();
            viagemDTO.setId(despesa.getViagem().getId());
            viagemDTO.setInicioFrete(despesa.getViagem().getInicioFrete());
            viagemDTO.setFimFrete(despesa.getViagem().getFimFrete());
            dto.setViagem(viagemDTO);
        }
        
        return dto;
    }

    private Despesa toEntity(DespesaDTO dto) {
        Despesa despesa = new Despesa();
        despesa.setId(dto.getId());
        despesa.setNome(dto.getNome());
        despesa.setDescricao(dto.getDescricao());
        despesa.setValor(dto.getValor());
        
        if (dto.getViagem() != null && dto.getViagem().getId() != null) {
            Viagem viagem = viagemRepository.findById(dto.getViagem().getId())
                    .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));
            despesa.setViagem(viagem);
        }
        
        return despesa;
    }

    private void updateEntityFromDTO(Despesa despesa, DespesaDTO dto) {
        despesa.setNome(dto.getNome());
        despesa.setDescricao(dto.getDescricao());
        despesa.setValor(dto.getValor());
        
        if (dto.getViagem() != null && dto.getViagem().getId() != null) {
            Viagem viagem = viagemRepository.findById(dto.getViagem().getId())
                    .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));
            despesa.setViagem(viagem);
        }
    }
}
