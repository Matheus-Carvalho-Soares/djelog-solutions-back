package com.djelog.services;

import com.djelog.dtos.UsuarioDTO;
import com.djelog.entities.Usuario;
import com.djelog.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario criarUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        usuario.setCargo(usuarioDTO.getCargo());
        
        return usuarioRepository.save(usuario);
    }

    public Usuario usuarioDtoToResponse(Usuario usuarioDTO){
        Usuario userWithoutPassword = new Usuario();
        userWithoutPassword.setId(usuarioDTO.getId());
        userWithoutPassword.setNome(usuarioDTO.getNome());
        userWithoutPassword.setEmail(usuarioDTO.getEmail());
        userWithoutPassword.setCargo(usuarioDTO.getCargo());

        return userWithoutPassword;
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario findById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario buscarPorId(UUID id) {
        return findById(id);
    }
}

