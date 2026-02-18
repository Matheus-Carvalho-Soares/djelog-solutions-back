package com.djelog.controllers;

import com.djelog.dtos.EmpresaDTO;
import com.djelog.entities.Empresa;
import com.djelog.repositories.EmpresaRepository;
import com.djelog.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresa")
@CrossOrigin(origins = "*")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;

    public EmpresaController(EmpresaRepository empresaRepository, UsuarioService usuarioService) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
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
    public ResponseEntity<List<EmpresaDTO>> findAll(Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Empresa> empresas = empresaRepository.findByUsuario_Id(usuarioId);
        List<EmpresaDTO> empresasDTO = empresas.stream().map(this::convertToDTO).toList();
        return empresasDTO.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(empresasDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDTO> findById(@PathVariable("id") UUID id) {
        return empresaRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmpresaDTO> create(@RequestBody Empresa empresa, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        empresa.setUsuario(usuarioService.findById(usuarioId));
        Empresa saved = empresaRepository.save(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDTO> update(@PathVariable(name = "id") UUID id, @RequestBody Empresa empresa, Authentication authentication) {
        UUID usuarioId = getAuthenticatedUserId(authentication);
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return empresaRepository.findById(id)
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
        if (!empresaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        empresaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
