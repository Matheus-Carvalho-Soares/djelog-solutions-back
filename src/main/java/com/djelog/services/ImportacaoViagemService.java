package com.djelog.services;

import com.djelog.dtos.*;
import com.djelog.entities.*;
import com.djelog.repositories.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ImportacaoViagemService {
    private static final Set<String> STATUSES = Set.of("CONCLUIDA", "EM_ANDAMENTO", "CANCELADA");
    private final ControleViagensVolvoParser parser;
    private final ImportacaoViagemRepository importacaoRepository;
    private final ImportacaoViagemLinhaRepository linhaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final VeiculoRepository veiculoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ViagemRepository viagemRepository;
    private final DespesaRepository despesaRepository;

    public ImportacaoViagemService(ControleViagensVolvoParser parser, ImportacaoViagemRepository importacaoRepository,
            ImportacaoViagemLinhaRepository linhaRepository, UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository, VeiculoRepository veiculoRepository,
            ProfissionalRepository profissionalRepository, ViagemRepository viagemRepository,
            DespesaRepository despesaRepository) {
        this.parser = parser;
        this.importacaoRepository = importacaoRepository;
        this.linhaRepository = linhaRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.veiculoRepository = veiculoRepository;
        this.profissionalRepository = profissionalRepository;
        this.viagemRepository = viagemRepository;
        this.despesaRepository = despesaRepository;
    }

    @Transactional
    public ImportacaoViagemDTO preview(MultipartFile file, UUID usuarioId) {
        List<ControleViagensVolvoParser.ParsedRow> parsed = parser.parse(file);
        Usuario owner = usuarioRepository.findById(usuarioId).orElseThrow(() -> new AccessDeniedException("Usuário não encontrado."));
        List<Veiculo> vehicles = veiculoRepository.findByProfissional_Usuario_Id(usuarioId);
        List<Profissional> drivers = profissionalRepository.findByUsuario_Id(usuarioId);
        List<Empresa> companies = empresaRepository.findByUsuario_Id(usuarioId);
        List<Viagem> trips = viagemRepository.findByProfissional_Usuario_Id(usuarioId);
        Map<UUID, List<Despesa>> expenses = loadExpenses(trips);
        Map<String, Long> existingSignatures = existingSignatures(usuarioId, trips, expenses);

        ImportacaoViagem batch = new ImportacaoViagem();
        batch.setUsuario(owner);
        batch.setNomeArquivo(safeFilename(file.getOriginalFilename()));
        batch.setHashArquivo(sha256(file));
        batch.setStatus("PREVIA");
        batch = importacaoRepository.save(batch);

        Map<String, Integer> seenInSheet = new HashMap<>();
        List<ImportacaoViagemLinha> rows = new ArrayList<>();
        for (ControleViagensVolvoParser.ParsedRow source : parsed) {
            ImportacaoViagemLinha row = toEntity(source, batch);
            if (!source.errors().isEmpty()) {
                row.setSituacao("DADO_INVALIDO");
                row.setMotivo(String.join(" ", source.errors()));
            } else {
                List<Veiculo> vehicleMatches = vehicles.stream()
                        .filter(v -> plateKey(v.getPlaca()).equals(plateKey(source.plate()))).toList();
                Veiculo vehicle = vehicleMatches.size() == 1 ? vehicleMatches.getFirst() : null;
                List<Empresa> companyMatches = companies.stream()
                        .filter(e -> companyKey(e.getNome()).equals(companyKey(source.company()))).toList();
                Empresa company = companyMatches.size() == 1 ? companyMatches.getFirst() : null;
                if (vehicle != null) {
                    row.setVeiculoId(vehicle.getId());
                    if (vehicle.getProfissional() != null) row.setProfissionalId(vehicle.getProfissional().getId());
                }
                if (company != null) row.setEmpresaId(company.getId());
                else {
                    String warning = source.company() == null || source.company().isBlank()
                            ? "Empresa não informada; a viagem será importada sem esse vínculo."
                            : companyMatches.size() > 1
                                ? "Mais de uma empresa corresponde ao nome da planilha; selecione uma ou importe sem empresa."
                                : "Empresa sem correspondência no cadastro; selecione uma ou importe sem empresa.";
                    addWarning(row, warning);
                }
                List<String> reasons = new ArrayList<>();
                if (vehicle == null) reasons.add(vehicleMatches.size() > 1
                        ? "A placa corresponde a mais de um veículo; selecione o correto."
                        : "Veículo não encontrado pela placa; selecione um veículo.");
                if (row.getProfissionalId() == null) reasons.add("Motorista não definido pelo veículo; selecione um motorista.");
                if (!reasons.isEmpty()) {
                    row.setSituacao("SEM_VINCULO");
                    row.setMotivo(String.join(" ", reasons));
                } else {
                    row.setAssinatura(signature(row));
                    boolean incompleteExpenses = source.warnings().stream().anyMatch(w -> w.contains(": //"));
                    long exactCount = incompleteExpenses ? 0 : existingSignatures.getOrDefault(row.getAssinatura(), 0L);
                    int ordinal = seenInSheet.merge(row.getAssinatura(), 1, Integer::sum);
                    if (ordinal <= exactCount && !incompleteExpenses) {
                        row.setSituacao("DUPLICATA_EXATA");
                        row.setMotivo("Assinatura igual a uma viagem existente (data, vínculos, rota, valores e despesas). Confira antes de importar.");
                    } else if (ordinal > exactCount + 1) {
                        row.setSituacao("REPETIDA_NA_PLANILHA");
                        row.setMotivo("Esta linha é igual a outra do mesmo arquivo; cada ocorrência representa uma viagem separada.");
                        row.setSelecionada(true);
                    } else if (incompleteExpenses && ordinal == 1) {
                        row.setSituacao("POSSIVEL_DUPLICATA");
                        row.setMotivo("Há despesa marcada como //; como o valor é desconhecido, a linha não será tratada como duplicata exata.");
                    } else {
                        row.setSituacao("NOVA");
                        row.setMotivo("Nenhuma viagem idêntica encontrada.");
                        row.setSelecionada(true);
                    }
                }
            }
            rows.add(row);
        }
        linhaRepository.saveAll(rows);
        return buildDTO(batch, rows, vehicles, drivers, companies, trips, expenses);
    }

    @Transactional(readOnly = true)
    public ImportacaoViagemDTO get(UUID id, UUID usuarioId) {
        ImportacaoViagem batch = importacaoRepository.findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Importação não encontrada."));
        List<ImportacaoViagemLinha> rows = linhaRepository.findByImportacao_IdOrderByNumeroLinha(id);
        List<Veiculo> vehicles = veiculoRepository.findByProfissional_Usuario_Id(usuarioId);
        List<Profissional> drivers = profissionalRepository.findByUsuario_Id(usuarioId);
        List<Empresa> companies = empresaRepository.findByUsuario_Id(usuarioId);
        List<Viagem> trips = viagemRepository.findByProfissional_Usuario_Id(usuarioId);
        return buildDTO(batch, rows, vehicles, drivers, companies, trips, loadExpenses(trips));
    }

    @Transactional
    public ResultadoImportacaoDTO confirm(UUID id, UUID usuarioId, ConfirmacaoImportacaoDTO request) {
        ImportacaoViagem batch = importacaoRepository.lockByIdAndUsuario(id, usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Importação não encontrada."));
        if (!"PREVIA".equals(batch.getStatus())) throw new IllegalStateException("Esta importação já foi confirmada.");
        List<ImportacaoViagemLinha> rows = linhaRepository.findByImportacao_IdOrderByNumeroLinha(id);
        Map<UUID, ImportacaoViagemLinha> byId = rows.stream().collect(Collectors.toMap(ImportacaoViagemLinha::getId, Function.identity()));
        if (request == null || request.linhas() == null) throw new IllegalArgumentException("Informe as decisões das linhas.");
        for (ConfirmacaoLinhaImportacaoDTO decision : request.linhas()) {
            ImportacaoViagemLinha row = byId.get(decision.linhaId());
            if (row == null) throw new IllegalArgumentException("A importação contém uma linha inválida.");
            row.setSelecionada(decision.incluir());
            if (decision.veiculoId() != null) row.setVeiculoId(decision.veiculoId());
            if (decision.profissionalId() != null) row.setProfissionalId(decision.profissionalId());
            row.setEmpresaId(decision.empresaId());
            if (decision.status() != null && !decision.status().isBlank()) row.setStatusViagem(decision.status());
        }
        List<ImportacaoViagemLinha> selected = rows.stream().filter(ImportacaoViagemLinha::isSelecionada).toList();
        if (selected.stream().anyMatch(row -> "DADO_INVALIDO".equals(row.getSituacao()))) {
            throw new IllegalArgumentException("Corrija os dados inválidos ou desmarque essas linhas.");
        }
        if (selected.stream().anyMatch(row -> row.getVeiculoId() == null || row.getProfissionalId() == null)) {
            throw new IllegalArgumentException("Resolva os vínculos de veículo e motorista nas linhas selecionadas.");
        }
        if (selected.stream().anyMatch(row -> row.getStatusViagem() != null && !STATUSES.contains(row.getStatusViagem()))) {
            throw new IllegalArgumentException("O status de uma viagem é inválido.");
        }

        List<Viagem> currentTrips = viagemRepository.findByProfissional_Usuario_Id(usuarioId);
        Map<UUID, List<Despesa>> expensesByTrip = loadExpenses(currentTrips);
        Map<String, Long> signatureCounts = existingSignatures(usuarioId, currentTrips, expensesByTrip);
        boolean exactSelected = selected.stream().anyMatch(row -> "DUPLICATA_EXATA".equals(row.getSituacao()));
        if ((exactSelected || selected.stream().anyMatch(row -> signatureCounts.getOrDefault(signature(row), 0L) > 0))
                && !request.confirmarDuplicatasExatas()) {
            throw new IllegalArgumentException("Há duplicatas exatas selecionadas. Confirme explicitamente a importação dessas viagens.");
        }

        Map<UUID, Veiculo> vehicles = veiculoRepository.findByProfissional_Usuario_Id(usuarioId).stream()
                .collect(Collectors.toMap(Veiculo::getId, Function.identity()));
        Map<UUID, Profissional> drivers = profissionalRepository.findByUsuario_Id(usuarioId).stream()
                .collect(Collectors.toMap(Profissional::getId, Function.identity()));
        Map<UUID, Empresa> companies = empresaRepository.findByUsuario_Id(usuarioId).stream()
                .collect(Collectors.toMap(Empresa::getId, Function.identity()));
        List<String> warnings = new ArrayList<>();
        for (ImportacaoViagemLinha row : selected) {
            Veiculo vehicle = vehicles.get(row.getVeiculoId());
            Profissional driver = drivers.get(row.getProfissionalId());
            Empresa company = companies.get(row.getEmpresaId());
            if (vehicle == null || driver == null || (row.getEmpresaId() != null && company == null)) {
                throw new AccessDeniedException("Um vínculo selecionado não pertence a este usuário.");
            }
            if (row.getDataViagem() == null || row.getInicioFrete() == null || row.getValorFrete() == null) {
                throw new IllegalArgumentException("Uma linha selecionada não tem os campos obrigatórios.");
            }
            Viagem trip = new Viagem();
            trip.setVeiculo(vehicle);
            trip.setProfissional(driver);
            trip.setEmpresa(company);
            trip.setDataInicio(row.getDataViagem().atStartOfDay());
            trip.setDataFim(null);
            trip.setInicioFrete(row.getInicioFrete());
            trip.setParadaIntermediaria(row.getParadaIntermediaria());
            trip.setFimFrete(row.getFimFrete());
            trip.setValorFrete(row.getValorFrete());
            trip.setComissao(row.getComissao() == null ? BigDecimal.ZERO : row.getComissao());
            String status = row.getStatusViagem() == null || row.getStatusViagem().isBlank() ? "CONCLUIDA" : row.getStatusViagem();
            trip.setStatus(status);
            trip = viagemRepository.save(trip);
            List<Despesa> expenses = makeExpenses(row, trip);
            if (!expenses.isEmpty()) despesaRepository.saveAll(expenses);
            row.setViagem(trip);
            row.setAssinatura(signature(row));
            row.setSituacao("IMPORTADA");
            row.setMotivo("Viagem criada a partir desta linha da planilha.");
            signatureCounts.merge(row.getAssinatura(), 1L, Long::sum);
        }
        int ignored = rows.size() - selected.size();
        batch.setStatus("CONFIRMADA");
        linhaRepository.saveAll(rows);
        importacaoRepository.save(batch);
        return new ResultadoImportacaoDTO(batch.getId(), selected.size(), ignored, warnings);
    }

    private List<Despesa> makeExpenses(ImportacaoViagemLinha row, Viagem trip) {
        List<Despesa> result = new ArrayList<>();
        addExpense(result, trip, "Abastecimento", null, row.getAbastecimento());
        addExpense(result, trip, "Pedágio", null, row.getPedagio());
        addExpense(result, trip, "Outras despesas", row.getDescricaoOutras(), row.getOutrasDespesas());
        return result;
    }

    private void addExpense(List<Despesa> output, Viagem trip, String name, String description, BigDecimal amount) {
        if (amount == null || amount.signum() == 0) return;
        Despesa expense = new Despesa();
        expense.setViagem(trip);
        expense.setNome(name);
        expense.setDescricao(description);
        expense.setValor(amount);
        output.add(expense);
    }

    private ImportacaoViagemLinha toEntity(ControleViagensVolvoParser.ParsedRow source, ImportacaoViagem batch) {
        ImportacaoViagemLinha row = new ImportacaoViagemLinha();
        row.setImportacao(batch);
        row.setNumeroLinha(source.sourceRow());
        row.setDataViagem(source.date());
        row.setPlaca(source.plate());
        row.setEmpresaOriginal(source.company());
        row.setInicioFrete(source.origin());
        row.setParadaIntermediaria(source.middle());
        row.setFimFrete(source.destination());
        row.setValorFrete(source.freight());
        row.setComissao(source.commission());
        row.setAbastecimento(source.fuel());
        row.setPedagio(source.toll());
        row.setOutrasDespesas(source.otherExpenses());
        row.setDescricaoOutras(source.otherDescription());
        row.setAvisos(String.join("\n", source.warnings()));
        row.setStatusViagem("CONCLUIDA");
        return row;
    }

    private ImportacaoViagemDTO buildDTO(ImportacaoViagem batch, List<ImportacaoViagemLinha> rows,
            List<Veiculo> vehicles, List<Profissional> drivers, List<Empresa> companies,
            List<Viagem> trips, Map<UUID, List<Despesa>> expenses) {
        List<ImportacaoLinhaDTO> lineDTOs = rows.stream().map(row -> {
            List<ImportacaoSugestaoDTO> similar = findSimilar(row, trips, expenses);
            List<String> warnings = row.getAvisos() == null || row.getAvisos().isBlank()
                    ? List.of() : Arrays.asList(row.getAvisos().split("\n"));
            return new ImportacaoLinhaDTO(row.getId(), row.getNumeroLinha(), row.getDataViagem(), row.getPlaca(),
                    row.getEmpresaOriginal(), row.getInicioFrete(), row.getParadaIntermediaria(), row.getFimFrete(),
                    row.getValorFrete(), row.getComissao(), row.getAbastecimento(), row.getPedagio(), row.getOutrasDespesas(),
                    row.getDescricaoOutras(), row.getVeiculoId(), row.getProfissionalId(), row.getEmpresaId(),
                    row.getStatusViagem(), row.getSituacao(), row.getMotivo(), row.isSelecionada(), warnings, similar);
        }).toList();
        List<ImportacaoCatalogoDTO> vehicleDTOs = vehicles.stream().map(v -> new ImportacaoCatalogoDTO(
                v.getId(), v.getMarca() + (v.getNome() == null ? "" : " " + v.getNome()),
                Optional.ofNullable(v.getPlaca()).orElse(""), v.getProfissional() == null ? null : v.getProfissional().getId())).toList();
        List<ImportacaoCatalogoDTO> driverDTOs = drivers.stream().map(d -> new ImportacaoCatalogoDTO(d.getId(), d.getNome(), d.getTelefone(), null)).toList();
        List<ImportacaoCatalogoDTO> companyDTOs = companies.stream().map(c -> new ImportacaoCatalogoDTO(c.getId(), c.getNome(), null, null)).toList();
        return new ImportacaoViagemDTO(batch.getId(), batch.getNomeArquivo(), batch.getStatus(), batch.getCriadoEm(),
                lineDTOs, vehicleDTOs, driverDTOs, companyDTOs);
    }

    private List<ImportacaoSugestaoDTO> findSimilar(ImportacaoViagemLinha source, List<Viagem> trips, Map<UUID, List<Despesa>> expenses) {
        if (source.getDataViagem() == null || source.getValorFrete() == null) return List.of();
        return trips.stream().map(trip -> {
            int score = 0;
            List<String> matches = new ArrayList<>();
            if (source.getVeiculoId() != null && source.getVeiculoId().equals(trip.getVeiculo().getId())) { score += 30; matches.add("veículo"); }
            if (source.getDataViagem().equals(trip.getDataInicio().toLocalDate())) { score += 25; matches.add("data"); }
            if (source.getProfissionalId() != null && source.getProfissionalId().equals(trip.getProfissional().getId())) { score += 15; matches.add("motorista"); }
            if (routeKey(source.getInicioFrete(), source.getParadaIntermediaria(), source.getFimFrete())
                    .equals(routeKey(trip.getInicioFrete(), trip.getParadaIntermediaria(), trip.getFimFrete()))) { score += 15; matches.add("rota"); }
            if (source.getEmpresaId() != null && trip.getEmpresa() != null
                    && source.getEmpresaId().equals(trip.getEmpresa().getId())) { score += 10; matches.add("empresa"); }
            if (sameAmounts(source, trip, expenses.getOrDefault(trip.getId(), List.of()))) { score += 5; matches.add("valores/despesas"); }
            String plate = trip.getVeiculo() == null ? null : trip.getVeiculo().getPlaca();
            String driver = trip.getProfissional() == null ? null : trip.getProfissional().getNome();
            String company = trip.getEmpresa() == null ? null : trip.getEmpresa().getNome();
            return new ImportacaoSugestaoDTO(trip.getId(), trip.getDataInicio().toLocalDate(), plate, driver, company,
                    routeLabel(trip.getInicioFrete(), trip.getParadaIntermediaria(), trip.getFimFrete()), score,
                    matches.isEmpty() ? "Critérios não coincidentes" : "Coincidem: " + String.join(", ", matches));
        }).filter(match -> match.pontuacao() >= 60)
                .sorted(Comparator.comparingInt(ImportacaoSugestaoDTO::pontuacao).reversed())
                .limit(3).toList();
    }

    private boolean sameAmounts(ImportacaoViagemLinha row, Viagem trip, List<Despesa> expenses) {
        if (row.getValorFrete().compareTo(trip.getValorFrete()) != 0 || zero(row.getComissao()).compareTo(zero(trip.getComissao())) != 0) return false;
        Map<String, BigDecimal> found = expenses.stream().collect(Collectors.groupingBy(d -> ControleViagensVolvoParser.normalize(d.getNome()),
                Collectors.reducing(BigDecimal.ZERO, d -> zero(d.getValor()), BigDecimal::add)));
        BigDecimal other = found.entrySet().stream().filter(e -> !Set.of("abastecimento", "pedagio").contains(e.getKey()))
                .map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        return zero(row.getAbastecimento()).compareTo(found.getOrDefault("abastecimento", BigDecimal.ZERO)) == 0
                && zero(row.getPedagio()).compareTo(found.getOrDefault("pedagio", BigDecimal.ZERO)) == 0
                && zero(row.getOutrasDespesas()).compareTo(other) == 0;
    }

    private Map<UUID, List<Despesa>> loadExpenses(List<Viagem> trips) {
        if (trips.isEmpty()) return Map.of();
        List<UUID> ids = trips.stream().map(Viagem::getId).toList();
        return despesaRepository.findByViagemIdsWithViagem(ids).stream().collect(Collectors.groupingBy(d -> d.getViagem().getId()));
    }

    private String signature(ImportacaoViagemLinha row) {
        return digest(String.join("|", Objects.toString(row.getDataViagem(), ""), Objects.toString(row.getVeiculoId(), ""),
                Objects.toString(row.getProfissionalId(), ""), Objects.toString(row.getEmpresaId(), ""),
                routeKey(row.getInicioFrete(), row.getParadaIntermediaria(), row.getFimFrete()),
                decimal(row.getValorFrete()), decimal(row.getComissao()),
                "abastecimento:" + decimal(row.getAbastecimento()) + ":",
                "pedagio:" + decimal(row.getPedagio()) + ":",
                "outras despesas:" + decimal(row.getOutrasDespesas()) + ":" + ControleViagensVolvoParser.normalize(row.getDescricaoOutras())));
    }

    private String signature(Viagem trip, List<Despesa> expenses) {
        Map<String, List<Despesa>> byName = expenses.stream().collect(Collectors.groupingBy(d -> ControleViagensVolvoParser.normalize(d.getNome())));
        List<String> parts = new ArrayList<>(List.of(Objects.toString(trip.getDataInicio().toLocalDate(), ""),
                Objects.toString(trip.getVeiculo() == null ? null : trip.getVeiculo().getId(), ""),
                Objects.toString(trip.getProfissional() == null ? null : trip.getProfissional().getId(), ""),
                Objects.toString(trip.getEmpresa() == null ? null : trip.getEmpresa().getId(), ""),
                routeKey(trip.getInicioFrete(), trip.getParadaIntermediaria(), trip.getFimFrete()),
                decimal(trip.getValorFrete()), decimal(trip.getComissao())));
        for (String category : List.of("abastecimento", "pedagio", "outras despesas")) {
            List<Despesa> values = byName.getOrDefault(category, List.of());
            BigDecimal total = values.isEmpty() ? null : values.stream().map(d -> zero(d.getValor())).reduce(BigDecimal.ZERO, BigDecimal::add);
            String description = category.equals("outras despesas") ? values.stream().map(Despesa::getDescricao)
                    .filter(Objects::nonNull).map(ControleViagensVolvoParser::normalize).sorted().collect(Collectors.joining(",")) : "";
            parts.add(category + ":" + decimal(total) + ":" + description);
        }
        expenses.stream().filter(d -> !Set.of("abastecimento", "pedagio", "outras despesas").contains(ControleViagensVolvoParser.normalize(d.getNome())))
                .map(d -> ControleViagensVolvoParser.normalize(d.getNome()) + ":" + decimal(d.getValor())
                        + ":" + ControleViagensVolvoParser.normalize(d.getDescricao())).sorted().forEach(parts::add);
        return digest(String.join("|", parts));
    }

    private Map<String, Long> existingSignatures(UUID usuarioId, List<Viagem> trips, Map<UUID, List<Despesa>> expenses) {
        List<ImportacaoViagemLinha> imported = linhaRepository.findByImportacao_Usuario_IdAndViagemIsNotNull(usuarioId);
        Set<UUID> importedTripIds = imported.stream().map(line -> line.getViagem().getId()).collect(Collectors.toSet());
        Map<String, Long> counts = imported.stream().filter(line -> line.getViagem() != null)
                .map(line -> signature(line.getViagem(), expenses.getOrDefault(line.getViagem().getId(), List.of())))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        trips.stream().filter(trip -> !importedTripIds.contains(trip.getId()))
                .map(trip -> signature(trip, expenses.getOrDefault(trip.getId(), List.of())))
                .forEach(signature -> counts.merge(signature, 1L, Long::sum));
        return counts;
    }
    private String routeKey(String origin, String middle, String end) {
        return String.join(">", List.of(Objects.toString(origin, ""), Objects.toString(middle, ""), Objects.toString(end, ""))
                .stream().map(ControleViagensVolvoParser::normalize).toList());
    }
    private String routeLabel(String origin, String middle, String end) {
        return Arrays.asList(origin, middle, end).stream().filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(" → "));
    }
    private String plateKey(String plate) { return ControleViagensVolvoParser.normalize(plate).replace(" ", ""); }
    private String companyKey(String name) { return ControleViagensVolvoParser.normalize(name); }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String decimal(BigDecimal value) { return value == null ? "-" : value.stripTrailingZeros().toPlainString(); }
    private void addWarning(ImportacaoViagemLinha row, String warning) {
        String current = row.getAvisos();
        row.setAvisos(current == null || current.isBlank() ? warning : current + "\n" + warning);
    }

    private String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    private String sha256(MultipartFile file) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(file.getBytes())); }
        catch (Exception e) { throw new IllegalArgumentException("Não foi possível calcular a impressão digital do arquivo.", e); }
    }
    private String safeFilename(String filename) {
        String name = Optional.ofNullable(filename).orElse("importacao.xlsx").replaceAll("[\\\\/\\r\\n]", "_");
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }
}
