package com.willard.inventario.config;

import com.willard.inventario.models.Rol;
import com.willard.inventario.models.Usuario;
import com.willard.inventario.repository.RolRepository;
import com.willard.inventario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InicializadorRoles implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public InicializadorRoles(RolRepository rolRepository,
                              UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Crear los roles base
        for (String nombre : List.of("ADMINISTRADOR", "MEDICO", "RECEPCIONISTA")) {
            if (!rolRepository.existsByNombre(nombre)) {
                Rol rol = new Rol();
                rol.setNombre(nombre);
                rol.setEstado(true);
                rolRepository.save(rol);
            }
        }

        // 2. Crear usuario admin por defecto si no existe
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            rolRepository.findByNombre("ADMINISTRADOR").ifPresent(rolAdmin -> {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador del Sistema");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol(rolAdmin);
                admin.setEstado(true);
                usuarioRepository.save(admin);
            });
        }
    }
}