package com.willard.inventario.repository;

import com.willard.inventario.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    boolean existsByNombre(String nombre);
    Optional<Rol> findByNombre(String nombre);
}