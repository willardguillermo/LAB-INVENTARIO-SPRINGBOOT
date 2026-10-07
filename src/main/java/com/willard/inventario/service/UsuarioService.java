package com.willard.inventario.service;

import com.willard.inventario.aop.Auditable;
import com.willard.inventario.aop.UsuarioActualProvider;
import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActualProvider usuarioActualProvider;

    public UsuarioService(UsuarioRepository usuarioRepository, RolService rolService,
                           PasswordEncoder passwordEncoder, UsuarioActualProvider usuarioActualProvider) {
        this.usuarioRepository = usuarioRepository;
        this.rolService = rolService;
        this.passwordEncoder = passwordEncoder;
        this.usuarioActualProvider = usuarioActualProvider;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    // El usuario de la sesión activa; lo usan las plantillas Thymeleaf y GET /api/sesion.
    public Usuario obtenerUsuarioAutenticado() {
        String username = usuarioActualProvider.obtenerUsuario();
        return usuarioRepository.findByUsername(username)
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
        // Solo se cambia la clave si se escribió una nueva; en blanco conserva la actual
        // (el formulario de edición la deja vacía a propósito, nunca precarga el hash guardado).
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    @Auditable(entidad = "Usuario", operacion = "MODIFICAR", detalle = "Cambio de estado")
    public Usuario cambiarEstado(Long id) {
        Usuario usuario = obtenerPorId(id);

        if (usuario.getUsername().equals(usuarioActualProvider.obtenerUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No puedes cambiar el estado de tu propio usuario");
        }

        boolean vaADesactivarse = Boolean.TRUE.equals(usuario.getEstado());
        if (vaADesactivarse && esUltimoAdministradorActivo(usuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede desactivar al último administrador activo");
        }

        usuario.setEstado(!usuario.getEstado());
        return usuarioRepository.save(usuario);
    }

    private boolean esUltimoAdministradorActivo(Usuario usuario) {
        return usuario.getRol() != null
                && ROL_ADMINISTRADOR.equals(usuario.getRol().getNombre())
                && usuarioRepository.countByRol_NombreAndEstado(ROL_ADMINISTRADOR, true) <= 1;
    }
}
