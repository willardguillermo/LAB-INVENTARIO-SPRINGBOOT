package com.willard.inventario.service;

import com.willard.inventario.aop.Auditable;
import com.willard.inventario.models.Rol;
import com.willard.inventario.repository.RolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class RolService {

    // Roles que crea InicializadorRoles: no se pueden renombrar ni desactivar para no dejar
    // a la aplicación sin un rol con el que el control de acceso por rol pueda funcionar.
    private static final Set<String> ROLES_BASE = Set.of("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA");
    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}");

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public List<Rol> listarActivos() {
        return rolRepository.findAll().stream().filter(Rol::getEstado).toList();
    }

    public Rol obtenerPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    @Transactional
    @Auditable(entidad = "Rol", operacion = "REGISTRAR")
    public Rol registrar(Rol rol) {
        String nombre = normalizar(rol.getNombre());
        validarNoRepetido(nombre, null);

        rol.setId(null);
        rol.setNombre(nombre);
        return rolRepository.save(rol);
    }

    @Transactional
    @Auditable(entidad = "Rol", operacion = "MODIFICAR")
    public Rol modificar(Long id, Rol datos) {
        Rol rol = obtenerPorId(id);
        String nombreNuevo = normalizar(datos.getNombre());

        if (ROLES_BASE.contains(rol.getNombre()) && !rol.getNombre().equals(nombreNuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "'" + rol.getNombre() + "' es un rol base del sistema y no se puede renombrar");
        }
        validarNoRepetido(nombreNuevo, id);

        rol.setNombre(nombreNuevo);
        return rolRepository.save(rol);
    }

    @Transactional
    @Auditable(entidad = "Rol", operacion = "MODIFICAR", detalle = "Cambio de estado")
    public Rol cambiarEstado(Long id) {
        Rol rol = obtenerPorId(id);
        if (ROLES_BASE.contains(rol.getNombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "'" + rol.getNombre() + "' es un rol base del sistema y no se puede desactivar");
        }
        rol.setEstado(!rol.getEstado());
        return rolRepository.save(rol);
    }

    private void validarNoRepetido(String nombre, Long idPropio) {
        rolRepository.findByNombre(nombre).ifPresent(existente -> {
            if (idPropio == null || !existente.getId().equals(idPropio)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un rol llamado '" + nombre + "'");
            }
        });
    }

    // "Médico " -> "MEDICO": sin tildes, en mayúsculas y sin espacios en los extremos.
    private String normalizar(String nombre) {
        if (nombre == null) {
            return null;
        }
        String sinTildes = MARCAS_DIACRITICAS.matcher(Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD))
                .replaceAll("");
        return sinTildes.toUpperCase();
    }
}
