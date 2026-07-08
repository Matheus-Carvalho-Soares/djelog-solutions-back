package com.djelog.controllers;

import com.djelog.dtos.LoginRequest;
import com.djelog.dtos.LoginResponse;
import com.djelog.dtos.SenhaUpdateDTO;
import com.djelog.dtos.UsuarioDTO;
import com.djelog.dtos.UsuarioUpdateDTO;
import com.djelog.entities.Usuario;
import com.djelog.services.CurrentUserService;
import com.djelog.services.JwtService;
import com.djelog.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final CurrentUserService currentUserService;

    public UsuarioController(
            UsuarioService usuarioService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            CurrentUserService currentUserService
    ) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioService.criarUsuario(usuarioDTO);

        Usuario userResponse = usuarioService.usuarioDtoToResponse(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String email = userDetails.getUsername();
            Usuario usuario = usuarioService.findByEmail(email);
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(true, token, "Usuário autenticado com sucesso.", usuario.getEmail(), usuario.getNome(), usuario.getId())); // Add userId here
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Email e/ou senha inválidas.", null, null, null));
        }
    }

    @GetMapping("/usuario/me")
    public ResponseEntity<Usuario> buscarUsuarioAtual() {
        Usuario userResponse = usuarioService.usuarioDtoToResponse(currentUserService.getCurrentUser());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/usuario/me")
    public ResponseEntity<Usuario> atualizarUsuarioAtual(@Valid @RequestBody UsuarioUpdateDTO usuarioDTO) {
        Usuario usuarioAtualizado = usuarioService.atualizarPerfil(currentUserService.getCurrentUserId(), usuarioDTO);
        Usuario userResponse = usuarioService.usuarioDtoToResponse(usuarioAtualizado);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/usuario/me/senha")
    public ResponseEntity<Void> alterarSenhaAtual(@Valid @RequestBody SenhaUpdateDTO senhaDTO) {
        usuarioService.alterarSenha(currentUserService.getCurrentUserId(), senhaDTO.getSenhaAtual(), senhaDTO.getNovaSenha());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuario/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable("id") UUID id, @Valid @RequestBody UsuarioUpdateDTO usuarioDTO) {
        if (!currentUserService.getCurrentUserId().equals(id)) {
            throw new AccessDeniedException("Usuário não autorizado");
        }
        Usuario usuarioAtualizado = usuarioService.atualizarPerfil(id, usuarioDTO);
        Usuario userResponse = usuarioService.usuarioDtoToResponse(usuarioAtualizado);
        return ResponseEntity.ok(userResponse);
    }
}
