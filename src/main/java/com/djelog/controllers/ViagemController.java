package com.djelog.controllers;

import com.djelog.dtos.ViagemDTO;
import com.djelog.dtos.ViagemRelatorioDTO;
import com.djelog.dtos.ProfissionalDTO;
import com.djelog.dtos.EmpresaDTO;
import com.djelog.dtos.VeiculoDTO;
import com.djelog.entities.Empresa;
import com.djelog.entities.Profissional;
import com.djelog.entities.Veiculo;
import com.djelog.entities.Viagem;
import com.djelog.repositories.ViagemRepository;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.repositories.ProfissionalRepository;
import com.djelog.repositories.VeiculoRepository;
import com.djelog.services.CurrentUserService;
import com.djelog.services.UsuarioService;
import com.djelog.services.ViagemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagem")
public class ViagemController {

    private final ViagemRepository viagemRepository;
    private final UsuarioService usuarioService;
    private final ViagemService viagemService;
    private final EmpresaRepository empresaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final VeiculoRepository veiculoRepository;
    private final CurrentUserService currentUserService;

    public ViagemController(ViagemRepository viagemRepository, UsuarioService usuarioService, ViagemService viagemService, EmpresaRepository empresaRepository, ProfissionalRepository profissionalRepository, VeiculoRepository veiculoRepository, CurrentUserService currentUserService) {
        this.viagemRepository = viagemRepository;
        this.usuarioService = usuarioService;
        this.viagemService = viagemService;
        this.empresaRepository = empresaRepository;
        this.profissionalRepository = profissionalRepository;
        this.veiculoRepository = veiculoRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ViagemDTO>> findAll() {
        UUID usuarioId = currentUserService.getCurrentUserId();
        List<Viagem> viagens = viagemRepository.findByProfissional_Usuario_Id(usuarioId);
        List<ViagemDTO> viagensDTO = viagens.stream().map(this::convertToDTO).toList();
        return viagensDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(viagensDTO);
    }

    @GetMapping("/excel/dados")
    public ResponseEntity<List<ViagemRelatorioDTO>> findDadosByDataInicioFim(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Informe a data inicial e a data final.");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual a data final.");
        }
        UUID usuarioId = currentUserService.getCurrentUserId();

        List<ViagemRelatorioDTO> dados = viagemService.findDadosByDataInicioFim(usuarioId, dataInicio, dataFim);
        return dados.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemDTO> findById(@PathVariable("id") UUID id) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ViagemDTO> create(@Valid @RequestBody Viagem viagem) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        
        if (viagem.getEmpresa() == null || viagem.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("Selecione uma empresa para a viagem.");
        }
        Optional<Empresa> empresa = empresaRepository.findByIdAndUsuario_Id(viagem.getEmpresa().getId(), usuarioId);
        if (empresa.isEmpty()) {
            throw new AccessDeniedException("Empresa nao encontrada para este usuario.");
        }
        
        if (viagem.getProfissional() == null || viagem.getProfissional().getId() == null) {
            throw new IllegalArgumentException("Selecione um motorista para a viagem.");
        }
        Optional<Profissional> profissional = profissionalRepository.findByIdAndUsuario_Id(viagem.getProfissional().getId(), usuarioId);
        if (profissional.isEmpty()) {
            throw new AccessDeniedException("Motorista nao encontrado para este usuario.");
        }
        
        if (viagem.getVeiculo() == null || viagem.getVeiculo().getId() == null) {
            throw new IllegalArgumentException("Selecione um veiculo para a viagem.");
        }
        Optional<Veiculo> veiculo = veiculoRepository.findByIdAndProfissional_Usuario_Id(viagem.getVeiculo().getId(), usuarioId);
        if (veiculo.isEmpty()) {
            throw new AccessDeniedException("Veiculo nao encontrado para este usuario.");
        }

        viagem.setEmpresa(empresa.get());
        viagem.setProfissional(profissional.get());
        viagem.setVeiculo(veiculo.get());
        
        Viagem saved = viagemRepository.save(viagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViagemDTO> update(@PathVariable("id") UUID id, @Valid @RequestBody Viagem viagem) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        
        if (viagem.getEmpresa() == null || viagem.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("Selecione uma empresa para a viagem.");
        }
        Optional<Empresa> empresa = empresaRepository.findByIdAndUsuario_Id(viagem.getEmpresa().getId(), usuarioId);
        if (empresa.isEmpty()) {
            throw new AccessDeniedException("Empresa nao encontrada para este usuario.");
        }
        
        if (viagem.getProfissional() == null || viagem.getProfissional().getId() == null) {
            throw new IllegalArgumentException("Selecione um motorista para a viagem.");
        }
        Optional<Profissional> profissional = profissionalRepository.findByIdAndUsuario_Id(viagem.getProfissional().getId(), usuarioId);
        if (profissional.isEmpty()) {
            throw new AccessDeniedException("Motorista nao encontrado para este usuario.");
        }
        
        if (viagem.getVeiculo() == null || viagem.getVeiculo().getId() == null) {
            throw new IllegalArgumentException("Selecione um veiculo para a viagem.");
        }
        Optional<Veiculo> veiculo = veiculoRepository.findByIdAndProfissional_Usuario_Id(viagem.getVeiculo().getId(), usuarioId);
        if (veiculo.isEmpty()) {
            throw new AccessDeniedException("Veiculo nao encontrado para este usuario.");
        }
        
        return viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .map(existing -> {
                    existing.setProfissional(profissional.get());
                    existing.setEmpresa(empresa.get());
                    existing.setVeiculo(veiculo.get());
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
        UUID usuarioId = currentUserService.getCurrentUserId();
        Viagem viagem = viagemRepository.findByIdAndProfissional_Usuario_Id(id, usuarioId)
                .orElseThrow(NoSuchElementException::new);
        viagemRepository.delete(viagem);
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
