package com.djelog.controllers;

import com.djelog.dtos.VeiculoDTO;
import com.djelog.entities.Veiculo;
import com.djelog.services.CurrentUserService;
import com.djelog.services.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/veiculo")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final CurrentUserService currentUserService;

    public VeiculoController(VeiculoService veiculoService, CurrentUserService currentUserService) {
        this.veiculoService = veiculoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<VeiculoDTO>> findAll() {
        List<VeiculoDTO> veiculos = veiculoService.findAll(currentUserService.getCurrentUserId());
        return veiculos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(veiculos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoDTO> findById(@PathVariable("id") UUID id) {
        return veiculoService.findById(id, currentUserService.getCurrentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VeiculoDTO> create(@Valid @RequestBody Veiculo veiculo) {
        VeiculoDTO created = veiculoService.create(veiculo, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoDTO> update(@PathVariable("id") UUID id, @Valid @RequestBody Veiculo veiculo) {
        return veiculoService.update(id, veiculo, currentUserService.getCurrentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        veiculoService.delete(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
