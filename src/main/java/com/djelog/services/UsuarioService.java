package com.djelog.services;

import com.djelog.dtos.UsuarioDTO;
import com.djelog.dtos.UsuarioUpdateDTO;
import com.djelog.entities.Usuario;
import com.djelog.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
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
            throw new IllegalArgumentException("Email ja cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        
        return usuarioRepository.save(usuario);
    }

    public Usuario usuarioDtoToResponse(Usuario usuarioDTO){
        Usuario userWithoutPassword = new Usuario();
        userWithoutPassword.setId(usuarioDTO.getId());
        userWithoutPassword.setNome(usuarioDTO.getNome());
        userWithoutPassword.setEmail(usuarioDTO.getEmail());

        return userWithoutPassword;
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado."));
    }

    public Usuario findById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado."));
    }

    public Usuario buscarPorId(UUID id) {
        return findById(id);
    }

    public Usuario atualizarUsuario(UUID id, UsuarioDTO usuarioDTO) {
        Usuario usuario = findById(id);

        if (usuarioDTO.getNome() != null) {
            usuario.setNome(usuarioDTO.getNome());
        }

        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new IllegalArgumentException("Email ja cadastrado.");
            }
            usuario.setEmail(usuarioDTO.getEmail());
        }

        if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario atualizarPerfil(UUID id, UsuarioUpdateDTO usuarioDTO) {
        Usuario usuario = findById(id);

        if (usuarioDTO.getNome() != null) {
            usuario.setNome(usuarioDTO.getNome().trim());
        }

        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().equalsIgnoreCase(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new IllegalArgumentException("Email ja cadastrado.");
            }
            usuario.setEmail(usuarioDTO.getEmail().trim());
        }

        return usuarioRepository.save(usuario);
    }

    public void alterarSenha(UUID id, String senhaAtual, String novaSenha) {
        Usuario usuario = findById(id);
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual invalida.");
        }
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}

