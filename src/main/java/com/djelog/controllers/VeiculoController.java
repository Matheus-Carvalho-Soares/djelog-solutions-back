package com.djelog.controllers;

import com.djelog.dtos.VeiculoDTO;
import com.djelog.dtos.ProfissionalDTO;
import com.djelog.entities.Veiculo;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/veiculo")
@CrossOrigin(origins = "*")
public class VeiculoController {

    private final VeiculoRepository veiculoRepository;
    private final UsuarioService usuarioService;
    private final ProfissionalRepository profissionalRepository;

    public VeiculoController(VeiculoRepository veiculoRepository, UsuarioService usuarioService, ProfissionalRepository profissionalRepository) {
        this.veiculoRepository = veiculoRepository;
        this.usuarioService = usuarioService;
        this.profissionalRepository = profissionalRepository;
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return null;
        }
        return usuarioService.findByEmail(email).getId();
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<VeiculoDTO>> findAll(Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Veiculo> veiculos = veiculoRepository.findByProfissional_Usuario_Id(usuarioId);
        List<VeiculoDTO> veiculosDTO = veiculos.stream().map(this::convertToDTO).toList();
        return veiculosDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(veiculosDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoDTO> findById(@PathVariable("id") UUID id) {
        return veiculoRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VeiculoDTO> create(@RequestBody Veiculo veiculo, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
    public ResponseEntity<VeiculoDTO> update(@PathVariable("id") UUID id, @RequestBody Veiculo veiculo, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (veiculo.getProfissional() == null || veiculo.getProfissional().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!profissionalRepository.existsByIdAndUsuario_Id(veiculo.getProfissional().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return veiculoRepository.findById(id)
                .map(existing -> {
                    existing.setMarca(veiculo.getMarca());
                    existing.setAno(veiculo.getAno());
                    existing.setProfissional(veiculo.getProfissional());
                    existing.setPlaca(veiculo.getPlaca());
                    existing.setStatus(veiculo.getStatus());
                    Veiculo saved = veiculoRepository.save(existing);
                    return ResponseEntity.ok(convertToDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (!veiculoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        veiculoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private VeiculoDTO convertToDTO(Veiculo veiculo) {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setId(veiculo.getId());
        dto.setMarca(veiculo.getMarca());
        dto.setAno(veiculo.getAno());
        dto.setPlaca(veiculo.getPlaca());
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
