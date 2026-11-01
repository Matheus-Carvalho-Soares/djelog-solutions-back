package com.djelog.solutions;

import com.djelog.dtos.ViagemCreateRequestDTO;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.RecebimentoFreteRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.services.ViagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViagemServiceTest {
    @Mock private ViagemRepository viagemRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private RecebimentoFreteRepository recebimentoRepository;

    private ViagemService service;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ViagemService(viagemRepository, empresaRepository, profissionalRepository,
                veiculoRepository, recebimentoRepository);
    }

    @Test
    void exemptsOwnerDriverFromCommissionOnNewTrip() {
        UUID professionalId = UUID.randomUUID();
        UUID truckId = UUID.randomUUID();
        Profissional driver = professional(professionalId);
        Veiculo truck = new Veiculo();
        truck.setId(truckId);
        truck.setTipo("CAMINHAO");
        truck.setProprietario(driver);
        when(profissionalRepository.findByIdAndUsuario_Id(professionalId, usuarioId)).thenReturn(Optional.of(driver));
        when(veiculoRepository.findByIdAndUsuarioId(truckId, usuarioId)).thenReturn(Optional.of(truck));
        when(viagemRepository.save(any(Viagem.class))).thenAnswer(invocation -> {
            Viagem trip = invocation.getArgument(0);
            if (trip.getId() == null) trip.setId(UUID.randomUUID());
            return trip;
        });
        when(recebimentoRepository.sumByViagemId(any())).thenReturn(BigDecimal.ZERO);
        when(recebimentoRepository.sumInicialByViagemId(any())).thenReturn(BigDecimal.ZERO);

        var saved = service.create(request(professionalId, truckId), usuarioId);

        assertEquals(BigDecimal.ZERO.setScale(2), saved.getValorComissaoManual());
        assertEquals(BigDecimal.ZERO.setScale(2), saved.getValorComissao());
    }

    @Test
    void rejectsTrailerAsTripVehicle() {
        UUID professionalId = UUID.randomUUID();
        UUID trailerId = UUID.randomUUID();
        Profissional driver = professional(professionalId);
        Veiculo trailer = new Veiculo();
        trailer.setId(trailerId);
        trailer.setTipo("CARRETA");
        when(profissionalRepository.findByIdAndUsuario_Id(professionalId, usuarioId)).thenReturn(Optional.of(driver));
        when(veiculoRepository.findByIdAndUsuarioId(trailerId, usuarioId)).thenReturn(Optional.of(trailer));

        assertThrows(IllegalArgumentException.class, () -> service.create(request(professionalId, trailerId), usuarioId));
    }

    private ViagemCreateRequestDTO request(UUID professionalId, UUID vehicleId) {
        ViagemCreateRequestDTO request = new ViagemCreateRequestDTO();
        request.setProfissionalId(professionalId);
        request.setVeiculoId(vehicleId);
        request.setInicioFrete("Campinas - SP");
        request.setValorFrete(new BigDecimal("1000.00"));
        request.setComissao(new BigDecimal("15.00"));
        request.setFormaPagamentoFrete("A_VISTA");
        request.setDataRecebimentoInicial(LocalDate.of(2026, 10, 12));
        request.setDataInicio(LocalDateTime.of(2026, 10, 12, 8, 0));
        request.setStatus("EM_ANDAMENTO");
        return request;
    }

    private Profissional professional(UUID id) {
        Profissional professional = new Profissional();
        professional.setId(id);
        professional.setNome("Motorista");
        return professional;
    }
}
