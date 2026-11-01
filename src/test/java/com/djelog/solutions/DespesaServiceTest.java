package com.djelog.solutions;

import com.djelog.dtos.DespesaDTO;
import com.djelog.dtos.ParcelaDespesaDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.entities.Despesa;
import com.djelog.entities.Usuario;
import com.djelog.entities.Viagem;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.UsuarioRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.services.DespesaService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {
    @Mock private DespesaRepository despesaRepository;
    @Mock private ViagemRepository viagemRepository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private DespesaService service;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new DespesaService(despesaRepository, viagemRepository, veiculoRepository, usuarioRepository);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(new Usuario()));
        when(despesaRepository.save(any(Despesa.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void defaultsTripExpenseToPaidAndUsesExpenseDateForPayment() {
        UUID viagemId = UUID.randomUUID();
        LocalDate dataDespesa = LocalDate.of(2026, 10, 12);
        Viagem viagem = new Viagem();
        viagem.setId(viagemId);
        viagem.setDataInicio(dataDespesa.atStartOfDay());
        when(viagemRepository.findByIdAndProfissional_Usuario_Id(viagemId, usuarioId)).thenReturn(Optional.of(viagem));

        DespesaDTO input = expense(dataDespesa);
        ViagemDTO tripRef = new ViagemDTO();
        tripRef.setId(viagemId);
        input.setViagem(tripRef);

        DespesaDTO saved = service.create(input, usuarioId);

        assertEquals(dataDespesa, saved.getDataDespesa());
        assertEquals("Auto Posto", saved.getRecebedor());
        assertEquals("PAGO", saved.getParcelas().get(0).getStatusPagamento());
        assertEquals(dataDespesa, saved.getParcelas().get(0).getDataPagamento());
        assertEquals(dataDespesa, saved.getParcelas().get(0).getDataVencimento());
    }

    @Test
    void keepsStandaloneExpensePendingByDefault() {
        LocalDate dataDespesa = LocalDate.of(2026, 10, 13);
        DespesaDTO saved = service.create(expense(dataDespesa), usuarioId);

        assertEquals("PENDENTE", saved.getParcelas().get(0).getStatusPagamento());
        assertEquals(dataDespesa, saved.getDataDespesa());
        assertEquals("Auto Posto", saved.getRecebedor());
    }

    @Test
    void persistsNewExpenseMetadata() {
        LocalDate dataDespesa = LocalDate.of(2026, 10, 14);
        DespesaDTO input = expense(dataDespesa);

        service.create(input, usuarioId);

        ArgumentCaptor<Despesa> captor = ArgumentCaptor.forClass(Despesa.class);
        verify(despesaRepository).save(captor.capture());
        assertEquals(dataDespesa, captor.getValue().getDataDespesa());
        assertEquals("Auto Posto", captor.getValue().getRecebedor());
    }

    private DespesaDTO expense(LocalDate dataDespesa) {
        DespesaDTO dto = new DespesaDTO();
        dto.setNome("Combustível");
        dto.setRecebedor("Auto Posto");
        dto.setDataDespesa(dataDespesa);
        dto.setCategoria("COMBUSTIVEL");
        dto.setValor(new BigDecimal("120.00"));
        ParcelaDespesaDTO installment = new ParcelaDespesaDTO();
        installment.setValor(new BigDecimal("120.00"));
        installment.setDataVencimento(dataDespesa);
        installment.setNumeroParcela(1);
        dto.setParcelas(List.of(installment));
        return dto;
    }
}
