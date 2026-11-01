package com.djelog.services;

import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VeiculoService {
    private static final String CAMINHAO = "CAMINHAO";
    private static final String CARRETA = "CARRETA";

    private final VeiculoRepository veiculoRepository;
    private final ProfissionalRepository profissionalRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ProfissionalRepository profissionalRepository) {
        this.veiculoRepository = veiculoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO> findAll(UUID usuarioId) {
        return veiculoRepository.findByUsuarioId(usuarioId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<VeiculoDTO> findById(UUID id, UUID usuarioId) {
        return veiculoRepository.findByIdAndUsuarioId(id, usuarioId).map(this::toDTO);
    }

    public VeiculoDTO create(VeiculoDTO dto, UUID usuarioId) {
        Veiculo veiculo = new Veiculo();
        applyDTO(veiculo, dto, usuarioId);
        return toDTO(veiculoRepository.save(veiculo));
    }

    public Optional<VeiculoDTO> update(UUID id, VeiculoDTO dto, UUID usuarioId) {
        return veiculoRepository.findByIdAndUsuarioId(id, usuarioId).map(existing -> {
            applyDTO(existing, dto, usuarioId);
            return toDTO(veiculoRepository.save(existing));
        });
    }

    public void delete(UUID id, UUID usuarioId) {
        Veiculo veiculo = veiculoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        veiculoRepository.delete(veiculo);
    }

    private void applyDTO(Veiculo veiculo, VeiculoDTO dto, UUID usuarioId) {
        String tipo = dto.getTipo() == null || dto.getTipo().isBlank()
                ? CAMINHAO : dto.getTipo().trim().toUpperCase(Locale.ROOT);
        if (!List.of(CAMINHAO, CARRETA).contains(tipo)) {
            throw new IllegalArgumentException("Tipo de veiculo invalido.");
        }
        if (CARRETA.equals(tipo) && veiculo.getId() != null
                && veiculoRepository.existsByCaminhao_Id(veiculo.getId())) {
            throw new IllegalArgumentException("Um caminhao com carreta vinculada nao pode ser convertido em carreta.");
        }

        Profissional motorista = resolveProfissional(dto.getProfissional(), usuarioId, tipo.equals(CAMINHAO));
        Profissional proprietario = resolveProfissional(dto.getProprietario(), usuarioId, false);
        Veiculo caminhao = resolveCaminhao(dto.getCaminhaoId(), usuarioId, veiculo.getId());

        if (CAMINHAO.equals(tipo)) {
            if (dto.getMarca() == null || dto.getMarca().isBlank()) {
                throw new IllegalArgumentException("Informe a marca do caminhao.");
            }
            if (dto.getCaminhaoId() != null) {
                throw new IllegalArgumentException("Um caminhao nao pode ser vinculado a outro caminhao.");
            }
        } else {
            if (dto.getModelo() == null || dto.getModelo().isBlank()) {
                throw new IllegalArgumentException("Informe o modelo da carreta.");
            }
            if (dto.getAno() == null) throw new IllegalArgumentException("Informe o ano da carreta.");
            if (proprietario == null) throw new IllegalArgumentException("Selecione o proprietario da carreta.");
            if (caminhao == null) throw new IllegalArgumentException("Selecione o caminhao vinculado a carreta.");
            boolean alreadyLinked = veiculo.getId() == null
                    ? veiculoRepository.existsByCaminhao_Id(caminhao.getId())
                    : veiculoRepository.existsByCaminhao_IdAndIdNot(caminhao.getId(), veiculo.getId());
            if (alreadyLinked) throw new IllegalArgumentException("Este caminhao ja possui uma carreta vinculada.");
        }

        veiculo.setTipo(tipo);
        veiculo.setMarca(blankToNull(dto.getMarca()));
        veiculo.setModelo(blankToNull(dto.getModelo()));
        veiculo.setAno(dto.getAno());
        veiculo.setPlaca(dto.getPlaca() == null ? null : dto.getPlaca().trim().toUpperCase(Locale.ROOT));
        veiculo.setNome(blankToNull(dto.getNome()));
        veiculo.setQtdPeso(dto.getQtdPeso());
        veiculo.setProfissional(motorista);
        veiculo.setProprietario(proprietario);
        veiculo.setCaminhao(caminhao);
        veiculo.setStatus(dto.getStatus() == null ? Boolean.TRUE : dto.getStatus());
    }

    private Profissional resolveProfissional(ProfissionalDTO ref, UUID usuarioId, boolean required) {
        if (ref == null || ref.getId() == null) {
            if (required) throw new IllegalArgumentException("Selecione um motorista para o veiculo.");
            return null;
        }
        return profissionalRepository.findByIdAndUsuario_Id(ref.getId(), usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Profissional nao encontrado para este usuario."));
    }

    private Veiculo resolveCaminhao(UUID id, UUID usuarioId, UUID currentVehicleId) {
        if (id == null) return null;
        if (id.equals(currentVehicleId)) throw new IllegalArgumentException("Uma carreta nao pode ser vinculada a si mesma.");
        Veiculo truck = veiculoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new AccessDeniedException("Caminhao nao encontrado para este usuario."));
        if (!CAMINHAO.equalsIgnoreCase(truck.getTipo() == null ? CAMINHAO : truck.getTipo())) {
            throw new IllegalArgumentException("A carreta deve ser vinculada a um caminhao.");
        }
        return truck;
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private VeiculoDTO toDTO(Veiculo veiculo) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setId(veiculo.getId());
        dto.setTipo(veiculo.getTipo() == null ? CAMINHAO : veiculo.getTipo());
        dto.setMarca(veiculo.getMarca());
        dto.setModelo(veiculo.getModelo());
        dto.setAno(veiculo.getAno());
        dto.setPlaca(veiculo.getPlaca());
        dto.setNome(veiculo.getNome());
        dto.setQtdPeso(veiculo.getQtdPeso());
        dto.setCaminhaoId(veiculo.getCaminhao() == null ? null : veiculo.getCaminhao().getId());
        dto.setStatus(veiculo.getStatus());
        dto.setProfissional(toProfissionalDTO(veiculo.getProfissional()));
        dto.setProprietario(toProfissionalDTO(veiculo.getProprietario()));
        return dto;
    }

    private ProfissionalDTO toProfissionalDTO(Profissional profissional) {
        if (profissional == null) return null;
        ProfissionalDTO dto = new ProfissionalDTO();
        dto.setId(profissional.getId());
        dto.setNome(profissional.getNome());
        dto.setTelefone(profissional.getTelefone());
        dto.setStatus(profissional.getStatus());
        return dto;
    }
}
