package com.djelog.controllers;

import com.djelog.dtos.ProfissionalDTO;
import com.djelog.entities.Profissional;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profissional")
@CrossOrigin(origins = "*")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final UsuarioService usuarioService;

    public ProfissionalController(ProfissionalRepository profissionalRepository, UsuarioService usuarioService) {
        this.profissionalRepository = profissionalRepository;
        this.usuarioService = usuarioService;
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
    public ResponseEntity<List<ProfissionalDTO>> findAll(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID usuarioId = getAuthenticatedUserId(authentication);
        List<Profissional> profissionais = profissionalRepository.findByUsuario_Id(usuarioId);
        List<ProfissionalDTO> profissionaisDTO = profissionais.stream().map(this::convertToDTO).toList();
        return profissionaisDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(profissionaisDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> findById(@PathVariable("id") UUID id) {
        return profissionalRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProfissionalDTO> create(@RequestBody Profissional profissional, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        profissional.setUsuario(usuarioService.findById(usuarioId));
        Profissional saved = profissionalRepository.save(profissional);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> update(@PathVariable("id") UUID id, @RequestBody Profissional profissional, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return profissionalRepository.findById(id)
                .map(existing -> {
                    existing.setNome(profissional.getNome());
                    existing.setTelefone(profissional.getTelefone());
                    existing.setUsuario(usuarioService.findById(usuarioId));
                    existing.setStatus(profissional.getStatus());
                    Profissional saved = profissionalRepository.save(existing);
                    return ResponseEntity.ok(convertToDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (!profissionalRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        profissionalRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ProfissionalDTO convertToDTO(Profissional profissional) {
        ProfissionalDTO dto = new ProfissionalDTO();
        dto.setId(profissional.getId());
        dto.setNome(profissional.getNome());
        dto.setTelefone(profissional.getTelefone());
        dto.setStatus(profissional.getStatus());
        return dto;
    }
}
