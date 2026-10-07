package com.willard.inventario.controller;

import com.willard.inventario.models.Usuario;
import com.willard.inventario.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class SesionController {

    private final UsuarioService usuarioService;

    public SesionController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Datos del usuario autenticado, para que el frontend muestre quién es y adapte el menú a su rol.
    @GetMapping("/api/sesion")
    public Map<String, String> sesion() {
        Usuario usuario = usuarioService.obtenerUsuarioAutenticado();

        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("username", usuario.getUsername());
        datos.put("nombre", usuario.getNombre());
        datos.put("rol", usuario.getRol().getNombre());
        return datos;
    }
}
