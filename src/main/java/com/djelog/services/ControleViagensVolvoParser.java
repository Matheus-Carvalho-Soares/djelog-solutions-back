package com.djelog.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.Normalizer;
import java.util.*;

@Component
public class ControleViagensVolvoParser {
    public static final int MAX_BYTES = 15 * 1024 * 1024;
    public static final int MAX_ROWS = 5000;
    private static final List<String> INPUT_COLUMNS = List.of("data", "placa cavalo", "transportadora", "destino 1",
            "destino 2", "destino 3", "preco frete", "abastecimento", "pedagio", "outras despesas", "descricao outras");
    private static final ThreadLocal<DataFormatter> FORMATTER = ThreadLocal.withInitial(() -> new DataFormatter(Locale.forLanguageTag("pt-BR")));

    public List<ParsedRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Selecione um arquivo XLSX.");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("O arquivo deve ter no máximo 15 MB.");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (!filename.endsWith(".xlsx")) throw new IllegalArgumentException("Envie um arquivo .xlsx.");
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()))) {
            Sheet lancamentos = findSheet(workbook, "lancamentos", "lançamentos");
            if (lancamentos == null) throw new IllegalArgumentException("Não encontrei a aba 'Lançamentos'.");
            BigDecimal commission = findCommission(findSheet(workbook, "cadastro"));
            int headerRowIndex = findHeaderRow(lancamentos);
            Row header = lancamentos.getRow(headerRowIndex);
            Map<String, Integer> columns = headerMap(header);
            require(columns, "data", "placa cavalo", "transportadora", "destino 1", "destino 2", "destino 3", "preco frete", "abastecimento", "pedagio", "outras despesas", "descricao outras");

            List<ParsedRow> parsed = new ArrayList<>();
            for (int rowIndex = headerRowIndex + 1; rowIndex <= lancamentos.getLastRowNum(); rowIndex++) {
                Row row = lancamentos.getRow(rowIndex);
                if (row == null || isBlankInput(row, columns)) continue;
                if (parsed.size() >= MAX_ROWS) throw new IllegalArgumentException("O arquivo ultrapassa o limite de 5.000 linhas de viagens.");
                List<String> warnings = new ArrayList<>();
                LocalDate date = readDate(cell(row, columns, "data"), warnings);
                String plate = readText(cell(row, columns, "placa cavalo"), warnings);
                String company = readText(cell(row, columns, "transportadora"), warnings);
                String origin = readText(cell(row, columns, "destino 1"), warnings);
                String middle = readText(cell(row, columns, "destino 2"), warnings);
                String destination = readText(cell(row, columns, "destino 3"), warnings);
                BigDecimal freight = readMoney(cell(row, columns, "preco frete"), warnings, "Preço Frete");
                BigDecimal fuel = readMoney(cell(row, columns, "abastecimento"), warnings, "Abastecimento");
                BigDecimal toll = readMoney(cell(row, columns, "pedagio"), warnings, "Pedágio");
                BigDecimal other = readMoney(cell(row, columns, "outras despesas"), warnings, "Outras Despesas");
                String otherDescription = readText(cell(row, columns, "descricao outras"), warnings);
                List<String> errors = new ArrayList<>();
                if (date == null) errors.add("Data ausente ou inválida.");
                if (plate == null) errors.add("Placa Cavalo ausente.");
                if (origin == null) errors.add("Destino 1 (origem) ausente.");
                if (freight == null || freight.signum() <= 0) errors.add("Preço Frete deve ser maior que zero.");
                if (commission == null) warnings.add("Comissão não encontrada na aba Cadastro; será usada 0%.");
                parsed.add(new ParsedRow(rowIndex + 1, date, plate, company, origin, middle, destination, freight,
                        commission == null ? BigDecimal.ZERO.setScale(2) : commission, fuel, toll, other,
                        otherDescription, warnings, errors));
            }
            if (parsed.isEmpty()) throw new IllegalArgumentException("Não encontrei linhas de viagens preenchidas na aba 'Lançamentos'.");
            return parsed;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível ler o XLSX. Verifique se o arquivo não está corrompido.", e);
        }
    }

    private Sheet findSheet(Workbook workbook, String... names) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String normalized = normalize(workbook.getSheetName(i));
            for (String name : names) if (normalized.equals(normalize(name))) return workbook.getSheetAt(i);
        }
        return null;
    }

    private int findHeaderRow(Sheet sheet) {
        for (int i = sheet.getFirstRowNum(); i <= Math.min(sheet.getLastRowNum(), 40); i++) {
            Map<String, Integer> headers = headerMap(sheet.getRow(i));
            if (headers.containsKey("data") && headers.containsKey("placa cavalo") && headers.containsKey("preco frete")) return i;
        }
        throw new IllegalArgumentException("Não encontrei o cabeçalho de viagens (Data, Placa Cavalo, Preço Frete).");
    }

    private Map<String, Integer> headerMap(Row row) {
        Map<String, Integer> result = new HashMap<>();
        if (row == null) return result;
        for (Cell c : row) {
            String value = normalize(FORMATTER.get().formatCellValue(c));
            if (!value.isBlank()) result.put(value, c.getColumnIndex());
        }
        return result;
    }

    private void require(Map<String, Integer> columns, String... names) {
        List<String> missing = Arrays.stream(names).filter(name -> !columns.containsKey(normalize(name))).toList();
        if (!missing.isEmpty()) throw new IllegalArgumentException("Faltam colunas necessárias na aba Lançamentos: " + String.join(", ", missing) + ".");
    }

    private boolean isBlankInput(Row row, Map<String, Integer> columns) {
        return INPUT_COLUMNS.stream().map(normalizeName -> row.getCell(columns.get(normalizeName))).allMatch(this::isBlank);
    }

    private Cell cell(Row row, Map<String, Integer> columns, String name) { return row.getCell(columns.get(normalize(name))); }
    private boolean isBlank(Cell cell) { return cell == null || cell.getCellType() == CellType.BLANK || FORMATTER.get().formatCellValue(cell).isBlank(); }

    private String readText(Cell cell, List<String> warnings) {
        if (isBlank(cell)) return null;
        if (cell.getCellType() == CellType.FORMULA) {
            warnings.add("Campo de entrada com fórmula ignorado (" + cell.getAddress().formatAsString() + ").");
            return null;
        }
        String value = FORMATTER.get().formatCellValue(cell).trim().replaceAll("\\s+", " ");
        return value.isBlank() || value.equals("//") ? null : value;
    }

    private LocalDate readDate(Cell cell, List<String> warnings) {
        if (isBlank(cell)) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toLocalDate();
        String value = readText(cell, warnings);
        if (value == null) return null;
        for (DateTimeFormatter format : List.of(DateTimeFormatter.ofPattern("d/M/uuuu"), DateTimeFormatter.ISO_LOCAL_DATE)) {
            try { return LocalDate.parse(value, format); } catch (DateTimeParseException ignored) { }
        }
        warnings.add("Data '" + value + "' não pôde ser interpretada.");
        return null;
    }

    private BigDecimal readMoney(Cell cell, List<String> warnings, String label) {
        if (isBlank(cell)) return null;
        if (cell.getCellType() == CellType.FORMULA) {
            warnings.add(label + " contém fórmula e foi ignorado.");
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
        String value = FORMATTER.get().formatCellValue(cell).trim();
        if (value.equals("//")) {
            warnings.add(label + ": // interpretado como informação ausente, não como zero.");
            return null;
        }
        String numeric = value.replaceAll("(?i)R\\$", "").replaceAll("\\s", "");
        if (numeric.contains(",")) numeric = numeric.replace(".", "").replace(',', '.');
        try { return new BigDecimal(numeric).setScale(2, RoundingMode.HALF_UP); }
        catch (NumberFormatException e) { warnings.add(label + " contém um valor inválido ('" + value + "')."); return null; }
    }

    private BigDecimal findCommission(Sheet sheet) {
        if (sheet == null) return null;
        for (Row row : sheet) for (Cell cell : row) {
            if (!normalize(FORMATTER.get().formatCellValue(cell)).contains("comissao")) continue;
            Cell valueCell = row.getCell(cell.getColumnIndex() + 1);
            BigDecimal value = commissionValue(valueCell);
            if (value == null && row.getRowNum() < sheet.getLastRowNum()) {
                Row next = sheet.getRow(row.getRowNum() + 1);
                if (next != null) value = commissionValue(next.getCell(cell.getColumnIndex()));
            }
            if (value != null) return value;
        }
        return null;
    }

    private BigDecimal commissionValue(Cell cell) {
        if (cell == null || isBlank(cell) || cell.getCellType() == CellType.FORMULA) return null;
        String displayed = FORMATTER.get().formatCellValue(cell).replace("%", "").trim();
        try {
            BigDecimal value = cell.getCellType() == CellType.NUMERIC
                    ? BigDecimal.valueOf(cell.getNumericCellValue())
                    : new BigDecimal(displayed.replace(',', '.'));
            if (cell.getCellType() == CellType.NUMERIC && cell.getCellStyle().getDataFormatString().contains("%")) value = value.multiply(BigDecimal.valueOf(100));
            if (displayed.contains("%") && value.compareTo(BigDecimal.ONE) <= 0) value = value.multiply(BigDecimal.valueOf(100));
            return value.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) { return null; }
    }

    public static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
    }

    public record ParsedRow(int sourceRow, LocalDate date, String plate, String company, String origin, String middle,
                            String destination, BigDecimal freight, BigDecimal commission, BigDecimal fuel,
                            BigDecimal toll, BigDecimal otherExpenses, String otherDescription,
                            List<String> warnings, List<String> errors) { }
}
