package com.willard.inventario.aop;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Devuelve el nombre del usuario que realiza la operación, para la auditoría.
@Component
public class UsuarioActualProvider {

    public static final String USUARIO_SISTEMA = "sistema";

    public String obtenerUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return USUARIO_SISTEMA;
        }
        return auth.getName();
    }
}
