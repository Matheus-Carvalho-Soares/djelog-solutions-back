package com.djelog.services;

import com.djelog.dtos.RecebimentoFreteDTO;
import com.djelog.entities.RecebimentoFrete;
import com.djelog.entities.Viagem;
import com.djelog.repositories.RecebimentoFreteRepository;
import com.djelog.repositories.ViagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class RecebimentoFreteService {
    private final RecebimentoFreteRepository repository;
    private final ViagemRepository viagemRepository;

    public RecebimentoFreteService(RecebimentoFreteRepository repository, ViagemRepository viagemRepository) {
        this.repository = repository;
        this.viagemRepository = viagemRepository;
    }

    @Transactional(readOnly = true)
    public List<RecebimentoFreteDTO> findByViagem(UUID viagemId, UUID usuarioId) {
        ensureTrip(viagemId, usuarioId);
        return repository.findByViagemIdAndUsuarioId(viagemId, usuarioId).stream().map(this::toDTO).toList();
    }

    public RecebimentoFreteDTO create(UUID viagemId, RecebimentoFreteDTO dto, UUID usuarioId) {
        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_IdForUpdate(viagemId, usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Viagem nao encontrada para este usuario."));
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor recebido deve ser maior que zero.");
        }
        BigDecimal recebido = repository.sumByViagemId(viagemId);
        if (recebido.add(dto.getValor()).compareTo(viagem.getValorFrete()) > 0) {
            throw new IllegalArgumentException("O total recebido nao pode ultrapassar o valor do frete.");
        }
        RecebimentoFrete entity = new RecebimentoFrete();
        entity.setViagem(viagem);
        entity.setValor(dto.getValor());
        entity.setDataRecebimento(dto.getDataRecebimento());
        entity.setObservacao(dto.getObservacao());
        entity.setInicial(false);
        return toDTO(repository.save(entity));
    }

    public void delete(UUID viagemId, UUID recebimentoId, UUID usuarioId) {
        ensureTrip(viagemId, usuarioId);
        RecebimentoFrete recebimento = repository.findByIdAndUsuarioId(recebimentoId, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        if (!recebimento.getViagem().getId().equals(viagemId)) throw new NoSuchElementException();
        repository.delete(recebimento);
    }

    private Viagem ensureTrip(UUID viagemId, UUID usuarioId) {
        return viagemRepository.findByIdAndProfissional_Usuario_Id(viagemId, usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Viagem nao encontrada para este usuario."));
    }

    private RecebimentoFreteDTO toDTO(RecebimentoFrete entity) {
        RecebimentoFreteDTO dto = new RecebimentoFreteDTO();
        dto.setId(entity.getId());
        dto.setValor(entity.getValor());
        dto.setDataRecebimento(entity.getDataRecebimento());
        dto.setObservacao(entity.getObservacao());
        dto.setInicial(entity.isInicial());
        return dto;
    }
}
