package com.willard.inventario.controller;

import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // RF-INV-01: Registrar producto (solo ADMINISTRADOR)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<ProductoEntity> registrarProducto(
            @Valid @RequestBody ProductoEntity producto) {

        ProductoEntity productoRegistrado =
                productoService.registrarProducto(producto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoRegistrado);
    }

    // RF-INV-02: Modificar producto (solo ADMINISTRADOR)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoEntity> modificarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoEntity producto) {

        ProductoEntity productoModificado =
                productoService.modificarProducto(id, producto);

        return ResponseEntity.ok(productoModificado);
    }

    // Listar todos (ADMINISTRADOR y MEDICO)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping
    public ResponseEntity<List<ProductoEntity>> listarProductos() {
        return ResponseEntity.ok(
                productoService.listarProductos()
        );
    }

    // Buscar por ID (ADMINISTRADOR y MEDICO)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoEntity> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productoService.buscarPorId(id)
        );
    }

    // RF-INV-14: Buscar por nombre (ADMINISTRADOR y MEDICO)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoEntity>> buscarPorNombre(
            @RequestParam String nombre) {

        return ResponseEntity.ok(
                productoService.buscarPorNombre(nombre)
        );
    }

    // RF-INV-14: Filtrar por tipo (ADMINISTRADOR y MEDICO)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping("/filtrar/tipo")
    public ResponseEntity<List<ProductoEntity>> filtrarPorTipo(
            @RequestParam String tipoProducto) {

        return ResponseEntity.ok(
                productoService.filtrarPorTipo(tipoProducto)
        );
    }

    // RF-INV-14: Filtrar por estado (ADMINISTRADOR y MEDICO)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping("/filtrar/estado")
    public ResponseEntity<List<ProductoEntity>> filtrarPorEstado(
            @RequestParam Boolean activo) {

        return ResponseEntity.ok(
                productoService.filtrarPorEstado(activo)
        );
    }

    // RF-INV-14: Buscar por nombre y tipo (ADMINISTRADOR y MEDICO)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping("/filtrar")
    public ResponseEntity<List<ProductoEntity>> buscarPorNombreYTipo(
            @RequestParam String nombre,
            @RequestParam String tipoProducto) {

        return ResponseEntity.ok(
                productoService.buscarPorNombreYTipo(
                        nombre,
                        tipoProducto
                )
        );
    }
}