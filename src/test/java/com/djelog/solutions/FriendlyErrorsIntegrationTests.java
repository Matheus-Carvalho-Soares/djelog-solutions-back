package com.djelog.solutions;

import com.djelog.entities.Empresa;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FriendlyErrorsIntegrationTests {

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
    void registerReturnsFieldErrorForShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Teste",
                                  "email": "usuario@example.com",
                                  "senha": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Revise os campos destacados."))
                .andExpect(jsonPath("$.fields.senha").value("Senha deve ter entre 10 e 128 caracteres."))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void registerReturnsFriendlyMessageForDuplicateEmail() throws Exception {
        createUser("duplicado@example.com", "SenhaAtual123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Outro Usuario",
                                  "email": "duplicado@example.com",
                                  "senha": "SenhaNova123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email ja cadastrado."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void empresaReturnsFieldErrorForInvalidContactEmail() throws Exception {
        Usuario usuario = createUser("empresa@example.com", "SenhaAtual123");

        mockMvc.perform(post("/api/empresa")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Empresa Teste",
                                  "nomeContato": "Contato",
                                  "emailContato": "email-invalido"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.emailContato").value("Informe um email de contato valido."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void passwordUpdateReturnsFriendlyMessageForWrongCurrentPassword() throws Exception {
        Usuario usuario = createUser("senha@example.com", "SenhaAtual123");

        mockMvc.perform(put("/api/auth/usuario/me/senha")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "senha-errada",
                                  "novaSenha": "NovaSenha123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Senha atual invalida."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void veiculoReturnsFriendlyMessageWhenDriverIsMissing() throws Exception {
        Usuario usuario = createUser("veiculo@example.com", "SenhaAtual123");

        mockMvc.perform(post("/api/veiculo")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "marca": "Volvo",
                                  "placa": "ABC1234",
                                  "status": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Selecione um motorista para o veiculo."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void viagemReturnsFriendlyMessageWhenEmpresaIsMissing() throws Exception {
        Usuario usuario = createUser("viagem@example.com", "SenhaAtual123");

        mockMvc.perform(post("/api/viagem")
                        .header("Authorization", bearerToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inicioFrete": "Origem",
                                  "valorFrete": 1000.00,
                                  "dataInicio": "2026-07-05T10:00:00",
                                  "status": "EM_ANDAMENTO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Selecione uma empresa para a viagem."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void viagemAndVeiculoReadsDoNotFailWithLazyAssociations() throws Exception {
        Usuario usuario = createUser("lazy@example.com", "SenhaAtual123");
        Profissional profissional = createProfissional(usuario);
        Empresa empresa = createEmpresa(usuario);
        Veiculo veiculo = createVeiculo(profissional);
        Viagem viagem = createViagem(profissional, empresa, veiculo);
        String token = bearerToken(usuario);

        mockMvc.perform(get("/api/veiculo/findAll").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].profissional.nome").value("Motorista Teste"));

        mockMvc.perform(get("/api/veiculo/{id}", veiculo.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissional.nome").value("Motorista Teste"));

        mockMvc.perform(get("/api/viagem/findAll").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].profissional.nome").value("Motorista Teste"))
                .andExpect(jsonPath("$[0].empresa.nome").value("Empresa Teste"))
                .andExpect(jsonPath("$[0].veiculo.placa").value("ABC1234"));

        mockMvc.perform(get("/api/viagem/{id}", viagem.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissional.nome").value("Motorista Teste"))
                .andExpect(jsonPath("$.empresa.nome").value("Empresa Teste"))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1234"));
    }

    @Test
    void despesaAndEstadiaCanBeCreatedWithOnlyTripId() throws Exception {
        Usuario usuario = createUser("trip-costs@example.com", "SenhaAtual123");
        Profissional profissional = createProfissional(usuario);
        Empresa empresa = createEmpresa(usuario);
        Veiculo veiculo = createVeiculo(profissional);
        Viagem viagem = createViagem(profissional, empresa, veiculo);
        String token = bearerToken(usuario);

        mockMvc.perform(post("/api/despesas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Pedagio",
                                  "descricao": "Pedagio da rota",
                                  "valor": 80,
                                  "viagem": { "id": "%s" }
                                }
                                """.formatted(viagem.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pedagio"))
                .andExpect(jsonPath("$.viagem.id").value(viagem.getId().toString()));

        mockMvc.perform(post("/api/estadias")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Pernoite",
                                  "valor": 120,
                                  "viagem": { "id": "%s" }
                                }
                                """.formatted(viagem.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Pernoite"))
                .andExpect(jsonPath("$.viagem.id").value(viagem.getId().toString()));
    }

    @Test
    void viagemAndVeiculoWritesDoNotFailWithLazyAssociations() throws Exception {
        Usuario usuario = createUser("lazy-writes@example.com", "SenhaAtual123");
        Profissional profissional = createProfissional(usuario);
        Empresa empresa = createEmpresa(usuario);
        Veiculo veiculo = createVeiculo(profissional);
        Viagem viagem = createViagem(profissional, empresa, veiculo);
        String token = bearerToken(usuario);

        mockMvc.perform(post("/api/veiculo")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "marca": "Scania",
                                  "placa": "XYZ1234",
                                  "status": true,
                                  "profissional": { "id": "%s" }
                                }
                                """.formatted(profissional.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profissional.nome").value("Motorista Teste"));

        mockMvc.perform(put("/api/veiculo/{id}", veiculo.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "marca": "Volvo Atualizado",
                                  "placa": "ABC1234",
                                  "status": true,
                                  "profissional": { "id": "%s" }
                                }
                                """.formatted(profissional.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissional.nome").value("Motorista Teste"));

        mockMvc.perform(post("/api/viagem")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inicioFrete": "Origem Nova",
                                  "fimFrete": "Destino Novo",
                                  "valorFrete": 1500.00,
                                  "comissao": 100.00,
                                  "dataInicio": "2026-07-06T10:00:00",
                                  "status": "EM_ANDAMENTO",
                                  "profissional": { "id": "%s" },
                                  "empresa": { "id": "%s" },
                                  "veiculo": { "id": "%s" }
                                }
                                """.formatted(profissional.getId(), empresa.getId(), veiculo.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profissional.nome").value("Motorista Teste"))
                .andExpect(jsonPath("$.empresa.nome").value("Empresa Teste"))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1234"));

        mockMvc.perform(put("/api/viagem/{id}", viagem.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inicioFrete": "Origem Atualizada",
                                  "fimFrete": "Destino Atualizado",
                                  "valorFrete": 1750.00,
                                  "comissao": 100.00,
                                  "dataInicio": "2026-07-07T10:00:00",
                                  "status": "EM_ANDAMENTO",
                                  "profissional": { "id": "%s" },
                                  "empresa": { "id": "%s" },
                                  "veiculo": { "id": "%s" }
                                }
                                """.formatted(profissional.getId(), empresa.getId(), veiculo.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissional.nome").value("Motorista Teste"))
                .andExpect(jsonPath("$.empresa.nome").value("Empresa Teste"))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1234"));
    }

    private Usuario createUser(String email, String password) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(password));
        return usuarioRepository.save(usuario);
    }

    private Profissional createProfissional(Usuario usuario) {
        Profissional profissional = new Profissional();
        profissional.setNome("Motorista Teste");
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

    private Veiculo createVeiculo(Profissional profissional) {
        Veiculo veiculo = new Veiculo();
        veiculo.setMarca("Volvo");
        veiculo.setPlaca("ABC1234");
        veiculo.setStatus(true);
        veiculo.setProfissional(profissional);
        return veiculoRepository.save(veiculo);
    }

    private Viagem createViagem(Profissional profissional, Empresa empresa, Veiculo veiculo) {
        Viagem viagem = new Viagem();
        viagem.setProfissional(profissional);
        viagem.setEmpresa(empresa);
        viagem.setVeiculo(veiculo);
        viagem.setInicioFrete("Origem");
        viagem.setFimFrete("Destino");
        viagem.setValorFrete(BigDecimal.valueOf(1000));
        viagem.setComissao(BigDecimal.valueOf(100));
        viagem.setDataInicio(LocalDateTime.of(2026, 7, 5, 10, 0));
        viagem.setStatus("EM_ANDAMENTO");
        return viagemRepository.save(viagem);
    }

    private String bearerToken(Usuario usuario) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        return "Bearer " + jwtService.generateToken(userDetails);
    }
}
