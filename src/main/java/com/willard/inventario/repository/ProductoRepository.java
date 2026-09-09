package com.willard.inventario.repository;

import com.willard.inventario.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByNombreContainingIgnoreCase(String nombre);

    List<ProductoEntity> findByTipoProductoIgnoreCase(String tipoProducto);

    List<ProductoEntity> findByActivo(Boolean activo);

    List<ProductoEntity> findByNombreContainingIgnoreCaseAndTipoProductoIgnoreCase(
            String nombre,
            String tipoProducto
    );
}