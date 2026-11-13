package com.djelog.solutions;

import com.djelog.dtos.ConfirmacaoPagamentoLoteDTO;
import com.djelog.dtos.ItemPagamentoLoteDTO;
import com.djelog.entities.Despesa;
import com.djelog.entities.ParcelaDespesa;
import com.djelog.entities.Usuario;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.UsuarioRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.services.DespesaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespesaBatchPaymentServiceTest {
    @Mock private DespesaRepository despesaRepository;
    @Mock private ViagemRepository viagemRepository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private DespesaService service;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new DespesaService(despesaRepository, viagemRepository, veiculoRepository, usuarioRepository);
    }

    @Test
    void confirmsSeveralPendingInstallmentsWithTheirOwnDates() {
        Despesa first = expense(UUID.randomUUID(), installment(UUID.randomUUID()));
        Despesa second = expense(UUID.randomUUID(), installment(UUID.randomUUID()));
        when(despesaRepository.findByIdAndUsuarioIdForUpdate(first.getId(), usuarioId)).thenReturn(Optional.of(first));
        when(despesaRepository.findByIdAndUsuarioIdForUpdate(second.getId(), usuarioId)).thenReturn(Optional.of(second));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(call -> call.getArgument(0));

        ConfirmacaoPagamentoLoteDTO request = new ConfirmacaoPagamentoLoteDTO(List.of(
                new ItemPagamentoLoteDTO(first.getId(), first.getParcelas().getFirst().getId(), LocalDate.of(2026, 11, 3)),
                new ItemPagamentoLoteDTO(second.getId(), second.getParcelas().getFirst().getId(), LocalDate.of(2026, 11, 4))));

        service.confirmarPagamentosLote(request, usuarioId);

        assertEquals("PAGO", first.getParcelas().getFirst().getStatusPagamento());
        assertEquals(LocalDate.of(2026, 11, 3), first.getParcelas().getFirst().getDataPagamento());
        assertEquals("PAGO", second.getParcelas().getFirst().getStatusPagamento());
        assertEquals(LocalDate.of(2026, 11, 4), second.getParcelas().getFirst().getDataPagamento());
        verify(despesaRepository, times(2)).save(any(Despesa.class));
    }

    @Test
    void validatesEveryInstallmentBeforeSavingAnyExpense() {
        Despesa valid = expense(UUID.randomUUID(), installment(UUID.randomUUID()));
        Despesa alreadyPaid = expense(UUID.randomUUID(), installment(UUID.randomUUID()));
        alreadyPaid.getParcelas().getFirst().setStatusPagamento("PAGO");
        when(despesaRepository.findByIdAndUsuarioIdForUpdate(valid.getId(), usuarioId)).thenReturn(Optional.of(valid));
        when(despesaRepository.findByIdAndUsuarioIdForUpdate(alreadyPaid.getId(), usuarioId)).thenReturn(Optional.of(alreadyPaid));
        ConfirmacaoPagamentoLoteDTO request = new ConfirmacaoPagamentoLoteDTO(List.of(
                new ItemPagamentoLoteDTO(valid.getId(), valid.getParcelas().getFirst().getId(), LocalDate.of(2026, 11, 3)),
                new ItemPagamentoLoteDTO(alreadyPaid.getId(), alreadyPaid.getParcelas().getFirst().getId(), LocalDate.of(2026, 11, 4))));

        assertThrows(IllegalArgumentException.class, () -> service.confirmarPagamentosLote(request, usuarioId));
        assertEquals("PENDENTE", valid.getParcelas().getFirst().getStatusPagamento());
        verify(despesaRepository, never()).save(any(Despesa.class));
    }

    private Despesa expense(UUID id, ParcelaDespesa parcela) {
        Despesa expense = new Despesa();
        expense.setId(id);
        expense.setUsuario(new Usuario());
        expense.setNome("Despesa");
        expense.setParcelas(List.of(parcela));
        return expense;
    }

    private ParcelaDespesa installment(UUID id) {
        ParcelaDespesa installment = new ParcelaDespesa();
        installment.setId(id);
        installment.setStatusPagamento("PENDENTE");
        installment.setDataVencimento(LocalDate.of(2026, 11, 1));
        installment.setNumeroParcela(1);
        return installment;
    }
}
