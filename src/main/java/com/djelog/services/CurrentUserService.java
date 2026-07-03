package com.djelog.services;

import com.djelog.entities.Usuario;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final UsuarioService usuarioService;

    public CurrentUserService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário não autenticado");
        }

        return usuarioService.findByEmail(authentication.getName());
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
