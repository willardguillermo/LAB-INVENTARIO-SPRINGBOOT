package com.willard.inventario.service;

import com.willard.inventario.models.Rol;
import com.willard.inventario.repository.RolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public Rol obtenerPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    public Rol registrar(Rol rol) {
        rol.setId(null);
        return rolRepository.save(rol);
    }

    public Rol modificar(Long id, Rol datos) {
        Rol rol = obtenerPorId(id);
        rol.setNombre(datos.getNombre());
        return rolRepository.save(rol);
    }

    public Rol cambiarEstado(Long id) {
        Rol rol = obtenerPorId(id);
        rol.setEstado(!rol.getEstado());
        return rolRepository.save(rol);
    }
}