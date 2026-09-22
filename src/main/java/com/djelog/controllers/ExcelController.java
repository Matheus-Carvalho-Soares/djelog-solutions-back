package com.djelog.controllers;

import com.djelog.dtos.ExcelFile;
import com.djelog.dtos.RelatorioFiltro;
import com.djelog.services.CurrentUserService;
import com.djelog.services.ExcelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final ExcelService excelService;
    private final CurrentUserService currentUserService;

    public ExcelController(ExcelService excelService, CurrentUserService currentUserService) {
        this.excelService = excelService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/relatorio-por-data")
    public ResponseEntity<byte[]> exportRelatorioPorData(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        ExcelFile excelFile = excelService.gerarRelatorioPorData(usuarioId, dataInicio, dataFim);

        return ResponseEntity.ok()
                .headers(buildDownloadHeaders(excelFile.filename()))
                .body(excelFile.content());
    }

    @GetMapping("/relatorio-por-veiculo")
    public ResponseEntity<byte[]> exportRelatorioPorVeiculo(
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
        UUID usuarioId = currentUserService.getCurrentUserId();
        ExcelFile excelFile = excelService.gerarRelatorioPorVeiculo(usuarioId, new RelatorioFiltro(
                dataInicio, dataFim, veiculoIds, profissionalIds, empresaIds, status, sortBy, sortDirection, busca
        ));

        return ResponseEntity.ok()
                .headers(buildDownloadHeaders(excelFile.filename()))
                .body(excelFile.content());
    }

    @GetMapping("/relatorio-por-profissional")
    public ResponseEntity<byte[]> exportRelatorioPorProfissional(
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
        UUID usuarioId = currentUserService.getCurrentUserId();
        ExcelFile excelFile = excelService.gerarRelatorioPorProfissional(usuarioId, new RelatorioFiltro(
                dataInicio, dataFim, veiculoIds, profissionalIds, empresaIds, status, sortBy, sortDirection, busca
        ));

        return ResponseEntity.ok()
                .headers(buildDownloadHeaders(excelFile.filename()))
                .body(excelFile.content());
    }

    private HttpHeaders buildDownloadHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(EXCEL_MEDIA_TYPE);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setCacheControl("no-store");
        return headers;
    }
}
