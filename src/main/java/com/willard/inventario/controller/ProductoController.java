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

    // RF-INV-03: Activar producto (solo ADMINISTRADOR)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ProductoEntity> activarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.activarProducto(id));
    }

    // RF-INV-03: Desactivar producto (eliminación lógica, solo ADMINISTRADOR)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoEntity> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.desactivarProducto(id));
    }

    // RF-INV-14: Buscar y filtrar productos (ADMINISTRADOR y MEDICO).
    // Ej: GET /api/productos?nombre=para&tipo=medicamento&activo=true&categoriaId=1&proveedorId=2
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    @GetMapping
    public ResponseEntity<List<ProductoEntity>> buscarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long proveedorId) {

        return ResponseEntity.ok(
                productoService.buscarProductos(nombre, tipo, activo, categoriaId, proveedorId)
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
}
