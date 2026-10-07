package com.willard.inventario.repository;

import com.willard.inventario.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
    boolean existsByNombre(String nombre);
}