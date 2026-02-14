package com.djelog.security;

import com.djelog.entities.Usuario;
import com.djelog.repositories.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailWithCargo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String role = "ROLE_USER";
        if (usuario.getCargo() != null && usuario.getCargo().getNome() != null && !usuario.getCargo().getNome().isBlank()) {
            role = "ROLE_" + usuario.getCargo().getNome().trim().toUpperCase();
        }

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
