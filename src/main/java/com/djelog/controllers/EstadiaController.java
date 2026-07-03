package com.djelog.controllers;

import com.djelog.dtos.EstadiaDTO;
import com.djelog.services.CurrentUserService;
import com.djelog.services.EstadiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estadias")
public class EstadiaController {

    private final EstadiaService estadiaService;
    private final CurrentUserService currentUserService;

    public EstadiaController(EstadiaService estadiaService, CurrentUserService currentUserService) {
        this.estadiaService = estadiaService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<EstadiaDTO>> findAll() {
        List<EstadiaDTO> estadias = estadiaService.findAll(currentUserService.getCurrentUserId());
        return estadias.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(estadias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadiaDTO> findById(@PathVariable UUID id) {
        EstadiaDTO estadia = estadiaService.findById(id, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(estadia);
    }

    @GetMapping("/viagem/{viagemId}")
    public ResponseEntity<List<EstadiaDTO>> findByViagemId(@PathVariable UUID viagemId) {
        List<EstadiaDTO> estadias = estadiaService.findByViagemId(viagemId, currentUserService.getCurrentUserId());
        return estadias.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(estadias);
    }

    @PostMapping
    public ResponseEntity<EstadiaDTO> create(@Valid @RequestBody EstadiaDTO estadiaDTO) {
        EstadiaDTO createdEstadia = estadiaService.create(estadiaDTO, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEstadia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadiaDTO> update(@PathVariable UUID id, @Valid @RequestBody EstadiaDTO estadiaDTO) {
        EstadiaDTO updatedEstadia = estadiaService.update(id, estadiaDTO, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(updatedEstadia);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        estadiaService.delete(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
