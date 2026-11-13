package com.djelog.controllers;

import com.djelog.dtos.ControleFinanceiroMotoristaRequest;
import com.djelog.dtos.MotoristaFinanceiroDTO;
import com.djelog.dtos.RegistrarPagamentoMotoristaRequest;
import com.djelog.services.CurrentUserService;
import com.djelog.services.ProfissionalFinanceiroService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profissional/{profissionalId}/financeiro")
public class ProfissionalFinanceiroController {
    private final ProfissionalFinanceiroService financeiroService;
    private final CurrentUserService currentUserService;

    public ProfissionalFinanceiroController(ProfissionalFinanceiroService financeiroService,
            CurrentUserService currentUserService) {
        this.financeiroService = financeiroService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<MotoristaFinanceiroDTO> consultar(@PathVariable UUID profissionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID veiculoId) {
        return ResponseEntity.ok(financeiroService.consultar(profissionalId,
                currentUserService.getCurrentUserId(), inicio, fim, veiculoId));
    }

    @PostMapping("/controle-inicial")
    public ResponseEntity<Void> inicializar(@PathVariable UUID profissionalId,
            @Valid @RequestBody ControleFinanceiroMotoristaRequest request) {
        financeiroService.inicializar(profissionalId, currentUserService.getCurrentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<Map<String, UUID>> registrarPagamento(@PathVariable UUID profissionalId,
            @Valid @RequestBody RegistrarPagamentoMotoristaRequest request) {
        UUID pagamentoId = financeiroService.registrarPagamento(profissionalId,
                currentUserService.getCurrentUserId(), request);
        return ResponseEntity.ok(Map.of("id", pagamentoId));
    }
}
