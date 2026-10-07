package com.willard.inventario.repository;

import com.willard.inventario.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    // Para no permitir desactivar al último ADMINISTRADOR activo
    long countByRol_NombreAndEstado(String nombreRol, Boolean estado);

}