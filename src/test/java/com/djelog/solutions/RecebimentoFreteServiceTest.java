package com.djelog.solutions;

import com.djelog.dtos.RecebimentoFreteDTO;
import com.djelog.entities.RecebimentoFrete;
import com.djelog.entities.Viagem;
import com.djelog.repositories.RecebimentoFreteRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.services.RecebimentoFreteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecebimentoFreteServiceTest {
    @Mock private RecebimentoFreteRepository repository;
    @Mock private ViagemRepository viagemRepository;
    private RecebimentoFreteService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID tripId = UUID.randomUUID();
    private final UUID receiptId = UUID.randomUUID();
    private Viagem trip;
    private RecebimentoFrete receipt;

    @BeforeEach
    void setUp() {
        service = new RecebimentoFreteService(repository, viagemRepository);
        trip = new Viagem(); trip.setId(tripId); trip.setValorFrete(new BigDecimal("300.00"));
        receipt = new RecebimentoFrete(); receipt.setId(receiptId); receipt.setViagem(trip);
        receipt.setValor(new BigDecimal("100.00")); receipt.setDataRecebimento(LocalDate.of(2026, 11, 1));
        when(viagemRepository.findByIdAndProfissional_Usuario_IdForUpdate(tripId, userId)).thenReturn(Optional.of(trip));
        when(repository.findByIdAndUsuarioId(receiptId, userId)).thenReturn(Optional.of(receipt));
    }

    @Test
    void updatesReceiptWhenNewTotalStaysWithinFreightAndKeepsInitialMarker() {
        receipt.setInicial(true);
        when(repository.sumByViagemId(tripId)).thenReturn(new BigDecimal("250.00"));
        when(repository.save(any(RecebimentoFrete.class))).thenAnswer(call -> call.getArgument(0));
        RecebimentoFreteDTO input = dto("50.00", LocalDate.of(2026, 11, 8));

        RecebimentoFreteDTO result = service.update(tripId, receiptId, input, userId);

        assertEquals(new BigDecimal("50.00"), result.getValor());
        assertEquals(LocalDate.of(2026, 11, 8), result.getDataRecebimento());
        assertTrue(result.isInicial());
    }

    @Test
    void rejectsReceiptEditThatWouldExceedFreight() {
        when(repository.sumByViagemId(tripId)).thenReturn(new BigDecimal("250.00"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.update(tripId, receiptId, dto("160.00", LocalDate.of(2026, 11, 8)), userId));

        assertTrue(error.getMessage().contains("ultrapassar o valor do frete"));
    }

    private RecebimentoFreteDTO dto(String value, LocalDate date) {
        RecebimentoFreteDTO dto = new RecebimentoFreteDTO();
        dto.setValor(new BigDecimal(value)); dto.setDataRecebimento(date); dto.setObservacao("ajuste");
        return dto;
    }
}
