package com.djelog.controllers;

import com.djelog.dtos.VeiculoDTO;
import com.djelog.dtos.ProfissionalDTO;
import com.djelog.entities.Veiculo;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.services.CurrentUserService;
import com.djelog.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/veiculo")
public class VeiculoController {

    private final VeiculoRepository veiculoRepository;
    private final UsuarioService usuarioService;
    private final ProfissionalRepository profissionalRepository;
    private final CurrentUserService currentUserService;

    public VeiculoController(VeiculoRepository veiculoRepository, UsuarioService usuarioService, ProfissionalRepository profissionalRepository, CurrentUserService currentUserService) {
        this.veiculoRepository = veiculoRepository;
        this.usuarioService = usuarioService;
        this.profissionalRepository = profissionalRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<VeiculoDTO>> findAll() {
        UUID usuarioId = currentUserService.getCurrentUserId();
        List<Veiculo> veiculos = veiculoRepository.findByProfissional_Usuario_Id(usuarioId);
        List<VeiculoDTO> veiculosDTO = veiculos.stream().map(this::convertToDTO).toList();
        return veiculosDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(veiculosDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoDTO> findById(@PathVariable("id") UUID id) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return veiculoRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VeiculoDTO> create(@Valid @RequestBody Veiculo veiculo) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        if (veiculo.getProfissional() == null || veiculo.getProfissional().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!profissionalRepository.existsByIdAndUsuario_Id(veiculo.getProfissional().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Veiculo saved = veiculoRepository.save(veiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoDTO> update(@PathVariable("id") UUID id, @Valid @RequestBody Veiculo veiculo) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        if (veiculo.getProfissional() == null || veiculo.getProfissional().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!profissionalRepository.existsByIdAndUsuario_Id(veiculo.getProfissional().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return veiculoRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(existing -> {
                    existing.setMarca(veiculo.getMarca());
                    existing.setAno(veiculo.getAno());
                    existing.setProfissional(veiculo.getProfissional());
                    existing.setPlaca(veiculo.getPlaca());
                    existing.setNome(veiculo.getNome());
                    existing.setQtdPeso(veiculo.getQtdPeso());
                    existing.setStatus(veiculo.getStatus());
                    Veiculo saved = veiculoRepository.save(existing);
                    return ResponseEntity.ok(convertToDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        Veiculo veiculo = veiculoRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        veiculoRepository.delete(veiculo);
        return ResponseEntity.noContent().build();
    }

    private VeiculoDTO convertToDTO(Veiculo veiculo) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setId(veiculo.getId());
        dto.setMarca(veiculo.getMarca());
        dto.setAno(veiculo.getAno());
        dto.setPlaca(veiculo.getPlaca());
        dto.setNome(veiculo.getNome());
        dto.setQtdPeso(veiculo.getQtdPeso());
        dto.setStatus(veiculo.getStatus());
        
        if (veiculo.getProfissional() != null) {
            ProfissionalDTO profissionalDTO = new ProfissionalDTO();
            profissionalDTO.setId(veiculo.getProfissional().getId());
            profissionalDTO.setNome(veiculo.getProfissional().getNome());
            profissionalDTO.setTelefone(veiculo.getProfissional().getTelefone());
            profissionalDTO.setStatus(veiculo.getProfissional().getStatus());
            dto.setProfissional(profissionalDTO);
        }
        
        return dto;
    }
}
