package com.djelog.solutions;

import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.services.VeiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private ProfissionalRepository profissionalRepository;

    private VeiculoService service;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new VeiculoService(veiculoRepository, profissionalRepository);
    }

    @Test
    void createsTrailerLinkedToUsersTruckAndOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID truckId = UUID.randomUUID();
        Profissional owner = professional(ownerId);
        Veiculo truck = new Veiculo();
        truck.setId(truckId);
        truck.setTipo("CAMINHAO");
        when(profissionalRepository.findByIdAndUsuario_Id(ownerId, usuarioId)).thenReturn(Optional.of(owner));
        when(veiculoRepository.findByIdAndUsuarioId(truckId, usuarioId)).thenReturn(Optional.of(truck));
        when(veiculoRepository.existsByCaminhao_Id(truckId)).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoDTO request = new VeiculoDTO();
        request.setTipo("CARRETA");
        request.setModelo("Graneleira");
        request.setAno(2024);
        request.setPlaca("ABC1D23");
        request.setCaminhaoId(truckId);
        request.setProprietario(professionalRef(ownerId));

        VeiculoDTO saved = service.create(request, usuarioId);

        assertEquals("CARRETA", saved.getTipo());
        assertEquals("Graneleira", saved.getModelo());
        assertEquals(truckId, saved.getCaminhaoId());
        assertEquals(ownerId, saved.getProprietario().getId());
    }

    @Test
    void rejectsOwnerFromAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        when(profissionalRepository.findByIdAndUsuario_Id(ownerId, usuarioId)).thenReturn(Optional.empty());
        VeiculoDTO request = new VeiculoDTO();
        request.setTipo("CARRETA");
        request.setModelo("Graneleira");
        request.setAno(2024);
        request.setPlaca("ABC1D23");
        request.setProprietario(professionalRef(ownerId));

        assertThrows(AccessDeniedException.class, () -> service.create(request, usuarioId));
    }

    private Profissional professional(UUID id) {
        Profissional professional = new Profissional();
        professional.setId(id);
        professional.setNome("Proprietário");
        return professional;
    }

    private ProfissionalDTO professionalRef(UUID id) {
        ProfissionalDTO ref = new ProfissionalDTO();
        ref.setId(id);
        return ref;
    }
}
