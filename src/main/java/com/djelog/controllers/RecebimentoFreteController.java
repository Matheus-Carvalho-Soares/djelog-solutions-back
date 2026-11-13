package com.djelog.controllers;

import com.djelog.dtos.RecebimentoFreteDTO;
import com.djelog.services.CurrentUserService;
import com.djelog.services.RecebimentoFreteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagem/{viagemId}/recebimentos")
public class RecebimentoFreteController {
    private final RecebimentoFreteService service;
    private final CurrentUserService currentUserService;

    public RecebimentoFreteController(RecebimentoFreteService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<RecebimentoFreteDTO>> findAll(@PathVariable UUID viagemId) {
        List<RecebimentoFreteDTO> values = service.findByViagem(viagemId, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(values);
    }

    @PostMapping
    public ResponseEntity<RecebimentoFreteDTO> create(
            @PathVariable UUID viagemId, @Valid @RequestBody RecebimentoFreteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(viagemId, dto, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{recebimentoId}")
    public ResponseEntity<RecebimentoFreteDTO> update(
            @PathVariable UUID viagemId, @PathVariable UUID recebimentoId, @Valid @RequestBody RecebimentoFreteDTO dto) {
        return ResponseEntity.ok(service.update(viagemId, recebimentoId, dto, currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/{recebimentoId}")
    public ResponseEntity<Void> delete(@PathVariable UUID viagemId, @PathVariable UUID recebimentoId) {
        service.delete(viagemId, recebimentoId, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
