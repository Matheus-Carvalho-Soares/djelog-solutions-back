package com.djelog.solutions;

import com.djelog.entities.Despesa;
import com.djelog.entities.Empresa;
import com.djelog.entities.Estadia;
import com.djelog.entities.Profissional;
import com.djelog.entities.Usuario;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.DespesaRepository;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.repositories.EstadiaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.UsuarioRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.repositories.ViagemRepository;
import com.djelog.services.JwtService;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RelatorioIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private EstadiaRepository estadiaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ViagemRepository viagemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @BeforeEach
    void cleanDatabase() {
        despesaRepository.deleteAll();
        estadiaRepository.deleteAll();
        viagemRepository.deleteAll();
        veiculoRepository.deleteAll();
        empresaRepository.deleteAll();
        profissionalRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void relatorioPorDataSomaEstadiasNaReceitaELucro() throws Exception {
        Usuario usuario = createUser("relatorio-data@example.com");
        Viagem viagem = createTripWithCosts(usuario, "ABC1234", "Motorista A", 1000, 10, 50, 200);
        createTripWithCosts(createUser("outro-data@example.com"), "ZZZ9999", "Outro Motorista", 5000, 10, 0, 900);

        mockMvc.perform(get("/api/viagem/excel/dados")
                        .header("Authorization", bearerToken(usuario))
                        .param("dataInicio", "2026-07-01T00:00:00")
                        .param("dataFim", "2026-07-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].veiculoPlaca").value("ABC1234"))
                .andExpect(jsonPath("$[0].valorFrete").value(1000))
                .andExpect(jsonPath("$[0].totalEstadias").value(200))
                .andExpect(jsonPath("$[0].receitaTotal").value(1200))
                .andExpect(jsonPath("$[0].comissao").value(100))
                .andExpect(jsonPath("$[0].totalDespesas").value(50))
                .andExpect(jsonPath("$[0].lucroLiquido").value(1050));

        assertThat(viagem.getId()).isNotNull();
    }

    @Test
    void relatoriosAgrupadosRespeitamUsuarioEPeriodo() throws Exception {
        Usuario usuario = createUser("relatorio-grupo@example.com");
        Profissional profissional = createProfissional(usuario, "Motorista A");
        Empresa empresa = createEmpresa(usuario);
        Veiculo veiculo = createVeiculo(profissional, "ABC1234");
        createViagemWithCosts(profissional, empresa, veiculo, 1000, 10, 50, 200);
        createViagemWithCosts(profissional, empresa, veiculo, 500, 20, 25, 50);
        createTripWithCosts(createUser("outro-grupo@example.com"), "ABC1234", "Motorista A", 7000, 10, 0, 1000);

        mockMvc.perform(get("/api/viagem/excel/dados/por-veiculo")
                        .header("Authorization", bearerToken(usuario))
                        .param("dataInicio", "2026-07-01T00:00:00")
                        .param("dataFim", "2026-07-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].grupoNome").value("ABC1234"))
                .andExpect(jsonPath("$[0].quantidadeViagens").value(2))
                .andExpect(jsonPath("$[0].valorFrete").value(1500))
                .andExpect(jsonPath("$[0].totalEstadias").value(250))
                .andExpect(jsonPath("$[0].receitaTotal").value(1750))
                .andExpect(jsonPath("$[0].comissao").value(200))
                .andExpect(jsonPath("$[0].totalDespesas").value(75))
                .andExpect(jsonPath("$[0].lucroLiquido").value(1475));

        mockMvc.perform(get("/api/viagem/excel/dados/por-profissional")
                        .header("Authorization", bearerToken(usuario))
                        .param("dataInicio", "2026-07-01T00:00:00")
                        .param("dataFim", "2026-07-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].grupoNome").value("Motorista A"))
                .andExpect(jsonPath("$[0].quantidadeViagens").value(2))
                .andExpect(jsonPath("$[0].receitaTotal").value(1750));
    }

    @Test
    void relatorioExcelPorVeiculoRetornaXlsxComColunaDeEstadias() throws Exception {
        Usuario usuario = createUser("relatorio-excel@example.com");
        createTripWithCosts(usuario, "ABC1234", "Motorista A", 1000, 10, 50, 200);

        MvcResult result = mockMvc.perform(get("/api/excel/relatorio-por-veiculo")
                        .header("Authorization", bearerToken(usuario))
                        .param("dataInicio", "2026-07-01T00:00:00")
                        .param("dataFim", "2026-07-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("spreadsheetml.sheet")))
                .andExpect(header().string("Content-Disposition", containsString("relatorio-por-veiculo_20260701_20260731.xlsx")))
                .andReturn();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            assertThat(workbook.getSheetAt(0).getRow(0).getCell(4).getStringCellValue()).isEqualTo("Estadias");
            assertThat(workbook.getSheetAt(0).getRow(1).getCell(4).getNumericCellValue()).isEqualTo(200.0);
            assertThat(workbook.getSheetAt(0).getRow(1).getCell(5).getNumericCellValue()).isEqualTo(1200.0);
        }
    }

    private Usuario createUser(String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("SenhaAtual123"));
        return usuarioRepository.save(usuario);
    }

    private Viagem createTripWithCosts(
            Usuario usuario,
            String placa,
            String nomeMotorista,
            int valorFrete,
            int comissao,
            int despesa,
            int estadia
    ) {
        Profissional profissional = createProfissional(usuario, nomeMotorista);
        Empresa empresa = createEmpresa(usuario);
        Veiculo veiculo = createVeiculo(profissional, placa);
        return createViagemWithCosts(profissional, empresa, veiculo, valorFrete, comissao, despesa, estadia);
    }

    private Viagem createViagemWithCosts(
            Profissional profissional,
            Empresa empresa,
            Veiculo veiculo,
            int valorFrete,
            int comissao,
            int despesa,
            int estadia
    ) {
        Viagem viagem = createViagem(profissional, empresa, veiculo, valorFrete, comissao);

        if (despesa > 0) {
            Despesa despesaEntity = new Despesa();
            despesaEntity.setViagem(viagem);
            despesaEntity.setNome("Pedagio");
            despesaEntity.setDescricao("Despesa da viagem");
            despesaEntity.setValor(despesa);
            despesaRepository.save(despesaEntity);
        }

        if (estadia > 0) {
            Estadia estadiaEntity = new Estadia();
            estadiaEntity.setViagem(viagem);
            estadiaEntity.setDescricao("Estadia da viagem");
            estadiaEntity.setValor(estadia);
            estadiaRepository.save(estadiaEntity);
        }

        return viagem;
    }

    private Profissional createProfissional(Usuario usuario, String nome) {
        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setTelefone("11999999999");
        profissional.setStatus(true);
        profissional.setUsuario(usuario);
        return profissionalRepository.save(profissional);
    }

    private Empresa createEmpresa(Usuario usuario) {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa Teste");
        empresa.setNomeContato("Contato Teste");
        empresa.setEmailContato("contato@example.com");
        empresa.setUsuario(usuario);
        return empresaRepository.save(empresa);
    }

    private Veiculo createVeiculo(Profissional profissional, String placa) {
        Veiculo veiculo = new Veiculo();
        veiculo.setMarca("Volvo");
        veiculo.setPlaca(placa);
        veiculo.setStatus(true);
        veiculo.setProfissional(profissional);
        return veiculoRepository.save(veiculo);
    }

    private Viagem createViagem(
            Profissional profissional,
            Empresa empresa,
            Veiculo veiculo,
            int valorFrete,
            int comissao
    ) {
        Viagem viagem = new Viagem();
        viagem.setProfissional(profissional);
        viagem.setEmpresa(empresa);
        viagem.setVeiculo(veiculo);
        viagem.setInicioFrete("Origem");
        viagem.setFimFrete("Destino");
        viagem.setValorFrete(BigDecimal.valueOf(valorFrete));
        viagem.setComissao(BigDecimal.valueOf(comissao));
        viagem.setDataInicio(LocalDateTime.of(2026, 7, 5, 10, 0));
        viagem.setStatus("EM_ANDAMENTO");
        return viagemRepository.save(viagem);
    }

    private String bearerToken(Usuario usuario) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        return "Bearer " + jwtService.generateToken(userDetails);
    }
}
