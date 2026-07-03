package com.djelog.controllers;

import com.djelog.dtos.ViagemComissionadaDTO;
import com.djelog.services.CurrentUserService;
import com.djelog.services.ViagemComissionadaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagem-comissionada")
public class ViagemComissionadaController {

    private final ViagemComissionadaService viagemComissionadaService;
    private final CurrentUserService currentUserService;

    public ViagemComissionadaController(ViagemComissionadaService viagemComissionadaService, CurrentUserService currentUserService) {
        this.viagemComissionadaService = viagemComissionadaService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<ViagemComissionadaDTO>> findAll() {
        List<ViagemComissionadaDTO> viagens = viagemComissionadaService.findAll(currentUserService.getCurrentUserId());
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemComissionadaDTO> findById(@PathVariable UUID id) {
        ViagemComissionadaDTO viagem = viagemComissionadaService.findById(id, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(viagem);
    }

    @GetMapping("/search/inicio-frete")
    public ResponseEntity<List<ViagemComissionadaDTO>> findByInicioFrete(@RequestParam String inicioFrete) {
        List<ViagemComissionadaDTO> viagens = viagemComissionadaService.findByInicioFrete(inicioFrete, currentUserService.getCurrentUserId());
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @GetMapping("/search/fim-frete")
    public ResponseEntity<List<ViagemComissionadaDTO>> findByFimFrete(@RequestParam String fimFrete) {
        List<ViagemComissionadaDTO> viagens = viagemComissionadaService.findByFimFrete(fimFrete, currentUserService.getCurrentUserId());
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @PostMapping
    public ResponseEntity<ViagemComissionadaDTO> create(@Valid @RequestBody ViagemComissionadaDTO dto) {
        ViagemComissionadaDTO created = viagemComissionadaService.create(dto, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViagemComissionadaDTO> update(@PathVariable UUID id, @Valid @RequestBody ViagemComissionadaDTO dto) {
        ViagemComissionadaDTO updated = viagemComissionadaService.update(id, dto, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        viagemComissionadaService.delete(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
