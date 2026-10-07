package com.willard.inventario.controller;

import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class SesionController {

    private final UsuarioRepository usuarioRepository;

    public SesionController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Datos del usuario autenticado, para que el frontend muestre quién es y adapte el menú a su rol.
    @GetMapping("/api/sesion")
    public Map<String, String> sesion(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("username", usuario.getUsername());
        datos.put("nombre", usuario.getNombre());
        datos.put("rol", usuario.getRol().getNombre());
        return datos;
    }
}
