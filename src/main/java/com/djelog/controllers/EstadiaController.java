package com.djelog.controllers;

import com.djelog.dtos.EstadiaDTO;
import com.djelog.services.EstadiaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estadias")
@CrossOrigin(origins = "*")
public class EstadiaController {

    private final EstadiaService estadiaService;

    public EstadiaController(EstadiaService estadiaService) {
        this.estadiaService = estadiaService;
    }

    @GetMapping
    public ResponseEntity<List<EstadiaDTO>> findAll() {
        List<EstadiaDTO> estadias = estadiaService.findAll();
        return estadias.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(estadias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadiaDTO> findById(@PathVariable UUID id) {
        EstadiaDTO estadia = estadiaService.findById(id);
        return ResponseEntity.ok(estadia);
    }

    @GetMapping("/viagem/{viagemId}")
    public ResponseEntity<List<EstadiaDTO>> findByViagemId(@PathVariable UUID viagemId) {
        List<EstadiaDTO> estadias = estadiaService.findByViagemId(viagemId);
        return estadias.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(estadias);
    }

    @PostMapping
    public ResponseEntity<EstadiaDTO> create(@RequestBody EstadiaDTO estadiaDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        EstadiaDTO createdEstadia = estadiaService.create(estadiaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEstadia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadiaDTO> update(@PathVariable UUID id, @RequestBody EstadiaDTO estadiaDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        EstadiaDTO updatedEstadia = estadiaService.update(id, estadiaDTO);
        return ResponseEntity.ok(updatedEstadia);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        estadiaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
