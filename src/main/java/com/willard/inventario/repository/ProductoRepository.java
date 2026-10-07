package com.willard.inventario.repository;

import com.willard.inventario.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// JpaSpecificationExecutor: RF-INV-14, búsqueda con filtros combinables (ver ProductoService)
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long>,
        JpaSpecificationExecutor<ProductoEntity> {

    // Trae las relaciones en la misma consulta (evita N+1 y no depende de open-in-view).
    // LEFT JOIN explícito: como categoría y unidad tienen @NotNull, Hibernate usaría INNER JOIN
    // y ocultaría productos antiguos que aún no tienen categoría o unidad asignada.
    @Query("""
            select p from ProductoEntity p
            left join fetch p.categoria
            left join fetch p.unidadMedida
            left join fetch p.proveedor
            where p.id = :id
            """)
    Optional<ProductoEntity> buscarConRelacionesPorId(@Param("id") Long id);

    // Usadas por el módulo de Proveedor (validar si tiene productos asociados antes de desactivar).
    List<ProductoEntity> findByProveedorId(Long proveedorId);

    long countByProveedorId(Long proveedorId);
}
