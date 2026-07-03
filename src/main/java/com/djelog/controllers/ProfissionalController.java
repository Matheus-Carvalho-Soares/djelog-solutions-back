package com.djelog.controllers;

import com.djelog.dtos.ProfissionalDTO;
import com.djelog.entities.Profissional;
import com.djelog.repositories.ProfissionalRepository;
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
@RequestMapping("/api/profissional")
public class ProfissionalController {

    private final ProfissionalRepository profissionalRepository;
    private final UsuarioService usuarioService;
    private final CurrentUserService currentUserService;

    public ProfissionalController(ProfissionalRepository profissionalRepository, UsuarioService usuarioService, CurrentUserService currentUserService) {
        this.profissionalRepository = profissionalRepository;
        this.usuarioService = usuarioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ProfissionalDTO>> findAll() {
        UUID usuarioId = currentUserService.getCurrentUserId();
        List<Profissional> profissionais = profissionalRepository.findByUsuario_Id(usuarioId);
        List<ProfissionalDTO> profissionaisDTO = profissionais.stream().map(this::convertToDTO).toList();
        return profissionaisDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(profissionaisDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> findById(@PathVariable("id") UUID id) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return profissionalRepository.findByIdAndUsuario_Id(id, usuarioId)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProfissionalDTO> create(@Valid @RequestBody Profissional profissional) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        profissional.setUsuario(usuarioService.findById(usuarioId));
        Profissional saved = profissionalRepository.save(profissional);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> update(@PathVariable("id") UUID id, @Valid @RequestBody Profissional profissional) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return profissionalRepository.findByIdAndUsuario_Id(id, usuarioId)
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
        UUID usuarioId = currentUserService.getCurrentUserId();
        Profissional profissional = profissionalRepository.findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        profissionalRepository.delete(profissional);
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
