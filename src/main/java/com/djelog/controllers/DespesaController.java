package com.djelog.controllers;

import com.djelog.dtos.DespesaDTO;
import com.djelog.services.DespesaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/despesas")
@CrossOrigin(origins = "*")
public class DespesaController {

    private final DespesaService despesaService;

    public DespesaController(DespesaService despesaService) {
        this.despesaService = despesaService;
    }

    @GetMapping
    public ResponseEntity<List<DespesaDTO>> findAll() {
        List<DespesaDTO> despesas = despesaService.findAll();
        return despesas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(despesas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespesaDTO> findById(@PathVariable UUID id) {
        DespesaDTO despesa = despesaService.findById(id);
        return ResponseEntity.ok(despesa);
    }

    @GetMapping("/viagem/{viagemId}")
    public ResponseEntity<List<DespesaDTO>> findByViagemId(@PathVariable UUID viagemId) {
        List<DespesaDTO> despesas = despesaService.findByViagemId(viagemId);
        return despesas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(despesas);
    }

    @PostMapping
    public ResponseEntity<DespesaDTO> create(@RequestBody DespesaDTO despesaDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        DespesaDTO createdDespesa = despesaService.create(despesaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDespesa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespesaDTO> update(@PathVariable UUID id, @RequestBody DespesaDTO despesaDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        DespesaDTO updatedDespesa = despesaService.update(id, despesaDTO);
        return ResponseEntity.ok(updatedDespesa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        despesaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
