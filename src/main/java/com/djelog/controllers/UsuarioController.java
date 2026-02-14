package com.djelog.controllers;

import com.djelog.dtos.LoginRequest;
import com.djelog.dtos.LoginResponse;
import com.djelog.dtos.UsuarioDTO;
import com.djelog.entities.Usuario;
import com.djelog.services.JwtService;
import com.djelog.services.UsuarioService;
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
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public UsuarioController(
            UsuarioService usuarioService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService
    ) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        try {
            Usuario usuario = usuarioService.criarUsuario(usuarioDTO);

            Usuario userResponse = usuarioService.usuarioDtoToResponse(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequest request) {
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
    @PutMapping("/usuario/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable("id") UUID id, @RequestBody UsuarioDTO usuarioDTO) {
        try {
            Usuario usuarioAtualizado = usuarioService.atualizarUsuario(id, usuarioDTO);
            Usuario userResponse = usuarioService.usuarioDtoToResponse(usuarioAtualizado);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
