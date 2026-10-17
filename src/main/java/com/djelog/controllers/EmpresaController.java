package com.djelog.controllers;

import com.djelog.dtos.EmpresaDTO;
import com.djelog.entities.Empresa;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.services.CurrentUserService;
import com.djelog.services.EmpresaService;
import com.djelog.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final CurrentUserService currentUserService;
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaRepository empresaRepository, UsuarioService usuarioService, CurrentUserService currentUserService, EmpresaService empresaService) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
        this.currentUserService = currentUserService;
        this.empresaService = empresaService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<EmpresaDTO>> findAll() {
        UUID usuarioId = currentUserService.getCurrentUserId();
        List<Empresa> empresas = empresaRepository.findByUsuario_Id(usuarioId);
        List<EmpresaDTO> empresasDTO = empresas.stream().map(this::convertToDTO).toList();
        return empresasDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(empresasDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDTO> findById(@PathVariable("id") UUID id) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return empresaRepository.findByIdAndUsuario_Id(id, usuarioId)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmpresaDTO> create(@Valid @RequestBody Empresa empresa) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        empresa.setUsuario(usuarioService.findById(usuarioId));
        Empresa saved = empresaRepository.save(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDTO> update(@PathVariable(name = "id") UUID id, @Valid @RequestBody Empresa empresa) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return empresaRepository.findByIdAndUsuario_Id(id, usuarioId)
                .map(existing -> {
                    existing.setNome(empresa.getNome());
                    existing.setDescricao(empresa.getDescricao());
                    existing.setNomeContato(empresa.getNomeContato());
                    existing.setTelefoneContato(empresa.getTelefoneContato());
                    existing.setEmailContato(empresa.getEmailContato());
                    existing.setUsuario(usuarioService.findById(usuarioId));
                    Empresa saved = empresaRepository.save(existing);
                    return ResponseEntity.ok(convertToDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        return empresaService.delete(id, currentUserService.getCurrentUserId())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private EmpresaDTO convertToDTO(Empresa empresa) {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setId(empresa.getId());
        dto.setNome(empresa.getNome());
        dto.setDescricao(empresa.getDescricao());
        dto.setNomeContato(empresa.getNomeContato());
        dto.setTelefoneContato(empresa.getTelefoneContato());
        dto.setEmailContato(empresa.getEmailContato());
        return dto;
    }
}
