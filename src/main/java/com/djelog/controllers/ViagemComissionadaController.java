package com.djelog.controllers;

import com.djelog.dtos.ViagemComissionadaDTO;
import com.djelog.services.ViagemComissionadaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagem-comissionada")
@CrossOrigin(origins = "*")
public class ViagemComissionadaController {

    private final ViagemComissionadaService viagemComissionadaService;

    public ViagemComissionadaController(ViagemComissionadaService viagemComissionadaService) {
        this.viagemComissionadaService = viagemComissionadaService;
    }

    @GetMapping
    public ResponseEntity<List<ViagemComissionadaDTO>> findAll() {
        List<ViagemComissionadaDTO> viagens = viagemComissionadaService.findAll();
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemComissionadaDTO> findById(@PathVariable UUID id) {
        ViagemComissionadaDTO viagem = viagemComissionadaService.findById(id);
        return ResponseEntity.ok(viagem);
    }

    @GetMapping("/search/inicio-frete")
    public ResponseEntity<List<ViagemComissionadaDTO>> findByInicioFrete(@RequestParam String inicioFrete) {
        List<ViagemComissionadaDTO> viagens = viagemComissionadaService.findByInicioFrete(inicioFrete);
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @GetMapping("/search/fim-frete")
    public ResponseEntity<List<ViagemComissionadaDTO>> findByFimFrete(@RequestParam String fimFrete) {
        List<ViagemComissionadaDTO> viagens = viagemComissionadaService.findByFimFrete(fimFrete);
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @PostMapping
    public ResponseEntity<ViagemComissionadaDTO> create(@RequestBody ViagemComissionadaDTO dto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        ViagemComissionadaDTO created = viagemComissionadaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViagemComissionadaDTO> update(@PathVariable UUID id, @RequestBody ViagemComissionadaDTO dto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        ViagemComissionadaDTO updated = viagemComissionadaService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        viagemComissionadaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
