package com.willard.inventario.controller;

import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.model.Proveedor;
import com.willard.inventario.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    public Proveedor registrar(@Valid @RequestBody Proveedor proveedor) {
        return proveedorService.registrar(proveedor);
    }

    @PutMapping("/{id}")
    public Proveedor modificar(@PathVariable Long id, @Valid @RequestBody Proveedor proveedor) {
        return proveedorService.modificar(id, proveedor);
    }

    @GetMapping
    public List<Proveedor> listar() {
        return proveedorService.listar();
    }

    @GetMapping("/{id}")
    public Proveedor buscarPorId(@PathVariable Long id) {
        return proveedorService.buscarPorId(id);
    }

    @GetMapping("/{id}/productos")
    public List<ProductoEntity> listarProductos(@PathVariable Long id) {
        return proveedorService.listarProductos(id);
    }

    @PatchMapping("/{id}/activar")
    public Proveedor activar(@PathVariable Long id) {
        return proveedorService.cambiarEstado(id, "ACTIVO");
    }

    @PatchMapping("/{id}/desactivar")
    public Proveedor desactivar(@PathVariable Long id) {
        return proveedorService.cambiarEstado(id, "INACTIVO");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
    }
}