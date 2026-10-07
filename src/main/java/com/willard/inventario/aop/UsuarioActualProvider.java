package com.willard.inventario.aop;

import org.springframework.stereotype.Component;

// Devuelve el nombre del usuario que realiza la operación, para la auditoría.
@Component
public class UsuarioActualProvider {

    public static final String USUARIO_SISTEMA = "sistema";

    // TODO (P5 - Spring Security): mientras no exista la seguridad se registra "sistema".
    //  Cuando se agregue Spring Security, reemplazar el cuerpo por algo como:
    //
    //    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
    //        return USUARIO_SISTEMA;
    //    }
    //    return auth.getName();
    //
    //  Es el único cambio necesario: el aspecto llama a este método en el mismo hilo de la
    //  petición, así que el SecurityContext está disponible.
    public String obtenerUsuario() {
        return USUARIO_SISTEMA;
    }
}
