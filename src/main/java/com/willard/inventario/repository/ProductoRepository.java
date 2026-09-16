package com.willard.inventario.repository;

import com.willard.inventario.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByNombreContainingIgnoreCase(String nombre);

    List<ProductoEntity> findByTipoProductoIgnoreCase(String tipoProducto);

    List<ProductoEntity> findByActivo(Boolean activo);

    List<ProductoEntity> findByNombreContainingIgnoreCaseAndTipoProductoIgnoreCase(
            String nombre,
            String tipoProducto
    );
}