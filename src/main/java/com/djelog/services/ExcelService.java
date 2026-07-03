package com.djelog.services;

import com.djelog.dtos.ExcelFile;
import com.djelog.dtos.ViagemRelatorioDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ExcelService {

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter RELATORIO_POR_DATA_DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] RELATORIO_POR_DATA_HEADERS = {
            "Data Início", "Data Fim", "Status", "Profissional", "Empresa",
            "Veículo Marca", "Veículo Placa", "Origem Frete", "Destino Frete",
            "Valor Frete", "Comissão", "Total Despesas", "Lucro Líquido"
    };

    private final ViagemService viagemService;

    public ExcelService(ViagemService viagemService) {
        this.viagemService = viagemService;
    }

    public ExcelFile gerarRelatorioPorData(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null || dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Período inválido");
        }

        List<ViagemRelatorioDTO> dados = viagemService.findDadosByDataInicioFim(usuarioId, dataInicio, dataFim);
        return new ExcelFile(
                buildRelatorioPorDataFilename(dataInicio, dataFim),
                buildRelatorioPorDataWorkbook(dados)
        );
    }

    private byte[] buildRelatorioPorDataWorkbook(List<ViagemRelatorioDTO> dados) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatório");
            CellStyle relatorioPorDataMoneyStyle = createRelatorioPorDataMoneyStyle(workbook);

            writeRelatorioPorDataHeader(sheet);
            writeRelatorioPorDataRows(sheet, dados, relatorioPorDataMoneyStyle);
            autoSizeRelatorioPorDataColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar relatório Excel", e);
        }
    }

    private CellStyle createRelatorioPorDataMoneyStyle(Workbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0.00"));
        return moneyStyle;
    }

    private void writeRelatorioPorDataHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < RELATORIO_POR_DATA_HEADERS.length; i++) {
            headerRow.createCell(i).setCellValue(RELATORIO_POR_DATA_HEADERS[i]);
        }
    }

    private void writeRelatorioPorDataRows(
            Sheet sheet,
            List<ViagemRelatorioDTO> dados,
            CellStyle relatorioPorDataMoneyStyle
    ) {
        BigDecimal totalFrete = BigDecimal.ZERO;
        BigDecimal totalComissao = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        BigDecimal totalLucro = BigDecimal.ZERO;

        int rowIndex = 1;
        for (ViagemRelatorioDTO item : dados) {
            Row row = sheet.createRow(rowIndex++);
            writeRelatorioPorDataItemRow(row, item, relatorioPorDataMoneyStyle);

            totalFrete = totalFrete.add(defaultZero(item.getValorFrete()));
            totalComissao = totalComissao.add(defaultZero(item.getComissao()));
            totalDespesas = totalDespesas.add(defaultZero(item.getTotalDespesas()));
            totalLucro = totalLucro.add(defaultZero(item.getLucroLiquido()));
        }

        Row totalRow = sheet.createRow(rowIndex);
        writeRelatorioPorDataTotalRow(
                totalRow,
                totalFrete,
                totalComissao,
                totalDespesas,
                totalLucro,
                relatorioPorDataMoneyStyle
        );
    }

    private void writeRelatorioPorDataItemRow(
            Row row,
            ViagemRelatorioDTO item,
            CellStyle relatorioPorDataMoneyStyle
    ) {
        writeTextCell(row, 0, formatRelatorioPorDataDate(item.getDataInicio()));
        writeTextCell(row, 1, formatRelatorioPorDataDate(item.getDataFim()));
        writeTextCell(row, 2, item.getStatus());
        writeTextCell(row, 3, item.getProfissionalNome());
        writeTextCell(row, 4, item.getEmpresaNome());
        writeTextCell(row, 5, item.getVeiculoMarca());
        writeTextCell(row, 6, item.getVeiculoPlaca());
        writeTextCell(row, 7, item.getInicioFrete());
        writeTextCell(row, 8, item.getFimFrete());
        writeMoneyCell(row, 9, item.getValorFrete(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 10, item.getComissao(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 11, item.getTotalDespesas(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 12, item.getLucroLiquido(), relatorioPorDataMoneyStyle);
    }

    private void writeRelatorioPorDataTotalRow(
            Row totalRow,
            BigDecimal totalFrete,
            BigDecimal totalComissao,
            BigDecimal totalDespesas,
            BigDecimal totalLucro,
            CellStyle relatorioPorDataMoneyStyle
    ) {
        writeTextCell(totalRow, 8, "TOTAIS");
        writeMoneyCell(totalRow, 9, totalFrete, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 10, totalComissao, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 11, totalDespesas, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 12, totalLucro, relatorioPorDataMoneyStyle);
    }

    private void autoSizeRelatorioPorDataColumns(Sheet sheet) {
        for (int i = 0; i < RELATORIO_POR_DATA_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String buildRelatorioPorDataFilename(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return "relatorio-por-data_"
                + LocalDate.from(dataInicio).format(FILE_DATE_FORMAT)
                + "_"
                + LocalDate.from(dataFim).format(FILE_DATE_FORMAT)
                + ".xlsx";
    }

    private String formatRelatorioPorDataDate(LocalDateTime date) {
        return date == null ? "" : date.toLocalDate().format(RELATORIO_POR_DATA_DISPLAY_DATE_FORMAT);
    }

    private void writeTextCell(Row row, int column, String value) {
        row.createCell(column).setCellValue(sanitizeForExcel(value));
    }

    private void writeMoneyCell(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(defaultZero(value).doubleValue());
        cell.setCellStyle(style);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String sanitizeForExcel(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.stripLeading();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@")) {
            return "'" + value;
        }

        return value;
    }
}
