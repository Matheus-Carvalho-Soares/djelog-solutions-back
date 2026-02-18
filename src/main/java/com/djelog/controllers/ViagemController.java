package com.djelog.controllers;

import com.djelog.dtos.ViagemDTO;
import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.EmpresaDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.entities.Viagem;
import com.djelog.repositories.ViagemRepository;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.services.UsuarioService;
import com.djelog.services.ViagemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagem")
@CrossOrigin(origins = "*")
public class ViagemController {

    private final ViagemRepository viagemRepository;
    private final UsuarioService usuarioService;
    private final ViagemService viagemService;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final VeiculoRepository veiculoRepository;

    public ViagemController(ViagemRepository viagemRepository, UsuarioService usuarioService, ViagemService viagemService, EmpresaRepository empresaRepository, ProfissionalRepository profissionalRepository, VeiculoRepository veiculoRepository) {
        this.viagemRepository = viagemRepository;
        this.usuarioService = usuarioService;
        this.viagemService = viagemService;
        this.empresaRepository = empresaRepository;
        this.profissionalRepository = profissionalRepository;
        this.veiculoRepository = veiculoRepository;
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return null;
        }
        return usuarioService.findByEmail(email).getId();
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ViagemDTO>> findAll(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        UUID usuarioId = usuarioService.findByEmail(email).getId();
        List<Viagem> viagens = viagemRepository.findByProfissional_Usuario_Id(usuarioId);
        List<ViagemDTO> viagensDTO = viagens.stream().map(this::convertToDTO).toList();
        return viagensDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagensDTO);
    }

    @GetMapping("/excel/dados")
    public ResponseEntity<List<ViagemRelatorioDTO>> findDadosByDataInicioFim(
            Authentication authentication,
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (dataInicio == null || dataFim == null) {
            return ResponseEntity.badRequest().build();
        }
        if (dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest().build();
        }

        String email = authentication.getName();
        UUID usuarioId = usuarioService.findByEmail(email).getId();

        List<ViagemRelatorioDTO> dados = viagemService.findDadosByDataInicioFim(usuarioId, dataInicio, dataFim);
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemDTO> findById(@PathVariable("id") UUID id) {
        return viagemRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ViagemDTO> create(@RequestBody Viagem viagem, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Validate that empresa belongs to the authenticated user
        if (viagem.getEmpresa() == null || viagem.getEmpresa().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!empresaRepository.existsByIdAndUsuario_Id(viagem.getEmpresa().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate that profissional belongs to the authenticated user
        if (viagem.getProfissional() == null || viagem.getProfissional().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!profissionalRepository.existsByIdAndUsuario_Id(viagem.getProfissional().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate that veiculo belongs to the authenticated user
        if (viagem.getVeiculo() == null || viagem.getVeiculo().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!veiculoRepository.existsByIdAndProfissional_Usuario_Id(viagem.getVeiculo().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Viagem saved = viagemRepository.save(viagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViagemDTO> update(@PathVariable("id") UUID id, @RequestBody Viagem viagem, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Validate that empresa belongs to the authenticated user
        if (viagem.getEmpresa() == null || viagem.getEmpresa().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!empresaRepository.existsByIdAndUsuario_Id(viagem.getEmpresa().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate that profissional belongs to the authenticated user
        if (viagem.getProfissional() == null || viagem.getProfissional().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!profissionalRepository.existsByIdAndUsuario_Id(viagem.getProfissional().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate that veiculo belongs to the authenticated user
        if (viagem.getVeiculo() == null || viagem.getVeiculo().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!veiculoRepository.existsByIdAndProfissional_Usuario_Id(viagem.getVeiculo().getId(), usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return viagemRepository.findById(id)
                .map(existing -> {
                    existing.setProfissional(viagem.getProfissional());
                    existing.setEmpresa(viagem.getEmpresa());
                    existing.setVeiculo(viagem.getVeiculo());
                    existing.setInicioFrete(viagem.getInicioFrete());
                    existing.setFimFrete(viagem.getFimFrete());
                    existing.setValorFrete(viagem.getValorFrete());
                    existing.setComissao(viagem.getComissao());
                    existing.setDataInicio(viagem.getDataInicio());
                    existing.setDataFim(viagem.getDataFim());
                    existing.setStatus(viagem.getStatus());
                    Viagem saved = viagemRepository.save(existing);
                    return ResponseEntity.ok(convertToDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (!viagemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        viagemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ViagemDTO convertToDTO(Viagem viagem) {
        ViagemDTO dto = new ViagemDTO();
        dto.setId(viagem.getId());
        dto.setInicioFrete(viagem.getInicioFrete());
        dto.setFimFrete(viagem.getFimFrete());
        dto.setValorFrete(viagem.getValorFrete());
        dto.setComissao(viagem.getComissao());
        dto.setDataInicio(viagem.getDataInicio());
        dto.setDataFim(viagem.getDataFim());
        dto.setStatus(viagem.getStatus());
        
        if (viagem.getProfissional() != null) {
            ProfissionalDTO profissionalDTO = new ProfissionalDTO();
            profissionalDTO.setId(viagem.getProfissional().getId());
            profissionalDTO.setNome(viagem.getProfissional().getNome());
            profissionalDTO.setTelefone(viagem.getProfissional().getTelefone());
            profissionalDTO.setStatus(viagem.getProfissional().getStatus());
            dto.setProfissional(profissionalDTO);
        }
        
        if (viagem.getEmpresa() != null) {
            EmpresaDTO empresaDTO = new EmpresaDTO();
            empresaDTO.setId(viagem.getEmpresa().getId());
            empresaDTO.setNome(viagem.getEmpresa().getNome());
            empresaDTO.setDescricao(viagem.getEmpresa().getDescricao());
            empresaDTO.setNomeContato(viagem.getEmpresa().getNomeContato());
            empresaDTO.setTelefoneContato(viagem.getEmpresa().getTelefoneContato());
            empresaDTO.setEmailContato(viagem.getEmpresa().getEmailContato());
            dto.setEmpresa(empresaDTO);
        }
        
        if (viagem.getVeiculo() != null) {
            VeiculoDTO veiculoDTO = new VeiculoDTO();
            veiculoDTO.setId(viagem.getVeiculo().getId());
            veiculoDTO.setMarca(viagem.getVeiculo().getMarca());
            veiculoDTO.setAno(viagem.getVeiculo().getAno());
            veiculoDTO.setPlaca(viagem.getVeiculo().getPlaca());
            veiculoDTO.setNome(viagem.getVeiculo().getNome());
            veiculoDTO.setQtdPeso(viagem.getVeiculo().getQtdPeso());
            veiculoDTO.setStatus(viagem.getVeiculo().getStatus());
            dto.setVeiculo(veiculoDTO);
        }
        
        return dto;
    }
}
