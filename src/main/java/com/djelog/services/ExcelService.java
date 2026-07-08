package com.djelog.services;

import com.djelog.dtos.ExcelFile;
import com.djelog.dtos.RelatorioAgrupadoDTO;
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
            "Data Inicio", "Data Fim", "Status", "Profissional", "Empresa",
            "Veiculo Marca", "Veiculo Placa", "Origem Frete", "Destino Frete",
            "Valor Frete", "Estadias", "Receita Total", "Comissao", "Total Despesas", "Lucro Liquido"
    };
    private static final String[] RELATORIO_POR_VEICULO_HEADERS = {
            "Veiculo", "Detalhe", "Qtd Viagens", "Valor Frete", "Estadias",
            "Receita Total", "Comissao", "Total Despesas", "Lucro Liquido"
    };
    private static final String[] RELATORIO_POR_PROFISSIONAL_HEADERS = {
            "Profissional", "Detalhe", "Qtd Viagens", "Valor Frete", "Estadias",
            "Receita Total", "Comissao", "Total Despesas", "Lucro Liquido"
    };

    private final RelatorioService relatorioService;

    public ExcelService(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    public ExcelFile gerarRelatorioPorData(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<ViagemRelatorioDTO> dados = relatorioService.findDadosByDataInicioFim(usuarioId, dataInicio, dataFim);
        return new ExcelFile(
                buildRelatorioPorDataFilename(dataInicio, dataFim),
                buildRelatorioPorDataWorkbook(dados)
        );
    }

    public ExcelFile gerarRelatorioPorVeiculo(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<RelatorioAgrupadoDTO> dados = relatorioService.findFaturamentoPorVeiculo(usuarioId, dataInicio, dataFim);
        return new ExcelFile(
                buildRelatorioPorVeiculoFilename(dataInicio, dataFim),
                buildRelatorioPorVeiculoWorkbook(dados)
        );
    }

    public ExcelFile gerarRelatorioPorProfissional(UUID usuarioId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<RelatorioAgrupadoDTO> dados = relatorioService.findFaturamentoPorProfissional(usuarioId, dataInicio, dataFim);
        return new ExcelFile(
                buildRelatorioPorProfissionalFilename(dataInicio, dataFim),
                buildRelatorioPorProfissionalWorkbook(dados)
        );
    }

    private byte[] buildRelatorioPorDataWorkbook(List<ViagemRelatorioDTO> dados) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatorio");
            CellStyle relatorioPorDataMoneyStyle = createMoneyStyle(workbook);

            writeRelatorioPorDataHeader(sheet);
            writeRelatorioPorDataRows(sheet, dados, relatorioPorDataMoneyStyle);
            autoSizeRelatorioPorDataColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar relatorio Excel", e);
        }
    }

    private byte[] buildRelatorioPorVeiculoWorkbook(List<RelatorioAgrupadoDTO> dados) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Por Veiculo");
            CellStyle relatorioPorVeiculoMoneyStyle = createMoneyStyle(workbook);

            writeRelatorioPorVeiculoHeader(sheet);
            writeRelatorioPorVeiculoRows(sheet, dados, relatorioPorVeiculoMoneyStyle);
            autoSizeRelatorioPorVeiculoColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar relatorio Excel", e);
        }
    }

    private byte[] buildRelatorioPorProfissionalWorkbook(List<RelatorioAgrupadoDTO> dados) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Por Profissional");
            CellStyle relatorioPorProfissionalMoneyStyle = createMoneyStyle(workbook);

            writeRelatorioPorProfissionalHeader(sheet);
            writeRelatorioPorProfissionalRows(sheet, dados, relatorioPorProfissionalMoneyStyle);
            autoSizeRelatorioPorProfissionalColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar relatorio Excel", e);
        }
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0.00"));
        return moneyStyle;
    }

    private void writeRelatorioPorDataHeader(Sheet sheet) {
        writeHeader(sheet, RELATORIO_POR_DATA_HEADERS);
    }

    private void writeRelatorioPorVeiculoHeader(Sheet sheet) {
        writeHeader(sheet, RELATORIO_POR_VEICULO_HEADERS);
    }

    private void writeRelatorioPorProfissionalHeader(Sheet sheet) {
        writeHeader(sheet, RELATORIO_POR_PROFISSIONAL_HEADERS);
    }

    private void writeHeader(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    private void writeRelatorioPorDataRows(
            Sheet sheet,
            List<ViagemRelatorioDTO> dados,
            CellStyle relatorioPorDataMoneyStyle
    ) {
        BigDecimal totalFrete = BigDecimal.ZERO;
        BigDecimal totalEstadias = BigDecimal.ZERO;
        BigDecimal totalReceita = BigDecimal.ZERO;
        BigDecimal totalComissao = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        BigDecimal totalLucro = BigDecimal.ZERO;

        int rowIndex = 1;
        for (ViagemRelatorioDTO item : dados) {
            Row row = sheet.createRow(rowIndex++);
            writeRelatorioPorDataItemRow(row, item, relatorioPorDataMoneyStyle);

            totalFrete = totalFrete.add(defaultZero(item.getValorFrete()));
            totalEstadias = totalEstadias.add(defaultZero(item.getTotalEstadias()));
            totalReceita = totalReceita.add(defaultZero(item.getReceitaTotal()));
            totalComissao = totalComissao.add(defaultZero(item.getComissao()));
            totalDespesas = totalDespesas.add(defaultZero(item.getTotalDespesas()));
            totalLucro = totalLucro.add(defaultZero(item.getLucroLiquido()));
        }

        Row totalRow = sheet.createRow(rowIndex);
        writeRelatorioPorDataTotalRow(
                totalRow,
                totalFrete,
                totalEstadias,
                totalReceita,
                totalComissao,
                totalDespesas,
                totalLucro,
                relatorioPorDataMoneyStyle
        );
    }

    private void writeRelatorioPorVeiculoRows(
            Sheet sheet,
            List<RelatorioAgrupadoDTO> dados,
            CellStyle relatorioPorVeiculoMoneyStyle
    ) {
        writeRelatorioAgrupadoRows(sheet, dados, relatorioPorVeiculoMoneyStyle);
    }

    private void writeRelatorioPorProfissionalRows(
            Sheet sheet,
            List<RelatorioAgrupadoDTO> dados,
            CellStyle relatorioPorProfissionalMoneyStyle
    ) {
        writeRelatorioAgrupadoRows(sheet, dados, relatorioPorProfissionalMoneyStyle);
    }

    private void writeRelatorioAgrupadoRows(
            Sheet sheet,
            List<RelatorioAgrupadoDTO> dados,
            CellStyle moneyStyle
    ) {
        int totalQuantidade = 0;
        BigDecimal totalFrete = BigDecimal.ZERO;
        BigDecimal totalEstadias = BigDecimal.ZERO;
        BigDecimal totalReceita = BigDecimal.ZERO;
        BigDecimal totalComissao = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        BigDecimal totalLucro = BigDecimal.ZERO;

        int rowIndex = 1;
        for (RelatorioAgrupadoDTO item : dados) {
            Row row = sheet.createRow(rowIndex++);
            writeRelatorioAgrupadoItemRow(row, item, moneyStyle);

            totalQuantidade += item.getQuantidadeViagens() == null ? 0 : item.getQuantidadeViagens();
            totalFrete = totalFrete.add(defaultZero(item.getValorFrete()));
            totalEstadias = totalEstadias.add(defaultZero(item.getTotalEstadias()));
            totalReceita = totalReceita.add(defaultZero(item.getReceitaTotal()));
            totalComissao = totalComissao.add(defaultZero(item.getComissao()));
            totalDespesas = totalDespesas.add(defaultZero(item.getTotalDespesas()));
            totalLucro = totalLucro.add(defaultZero(item.getLucroLiquido()));
        }

        Row totalRow = sheet.createRow(rowIndex);
        writeRelatorioAgrupadoTotalRow(
                totalRow,
                totalQuantidade,
                totalFrete,
                totalEstadias,
                totalReceita,
                totalComissao,
                totalDespesas,
                totalLucro,
                moneyStyle
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
        writeMoneyCell(row, 10, item.getTotalEstadias(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 11, item.getReceitaTotal(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 12, item.getComissao(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 13, item.getTotalDespesas(), relatorioPorDataMoneyStyle);
        writeMoneyCell(row, 14, item.getLucroLiquido(), relatorioPorDataMoneyStyle);
    }

    private void writeRelatorioAgrupadoItemRow(
            Row row,
            RelatorioAgrupadoDTO item,
            CellStyle moneyStyle
    ) {
        writeTextCell(row, 0, item.getGrupoNome());
        writeTextCell(row, 1, item.getGrupoDetalhe());
        writeIntegerCell(row, 2, item.getQuantidadeViagens());
        writeMoneyCell(row, 3, item.getValorFrete(), moneyStyle);
        writeMoneyCell(row, 4, item.getTotalEstadias(), moneyStyle);
        writeMoneyCell(row, 5, item.getReceitaTotal(), moneyStyle);
        writeMoneyCell(row, 6, item.getComissao(), moneyStyle);
        writeMoneyCell(row, 7, item.getTotalDespesas(), moneyStyle);
        writeMoneyCell(row, 8, item.getLucroLiquido(), moneyStyle);
    }

    private void writeRelatorioPorDataTotalRow(
            Row totalRow,
            BigDecimal totalFrete,
            BigDecimal totalEstadias,
            BigDecimal totalReceita,
            BigDecimal totalComissao,
            BigDecimal totalDespesas,
            BigDecimal totalLucro,
            CellStyle relatorioPorDataMoneyStyle
    ) {
        writeTextCell(totalRow, 8, "TOTAIS");
        writeMoneyCell(totalRow, 9, totalFrete, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 10, totalEstadias, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 11, totalReceita, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 12, totalComissao, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 13, totalDespesas, relatorioPorDataMoneyStyle);
        writeMoneyCell(totalRow, 14, totalLucro, relatorioPorDataMoneyStyle);
    }

    private void writeRelatorioAgrupadoTotalRow(
            Row totalRow,
            int totalQuantidade,
            BigDecimal totalFrete,
            BigDecimal totalEstadias,
            BigDecimal totalReceita,
            BigDecimal totalComissao,
            BigDecimal totalDespesas,
            BigDecimal totalLucro,
            CellStyle moneyStyle
    ) {
        writeTextCell(totalRow, 1, "TOTAIS");
        writeIntegerCell(totalRow, 2, totalQuantidade);
        writeMoneyCell(totalRow, 3, totalFrete, moneyStyle);
        writeMoneyCell(totalRow, 4, totalEstadias, moneyStyle);
        writeMoneyCell(totalRow, 5, totalReceita, moneyStyle);
        writeMoneyCell(totalRow, 6, totalComissao, moneyStyle);
        writeMoneyCell(totalRow, 7, totalDespesas, moneyStyle);
        writeMoneyCell(totalRow, 8, totalLucro, moneyStyle);
    }

    private void autoSizeRelatorioPorDataColumns(Sheet sheet) {
        autoSizeColumns(sheet, RELATORIO_POR_DATA_HEADERS.length);
    }

    private void autoSizeRelatorioPorVeiculoColumns(Sheet sheet) {
        autoSizeColumns(sheet, RELATORIO_POR_VEICULO_HEADERS.length);
    }

    private void autoSizeRelatorioPorProfissionalColumns(Sheet sheet) {
        autoSizeColumns(sheet, RELATORIO_POR_PROFISSIONAL_HEADERS.length);
    }

    private void autoSizeColumns(Sheet sheet, int totalColumns) {
        for (int i = 0; i < totalColumns; i++) {
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

    private String buildRelatorioPorVeiculoFilename(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return "relatorio-por-veiculo_"
                + LocalDate.from(dataInicio).format(FILE_DATE_FORMAT)
                + "_"
                + LocalDate.from(dataFim).format(FILE_DATE_FORMAT)
                + ".xlsx";
    }

    private String buildRelatorioPorProfissionalFilename(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return "relatorio-por-profissional_"
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

    private void writeIntegerCell(Row row, int column, Integer value) {
        row.createCell(column).setCellValue(value == null ? 0 : value);
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
