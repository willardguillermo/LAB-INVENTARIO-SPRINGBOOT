package com.willard.inventario.service;

import com.willard.inventario.aop.Auditable;
import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    @Transactional
    @Auditable(entidad = "Usuario", operacion = "REGISTRAR")
    public Usuario registrar(Usuario usuario) {
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe asignar un rol");
        }
        Rol rol = rolService.obtenerPorId(usuario.getRol().getId());
        usuario.setId(null);
        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @Auditable(entidad = "Usuario", operacion = "MODIFICAR")
    public Usuario modificar(Long id, Usuario datos) {
        Usuario usuario = obtenerPorId(id);
        usuario.setNombre(datos.getNombre());
        usuario.setUsername(datos.getUsername());
        if (datos.getRol() != null && datos.getRol().getId() != null) {
            usuario.setRol(rolService.obtenerPorId(datos.getRol().getId()));
        }
        // Solo se cambia la clave si se escribió una nueva
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @Auditable(entidad = "Usuario", operacion = "MODIFICAR", detalle = "Cambio de estado")
    public Usuario cambiarEstado(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setEstado(!usuario.getEstado());
        return usuarioRepository.save(usuario);
    }
}