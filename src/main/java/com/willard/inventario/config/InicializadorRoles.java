package com.willard.inventario.config;

import com.willard.inventario.models.Rol;
import com.willard.inventario.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Crea los roles base al arrancar la aplicación (solo si no existen).
@Component
public class InicializadorRoles implements CommandLineRunner {

    private final RolRepository rolRepository;

    public InicializadorRoles(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) {
        for (String nombre : List.of("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA")) {
            if (!rolRepository.existsByNombre(nombre)) {
                Rol rol = new Rol();
                rol.setNombre(nombre);
                rolRepository.save(rol);
            }
        }
    }
}