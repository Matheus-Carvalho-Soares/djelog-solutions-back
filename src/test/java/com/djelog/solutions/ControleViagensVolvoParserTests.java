package com.djelog.solutions;

import com.djelog.services.ControleViagensVolvoParser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ControleViagensVolvoParserTests {
    private final ControleViagensVolvoParser parser = new ControleViagensVolvoParser();

    @Test
    void leAsDezesseteLinhasDoPerfilSemAvaliarFormulasEDistingueValoresAusentes() throws Exception {
        MockMultipartFile file = workbook();

        List<ControleViagensVolvoParser.ParsedRow> rows = parser.parse(file);

        assertThat(rows).hasSize(17);
        assertThat(rows.stream().filter(row -> row.date().equals(LocalDate.of(2026, 9, 12))
                && row.origin().equals("Santos") && row.destination().equals("Santos")
                && row.freight().compareTo(new BigDecimal("400.00")) == 0)).hasSize(10);
        assertThat(rows.getFirst().commission()).isEqualByComparingTo("15.00");
        assertThat(rows.get(10).toll()).isNull();
        assertThat(rows.get(10).warnings()).anyMatch(message -> message.contains("informação ausente, não como zero"));
        assertThat(rows.get(11).fuel()).isEqualByComparingTo("3826.77");
        assertThat(rows.get(12).toll()).isEqualByComparingTo("316.20");
        assertThat(rows.get(13).freight()).isEqualByComparingTo("2262.20");
        assertThat(rows.get(10).sourceRow()).isEqualTo(15);
    }

    private MockMultipartFile workbook() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet cadastro = workbook.createSheet("Cadastro");
            cadastro.createRow(3).createCell(0).setCellValue("Comissão");
            Cell commission = cadastro.getRow(3).createCell(1);
            commission.setCellValue(0.15);
            commission.setCellStyle(workbook.createCellStyle());
            commission.getCellStyle().setDataFormat(workbook.createDataFormat().getFormat("0%"));

            Sheet sheet = workbook.createSheet("Lançamentos");
            String[] headers = {"Data", "Placa Cavalo", "Transportadora", "Destino 1", "Destino 2", "Destino 3",
                    "Preço Frete", "Abastecimento", "Pedágio", "Outras Despesas", "Descrição Outras",
                    "Modelo", "Placa Carreta", "Viagem", "Comissão", "Total Despesas", "Resultado"};
            Row header = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
            for (int i = 0; i < 17; i++) {
                Row row = sheet.createRow(4 + i);
                Cell date = row.createCell(0);
                date.setCellValue(java.sql.Date.valueOf(i < 10 ? LocalDate.of(2026, 9, 12) : LocalDate.of(2026, 9, 10 + (i % 8))));
                date.setCellStyle(dateStyle);
                row.createCell(1).setCellValue("JOZ9C78");
                row.createCell(2).setCellValue(i == 16 ? "" : "MultiLog");
                row.createCell(3).setCellValue(i < 10 ? "Santos" : "Origem " + i);
                row.createCell(4).setCellValue("");
                row.createCell(5).setCellValue(i < 10 ? "Santos" : "Destino " + i);
                row.createCell(6).setCellValue(i < 10 ? 400.00 : 2262.20 + i);
                row.createCell(7).setCellValue(100.50);
                row.createCell(8).setCellValue(25.20);
                row.createCell(9).setCellValue(0.00);
                row.createCell(10).setCellValue("");
            }
            sheet.getRow(4 + 10).getCell(8).setCellValue("//");
            sheet.getRow(4 + 11).getCell(7).setCellValue(3826.77);
            sheet.getRow(4 + 12).getCell(8).setCellValue(316.20);
            sheet.getRow(4 + 13).getCell(6).setCellValue(2262.20);
            Row calculatedOnly = sheet.createRow(21);
            calculatedOnly.createCell(15).setCellFormula("1/0");
            calculatedOnly.createCell(16).setCellFormula("1/0");
            Sheet summary = workbook.createSheet("Resumo Semanal");
            summary.createRow(0).createCell(0).setCellFormula("1/0");
            workbook.write(output);
            return new MockMultipartFile("file", "Controle Viagens Volvo.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }
}
