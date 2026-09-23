package com.djelog.solutions;

import com.djelog.dtos.*;
import com.djelog.entities.*;
import com.djelog.repositories.*;
import com.djelog.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoViagemIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired ProfissionalRepository profissionalRepository;
    @Autowired VeiculoRepository veiculoRepository;
    @Autowired ViagemRepository viagemRepository;
    @Autowired DespesaRepository despesaRepository;
    @Autowired ImportacaoViagemRepository importacaoRepository;
    @Autowired ImportacaoViagemLinhaRepository linhaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired UserDetailsService userDetailsService;

    @BeforeEach
    void clean() {
        linhaRepository.deleteAll();
        importacaoRepository.deleteAll();
        despesaRepository.deleteAll();
        viagemRepository.deleteAll();
        veiculoRepository.deleteAll();
        empresaRepository.deleteAll();
        profissionalRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void importsRepeatedOccurrencesAndDetectsTheSameTripsOnTheNextUpload() throws Exception {
        Usuario owner = createUser("import-owner@example.com");
        createCatalog(owner);
        String token = token(owner);
        ImportacaoViagemDTO first = preview(token, workbook(2));
        assertThat(first.linhas()).hasSize(2);
        assertThat(first.linhas()).allMatch(row -> row.selecionada());
        assertThat(first.linhas()).extracting(ImportacaoLinhaDTO::situacao)
                .containsExactly("NOVA", "REPETIDA_NA_PLANILHA");

        List<ConfirmacaoLinhaImportacaoDTO> decisions = first.linhas().stream()
                .map(row -> new ConfirmacaoLinhaImportacaoDTO(row.id(), true, row.veiculoId(), row.profissionalId(), row.empresaId(), "CONCLUIDA"))
                .toList();
        mockMvc.perform(post("/api/viagem/importacoes/" + first.id() + "/confirmar")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmacaoImportacaoDTO(false, decisions))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criadas").value(2));
        assertThat(viagemRepository.count()).isEqualTo(2);

        ImportacaoViagemDTO repeated = preview(token, workbook(2));
        assertThat(repeated.linhas()).allMatch(row -> row.situacao().equals("DUPLICATA_EXATA") && !row.selecionada());
        mockMvc.perform(get("/api/viagem/importacoes/" + repeated.id()).header("Authorization", token(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.linhas.length()").value(2));

        Usuario other = createUser("import-other@example.com");
        mockMvc.perform(get("/api/viagem/importacoes/" + repeated.id()).header("Authorization", token(other)))
                .andExpect(status().isNotFound());

        Viagem changed = viagemRepository.findByProfissional_Usuario_Id(owner.getId()).getFirst();
        changed.setValorFrete(new BigDecimal("401.00"));
        viagemRepository.save(changed);
        ImportacaoViagemDTO edited = preview(token, workbook(2));
        assertThat(edited.linhas()).filteredOn(row -> row.situacao().equals("DUPLICATA_EXATA")).hasSize(1);
        assertThat(edited.linhas()).filteredOn(row -> row.situacao().equals("NOVA")).hasSize(1);
        changed.setValorFrete(new BigDecimal("400.00"));
        viagemRepository.save(changed);

        ImportacaoViagemDTO added = preview(token, workbook(3));
        assertThat(added.linhas()).filteredOn(row -> row.situacao().equals("DUPLICATA_EXATA")).hasSize(2);
        assertThat(added.linhas()).filteredOn(row -> row.selecionada()).hasSize(1);
        assertThat(added.linhas()).filteredOn(row -> row.selecionada()).allMatch(row -> row.situacao().equals("NOVA"));
        assertThat(added.linhas().getLast().paradaIntermediaria()).isEqualTo("Sorocaba");
        List<ConfirmacaoLinhaImportacaoDTO> addDecision = added.linhas().stream()
                .map(row -> new ConfirmacaoLinhaImportacaoDTO(row.id(), row.selecionada(), row.veiculoId(), row.profissionalId(), row.empresaId(), row.statusViagem()))
                .toList();
        mockMvc.perform(post("/api/viagem/importacoes/" + added.id() + "/confirmar")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmacaoImportacaoDTO(false, addDecision))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.criadas").value(1));
        Viagem addedTrip = viagemRepository.findByProfissional_Usuario_Id(owner.getId()).stream()
                .filter(trip -> trip.getInicioFrete().equals("Campinas")).findFirst().orElseThrow();
        assertThat(addedTrip.getParadaIntermediaria()).isEqualTo("Sorocaba");
        assertThat(despesaRepository.findByViagem_Id(addedTrip.getId())).extracting(Despesa::getValor)
                .containsExactlyInAnyOrder(new BigDecimal("3826.77"), new BigDecimal("316.20"), new BigDecimal("22.62"));
    }

    @Test
    void requiresExplicitDuplicateAcknowledgementAndRollsBackWhenOneSelectedLinkIsForeign() throws Exception {
        Usuario owner = createUser("import-rollback@example.com");
        createCatalog(owner);
        Usuario other = createUser("import-foreign@example.com");
        Catalog foreignCatalog = createCatalog(other);
        String token = token(owner);
        ImportacaoViagemDTO initial = preview(token, workbook(2));
        List<ConfirmacaoLinhaImportacaoDTO> initialDecisions = initial.linhas().stream()
                .map(row -> new ConfirmacaoLinhaImportacaoDTO(row.id(), true, row.veiculoId(), row.profissionalId(), row.empresaId(), "CONCLUIDA"))
                .toList();
        mockMvc.perform(post("/api/viagem/importacoes/" + initial.id() + "/confirmar")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmacaoImportacaoDTO(false, initialDecisions))))
                .andExpect(status().isOk());
        ImportacaoViagemDTO batch = preview(token, workbook(2));

        List<ConfirmacaoLinhaImportacaoDTO> duplicateDecisions = batch.linhas().stream()
                .map(row -> new ConfirmacaoLinhaImportacaoDTO(row.id(), true, row.veiculoId(), row.profissionalId(), row.empresaId(), "CONCLUIDA"))
                .toList();
        mockMvc.perform(post("/api/viagem/importacoes/" + batch.id() + "/confirmar")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmacaoImportacaoDTO(false, duplicateDecisions))))
                .andExpect(status().isBadRequest());
        assertThat(viagemRepository.count()).isEqualTo(2);

        List<ConfirmacaoLinhaImportacaoDTO> foreignDecision = new ArrayList<>();
        ImportacaoLinhaDTO first = batch.linhas().getFirst();
        ImportacaoLinhaDTO second = batch.linhas().get(1);
        foreignDecision.add(new ConfirmacaoLinhaImportacaoDTO(first.id(), true, first.veiculoId(), first.profissionalId(), first.empresaId(), "CONCLUIDA"));
        foreignDecision.add(new ConfirmacaoLinhaImportacaoDTO(second.id(), true, second.veiculoId(), second.profissionalId(), foreignCatalog.empresa().getId(), "CONCLUIDA"));
        mockMvc.perform(post("/api/viagem/importacoes/" + batch.id() + "/confirmar")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmacaoImportacaoDTO(true, foreignDecision))))
                .andExpect(status().isForbidden());
        assertThat(viagemRepository.count()).isEqualTo(2);
    }

    @Test
    void blankTransportadoraCanBeImportedWithoutAnEmpresa() throws Exception {
        Usuario owner = createUser("import-company@example.com");
        Catalog catalog = createCatalog(owner);
        String token = token(owner);
        ImportacaoViagemDTO batch = preview(token, workbook(1, true));
        ImportacaoLinhaDTO row = batch.linhas().getFirst();
        assertThat(row.situacao()).isEqualTo("NOVA");
        assertThat(row.selecionada()).isTrue();
        assertThat(row.empresaId()).isNull();
        assertThat(row.avisos()).anyMatch(warning -> warning.contains("Empresa não informada"));

        ConfirmacaoLinhaImportacaoDTO decision = new ConfirmacaoLinhaImportacaoDTO(
                row.id(), true, row.veiculoId(), row.profissionalId(), null, "CONCLUIDA");
        mockMvc.perform(post("/api/viagem/importacoes/" + batch.id() + "/confirmar")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmacaoImportacaoDTO(false, List.of(decision)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.criadas").value(1));
        Viagem viagem = viagemRepository.findByProfissional_Usuario_Id(catalog.profissional().getUsuario().getId()).getFirst();
        assertThat(viagem.getEmpresa()).isNull();
    }

    private ImportacaoViagemDTO preview(String token, MockMultipartFile file) throws Exception {
        var result = mockMvc.perform(multipart("/api/viagem/importacoes/preview").file(file).header("Authorization", token))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), ImportacaoViagemDTO.class);
    }

    private MockMultipartFile workbook(int count) throws Exception {
        return workbook(count, false);
    }

    private MockMultipartFile workbook(int count, boolean blankCompany) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet cadastro = workbook.createSheet("Cadastro");
            cadastro.createRow(0).createCell(0).setCellValue("Comissão");
            cadastro.getRow(0).createCell(1).setCellValue("15%");
            Sheet sheet = workbook.createSheet("Lançamentos");
            String[] headers = {"Data", "Placa Cavalo", "Transportadora", "Destino 1", "Destino 2", "Destino 3",
                    "Preço Frete", "Abastecimento", "Pedágio", "Outras Despesas", "Descrição Outras"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            for (int i = 0; i < count; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue("12/09/2026");
                row.createCell(1).setCellValue("JOZ9C78");
                row.createCell(2).setCellValue(blankCompany ? "" : "MULTILOG");
                row.createCell(3).setCellValue(i < 2 ? "Santos" : "Campinas");
                row.createCell(4).setCellValue(i < 2 ? "" : "Sorocaba");
                row.createCell(5).setCellValue(i < 2 ? "Santos" : "Ribeirão Preto");
                row.createCell(6).setCellValue(i < 2 ? "400,00" : "750,50");
                row.createCell(7).setCellValue(i < 2 ? "" : "3.826,77");
                row.createCell(8).setCellValue(i < 2 ? "" : "316,20");
                row.createCell(9).setCellValue(i < 2 ? "" : "22,62");
                row.createCell(10).setCellValue(i < 2 ? "" : "Reparo");
            }
            workbook.write(out);
            return new MockMultipartFile("file", "incremental.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }

    private Catalog createCatalog(Usuario user) {
        Profissional driver = new Profissional();
        driver.setNome("Magno");
        driver.setStatus(true);
        driver.setUsuario(user);
        driver = profissionalRepository.save(driver);
        Empresa company = new Empresa();
        company.setNome("MultiLog");
        company.setNomeContato("Contato");
        company.setUsuario(user);
        company = empresaRepository.save(company);
        Veiculo vehicle = new Veiculo();
        vehicle.setPlaca("JOZ9C78");
        vehicle.setMarca("Volvo");
        vehicle.setStatus(true);
        vehicle.setProfissional(driver);
        vehicle = veiculoRepository.save(vehicle);
        return new Catalog(driver, company, vehicle);
    }

    private Usuario createUser(String email) {
        Usuario user = new Usuario();
        user.setNome("Importador");
        user.setEmail(email);
        user.setSenha(passwordEncoder.encode("SenhaAtual123"));
        return usuarioRepository.save(user);
    }

    private String token(Usuario user) {
        return "Bearer " + jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
    }

    private record Catalog(Profissional profissional, Empresa empresa, Veiculo veiculo) { }
}
