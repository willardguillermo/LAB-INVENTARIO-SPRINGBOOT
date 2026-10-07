package com.willard.inventario.service;

import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolService rolService) {
        this.usuarioRepository = usuarioRepository;
        this.rolService = rolService;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario registrar(Usuario usuario) {
        Rol rol = rolService.obtenerPorId(usuario.getRol().getId());
        usuario.setId(null);
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    public Usuario modificar(Long id, Usuario datos) {
        Usuario usuario = obtenerPorId(id);
        usuario.setNombre(datos.getNombre());
        usuario.setUsername(datos.getUsername());
        if (datos.getRol() != null) {
            usuario.setRol(rolService.obtenerPorId(datos.getRol().getId()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarEstado(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(!usuario.getEstado());
        return usuarioRepository.save(usuario);
    }
}