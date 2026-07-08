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
import java.util.NoSuchElementException;
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

    public List<DespesaDTO> findAll(UUID usuarioId) {
        return despesaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DespesaDTO findById(UUID id, UUID usuarioId) {
        Despesa despesa = despesaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        return toDTO(despesa);
    }

    public List<DespesaDTO> findByViagemId(UUID viagemId, UUID usuarioId) {
        return despesaRepository.findByViagemIdAndUsuarioId(viagemId, usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DespesaDTO create(DespesaDTO despesaDTO, UUID usuarioId) {
        Despesa despesa = toEntity(despesaDTO, usuarioId);
        Despesa savedDespesa = despesaRepository.save(despesa);
        return toDTO(savedDespesa);
    }

    public DespesaDTO update(UUID id, DespesaDTO despesaDTO, UUID usuarioId) {
        Despesa existingDespesa = despesaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);

        updateEntityFromDTO(existingDespesa, despesaDTO, usuarioId);
        Despesa updatedDespesa = despesaRepository.save(existingDespesa);
        return toDTO(updatedDespesa);
    }

    public void delete(UUID id, UUID usuarioId) {
        if (!despesaRepository.existsByIdAndUsuarioId(id, usuarioId)) {
            throw new NoSuchElementException();
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

    private Despesa toEntity(DespesaDTO dto, UUID usuarioId) {
        Despesa despesa = new Despesa();
        despesa.setNome(dto.getNome());
        despesa.setDescricao(dto.getDescricao());
        despesa.setValor(dto.getValor());

        if (dto.getViagem() == null || dto.getViagem().getId() == null) {
            throw new IllegalArgumentException("Selecione uma viagem para a despesa.");
        }

        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(dto.getViagem().getId(), usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Viagem nao encontrada para este usuario."));
        despesa.setViagem(viagem);

        return despesa;
    }

    private void updateEntityFromDTO(Despesa despesa, DespesaDTO dto, UUID usuarioId) {
        despesa.setNome(dto.getNome());
        despesa.setDescricao(dto.getDescricao());
        despesa.setValor(dto.getValor());

        if (dto.getViagem() == null || dto.getViagem().getId() == null) {
            throw new IllegalArgumentException("Selecione uma viagem para a despesa.");
        }

        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(dto.getViagem().getId(), usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Viagem nao encontrada para este usuario."));
        despesa.setViagem(viagem);
    }
}
