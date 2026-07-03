package com.djelog.controllers;

import com.djelog.dtos.DespesaDTO;
import com.djelog.services.CurrentUserService;
import com.djelog.services.DespesaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {

    private final DespesaService despesaService;
    private final CurrentUserService currentUserService;

    public DespesaController(DespesaService despesaService, CurrentUserService currentUserService) {
        this.despesaService = despesaService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<DespesaDTO>> findAll() {
        List<DespesaDTO> despesas = despesaService.findAll(currentUserService.getCurrentUserId());
        return despesas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(despesas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespesaDTO> findById(@PathVariable UUID id) {
        DespesaDTO despesa = despesaService.findById(id, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(despesa);
    }

    @GetMapping("/viagem/{viagemId}")
    public ResponseEntity<List<DespesaDTO>> findByViagemId(@PathVariable UUID viagemId) {
        List<DespesaDTO> despesas = despesaService.findByViagemId(viagemId, currentUserService.getCurrentUserId());
        return despesas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(despesas);
    }

    @PostMapping
    public ResponseEntity<DespesaDTO> create(@Valid @RequestBody DespesaDTO despesaDTO) {
        DespesaDTO createdDespesa = despesaService.create(despesaDTO, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDespesa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespesaDTO> update(@PathVariable UUID id, @Valid @RequestBody DespesaDTO despesaDTO) {
        DespesaDTO updatedDespesa = despesaService.update(id, despesaDTO, currentUserService.getCurrentUserId());
        return ResponseEntity.ok(updatedDespesa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        despesaService.delete(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
