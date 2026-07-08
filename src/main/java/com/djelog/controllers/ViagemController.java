package com.djelog.controllers;

import com.djelog.dtos.RelatorioAgrupadoDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.entities.Viagem;
import com.djelog.services.CurrentUserService;
import com.djelog.services.RelatorioService;
import com.djelog.services.ViagemService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagem")
public class ViagemController {

    private final ViagemService viagemService;
    private final RelatorioService relatorioService;
    private final CurrentUserService currentUserService;

    public ViagemController(
            ViagemService viagemService,
            RelatorioService relatorioService,
            CurrentUserService currentUserService
    ) {
        this.viagemService = viagemService;
        this.relatorioService = relatorioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ViagemDTO>> findAll() {
        List<ViagemDTO> viagens = viagemService.findAll(currentUserService.getCurrentUserId());
        return viagens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagens);
    }

    @GetMapping("/excel/dados")
    public ResponseEntity<List<ViagemRelatorioDTO>> findDadosByDataInicioFim(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        List<ViagemRelatorioDTO> dados = relatorioService.findDadosByDataInicioFim(
                currentUserService.getCurrentUserId(),
                dataInicio,
                dataFim
        );
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/excel/dados/por-veiculo")
    public ResponseEntity<List<RelatorioAgrupadoDTO>> findFaturamentoPorVeiculo(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        List<RelatorioAgrupadoDTO> dados = relatorioService.findFaturamentoPorVeiculo(
                currentUserService.getCurrentUserId(),
                dataInicio,
                dataFim
        );
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/excel/dados/por-profissional")
    public ResponseEntity<List<RelatorioAgrupadoDTO>> findFaturamentoPorProfissional(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        List<RelatorioAgrupadoDTO> dados = relatorioService.findFaturamentoPorProfissional(
                currentUserService.getCurrentUserId(),
                dataInicio,
                dataFim
        );
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemDTO> findById(@PathVariable("id") UUID id) {
        return viagemService.findById(id, currentUserService.getCurrentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ViagemDTO> create(@Valid @RequestBody Viagem viagem) {
        ViagemDTO created = viagemService.create(viagem, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViagemDTO> update(@PathVariable("id") UUID id, @Valid @RequestBody Viagem viagem) {
        return viagemService.update(id, viagem, currentUserService.getCurrentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        viagemService.delete(id, currentUserService.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
