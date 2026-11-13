package com.djelog.solutions;

import com.djelog.dtos.RegistrarPagamentoMotoristaRequest;
import com.djelog.dtos.MotoristaFinanceiroDTO;
import com.djelog.entities.Profissional;
import com.djelog.entities.PagamentoMotorista;
import com.djelog.entities.ControleFinanceiroMotorista;
import com.djelog.entities.Usuario;
import com.djelog.entities.Viagem;
import com.djelog.repositories.PagamentoMotoristaItemRepository;
import com.djelog.repositories.PagamentoMotoristaRepository;
import com.djelog.repositories.ControleFinanceiroMotoristaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.services.ProfissionalFinanceiroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalFinanceiroServiceTest {
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ControleFinanceiroMotoristaRepository controleRepository;
    @Mock private ViagemRepository viagemRepository;
    @Mock private PagamentoMotoristaRepository pagamentoRepository;
    @Mock private PagamentoMotoristaItemRepository itemRepository;

    private ProfissionalFinanceiroService service;
    private final UUID usuarioId = UUID.randomUUID();
    private final UUID profissionalId = UUID.randomUUID();
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        service = new ProfissionalFinanceiroService(profissionalRepository, controleRepository, viagemRepository,
                pagamentoRepository, itemRepository);
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        profissional = new Profissional();
        profissional.setId(profissionalId);
        profissional.setNome("João da Silva");
        profissional.setUsuario(usuario);
        when(profissionalRepository.findByIdAndUsuario_Id(profissionalId, usuarioId))
                .thenReturn(Optional.of(profissional));
    }

    @Test
    void returnsAllHistoricalTripsWithoutInitialControlAndKeepsStatusSemantics() {
        Viagem oldCompleted = trip(UUID.randomUUID(), LocalDate.now().minusYears(2), "CONCLUIDA", "1000.00");
        Viagem active = trip(UUID.randomUUID(), LocalDate.now(), "EM_ANDAMENTO", "800.00");
        Viagem cancelled = trip(UUID.randomUUID(), LocalDate.now().minusDays(3), "CANCELADA", "500.00");
        when(viagemRepository.findFinanceiroTrips(eq(profissionalId), eq(usuarioId)))
                .thenReturn(List.of(oldCompleted, active, cancelled));
        when(itemRepository.sumByViagemIds(anyCollection())).thenReturn(List.of());
        ControleFinanceiroMotorista legacy = mock(ControleFinanceiroMotorista.class);
        UUID legacyId = UUID.randomUUID();
        when(legacy.getId()).thenReturn(legacyId);
        when(legacy.getSaldoInicialPendente()).thenReturn(new BigDecimal("300.00"));
        when(controleRepository.findByProfissional_IdAndUsuario_Id(profissionalId, usuarioId))
                .thenReturn(Optional.of(legacy));
        when(itemRepository.sumByControleId(legacyId)).thenReturn(new BigDecimal("100.00"));
        when(pagamentoRepository.findDetailsByPeriod(eq(profissionalId), eq(usuarioId),
                isNull(), isNull())).thenReturn(List.of());
        lenient().when(pagamentoRepository.findDetailsByPeriod(eq(profissionalId), eq(usuarioId),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        MotoristaFinanceiroDTO result = service.consultar(profissionalId, usuarioId, null, null, null);

        assertEquals(3, result.viagens().size(), "the full trip history includes cancelled rows for reference");
        assertEquals(new BigDecimal("150.00"), result.resumo().saldoEmAberto());
        assertEquals(new BigDecimal("200.00"), result.resumo().saldoLegadoSemViagem(),
                "the preserved aggregate is shown separately and is not assigned to trip balances");
        assertEquals(new BigDecimal("120.00"), result.resumo().comissaoProjetadaPeriodo());
        MotoristaFinanceiroDTO.Viagem completedRow = result.viagens().stream()
                .filter(row -> "CONCLUIDA".equals(row.status())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("150.00"), completedRow.comissao());
        assertTrue(result.viagens().stream().anyMatch(row -> row.previsao() && row.saldo().signum() == 0));
        MotoristaFinanceiroDTO.Viagem cancelledRow = result.viagens().stream()
                .filter(row -> "CANCELADA".equals(row.status())).findFirst().orElseThrow();
        assertEquals(BigDecimal.ZERO.setScale(2), cancelledRow.comissao());
        assertEquals(BigDecimal.ZERO.setScale(2), cancelledRow.saldo());
        verify(viagemRepository).findFinanceiroTrips(profissionalId, usuarioId);
    }

    @Test
    void keepsOpenBalanceHistoricalWhenTripListIsFilteredByPeriod() {
        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.withDayOfMonth(1);
        Viagem current = trip(UUID.randomUUID(), today.minusDays(1), "CONCLUIDA", "1000.00");
        Viagem historical = trip(UUID.randomUUID(), today.minusYears(2), "CONCLUIDA", "1000.00");
        when(viagemRepository.findFinanceiroTrips(eq(profissionalId), eq(usuarioId)))
                .thenReturn(List.of(current, historical));
        when(itemRepository.sumByViagemIds(anyCollection())).thenReturn(List.of());
        lenient().when(pagamentoRepository.findDetailsByPeriod(eq(profissionalId), eq(usuarioId),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        MotoristaFinanceiroDTO result = service.consultar(profissionalId, usuarioId, periodStart, today, null);

        assertEquals(1, result.viagens().size(), "the period filter should still limit the visible trip list");
        assertEquals(new BigDecimal("150.00"), result.resumo().comissaoLiberadaPeriodo());
        assertEquals(new BigDecimal("300.00"), result.resumo().saldoEmAberto(),
                "the all-time amount available for payment must not shrink with the list filter");
        verify(viagemRepository).findFinanceiroTrips(profissionalId, usuarioId);
    }

    @Test
    void registersTripCommissionPaymentWithoutInitialControl() {
        Viagem completed = trip(UUID.randomUUID(), LocalDate.now().minusDays(1), "CONCLUIDA", "1000.00");
        when(viagemRepository.findByIdAndProfissional_Usuario_IdForUpdate(completed.getId(), usuarioId))
                .thenReturn(Optional.of(completed));
        when(itemRepository.sumByViagemId(completed.getId())).thenReturn(BigDecimal.ZERO);
        when(pagamentoRepository.save(any(PagamentoMotorista.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        RegistrarPagamentoMotoristaRequest request = new RegistrarPagamentoMotoristaRequest(
                LocalDate.now(), "PIX", null, null,
                List.of(new RegistrarPagamentoMotoristaRequest.Alocacao(
                        completed.getId(), new BigDecimal("150.00"))));

        assertDoesNotThrow(() -> service.registrarPagamento(profissionalId, usuarioId, request));

        ArgumentCaptor<PagamentoMotorista> captor = ArgumentCaptor.forClass(PagamentoMotorista.class);
        verify(pagamentoRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getValor());
        assertEquals(1, captor.getValue().getItens().size());
        assertSame(completed, captor.getValue().getItens().getFirst().getViagem());
        assertNull(captor.getValue().getItens().getFirst().getControle());
        verify(viagemRepository).findByIdAndProfissional_Usuario_IdForUpdate(completed.getId(), usuarioId);
    }

    private Viagem trip(UUID id, LocalDate date, String status, String freight) {
        Viagem trip = new Viagem();
        trip.setId(id);
        trip.setProfissional(profissional);
        trip.setInicioFrete("São Paulo");
        trip.setFimFrete("Curitiba");
        trip.setValorFrete(new BigDecimal(freight));
        trip.setComissao(new BigDecimal("15.00"));
        trip.setDataInicio(date.atTime(10, 0));
        trip.setStatus(status);
        return trip;
    }
}
