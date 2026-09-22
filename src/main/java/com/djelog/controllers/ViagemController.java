package com.djelog.controllers;

import com.djelog.dtos.RelatorioAgrupadoDTO;
import com.djelog.dtos.RelatorioDetalhePageDTO;
import com.djelog.dtos.RelatorioFiltro;
import com.djelog.dtos.ConfirmacaoImportacaoDTO;
import com.djelog.dtos.ImportacaoViagemDTO;
import com.djelog.dtos.ResultadoImportacaoDTO;
import com.djelog.dtos.ViagemDTO;
import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.entities.Viagem;
import com.djelog.services.CurrentUserService;
import com.djelog.services.RelatorioService;
import com.djelog.services.ViagemService;
import com.djelog.services.ImportacaoViagemService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
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
    private final ImportacaoViagemService importacaoViagemService;

    public ViagemController(
            ViagemService viagemService,
            RelatorioService relatorioService,
            CurrentUserService currentUserService,
            ImportacaoViagemService importacaoViagemService
    ) {
        this.viagemService = viagemService;
        this.relatorioService = relatorioService;
        this.currentUserService = currentUserService;
        this.importacaoViagemService = importacaoViagemService;
    }

    @PostMapping(value = "/importacoes/preview", consumes = "multipart/form-data")
    public ResponseEntity<ImportacaoViagemDTO> previewImportacao(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(importacaoViagemService.preview(file, currentUserService.getCurrentUserId()));
    }

    @GetMapping("/importacoes/{id}")
    public ResponseEntity<ImportacaoViagemDTO> getImportacao(@PathVariable UUID id) {
        return ResponseEntity.ok(importacaoViagemService.get(id, currentUserService.getCurrentUserId()));
    }

    @PostMapping("/importacoes/{id}/confirmar")
    public ResponseEntity<ResultadoImportacaoDTO> confirmarImportacao(
            @PathVariable UUID id, @RequestBody ConfirmacaoImportacaoDTO request) {
        return ResponseEntity.ok(importacaoViagemService.confirm(id, currentUserService.getCurrentUserId(), request));
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
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(value = "veiculoIds", required = false) List<UUID> veiculoIds,
            @RequestParam(value = "profissionalIds", required = false) List<UUID> profissionalIds,
            @RequestParam(value = "empresaIds", required = false) List<UUID> empresaIds,
            @RequestParam(value = "status", required = false) List<String> status,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            @RequestParam(value = "busca", required = false) String busca
    ) {
        RelatorioFiltro filtro = new RelatorioFiltro(
                dataInicio, dataFim, veiculoIds, profissionalIds, empresaIds, status, sortBy, sortDirection, busca
        );
        List<RelatorioAgrupadoDTO> dados = relatorioService.findFaturamentoPorVeiculo(
                currentUserService.getCurrentUserId(),
                filtro
        );
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/excel/dados/por-profissional")
    public ResponseEntity<List<RelatorioAgrupadoDTO>> findFaturamentoPorProfissional(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(value = "veiculoIds", required = false) List<UUID> veiculoIds,
            @RequestParam(value = "profissionalIds", required = false) List<UUID> profissionalIds,
            @RequestParam(value = "empresaIds", required = false) List<UUID> empresaIds,
            @RequestParam(value = "status", required = false) List<String> status,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            @RequestParam(value = "busca", required = false) String busca
    ) {
        RelatorioFiltro filtro = new RelatorioFiltro(
                dataInicio, dataFim, veiculoIds, profissionalIds, empresaIds, status, sortBy, sortDirection, busca
        );
        List<RelatorioAgrupadoDTO> dados = relatorioService.findFaturamentoPorProfissional(
                currentUserService.getCurrentUserId(),
                filtro
        );
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/excel/dados/por-veiculo/{veiculoId}/viagens")
    public ResponseEntity<RelatorioDetalhePageDTO> findDetalhesPorVeiculo(
            @PathVariable UUID veiculoId,
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(value = "profissionalIds", required = false) List<UUID> profissionalIds,
            @RequestParam(value = "empresaIds", required = false) List<UUID> empresaIds,
            @RequestParam(value = "status", required = false) List<String> status,
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size
    ) {
        RelatorioFiltro filtro = new RelatorioFiltro(
                dataInicio, dataFim, List.of(veiculoId), profissionalIds, empresaIds, status, "grupoNome", "asc", busca
        );
        RelatorioDetalhePageDTO detalhes = relatorioService.findDetalhesPorVeiculo(
                currentUserService.getCurrentUserId(), veiculoId, filtro, page, size
        );
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/excel/dados/por-profissional/{profissionalId}/viagens")
    public ResponseEntity<RelatorioDetalhePageDTO> findDetalhesPorProfissional(
            @PathVariable UUID profissionalId,
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(value = "veiculoIds", required = false) List<UUID> veiculoIds,
            @RequestParam(value = "empresaIds", required = false) List<UUID> empresaIds,
            @RequestParam(value = "status", required = false) List<String> status,
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size
    ) {
        RelatorioFiltro filtro = new RelatorioFiltro(
                dataInicio, dataFim, veiculoIds, List.of(profissionalId), empresaIds, status, "grupoNome", "asc", busca
        );
        RelatorioDetalhePageDTO detalhes = relatorioService.findDetalhesPorProfissional(
                currentUserService.getCurrentUserId(), profissionalId, filtro, page, size
        );
        return ResponseEntity.ok(detalhes);
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
