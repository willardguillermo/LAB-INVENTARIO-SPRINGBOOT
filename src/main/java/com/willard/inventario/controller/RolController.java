package com.willard.inventario.controller;

import com.willard.inventario.models.Rol;
import com.willard.inventario.service.RolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<Rol> listar() {
        return rolService.listar();
    }

    @GetMapping("/{id}")
    public Rol obtenerPorId(@PathVariable Long id) {
        return rolService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Rol> registrar(@RequestBody Rol rol) {
        return new ResponseEntity<>(rolService.registrar(rol), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Rol modificar(@PathVariable Long id, @RequestBody Rol rol) {
        return rolService.modificar(id, rol);
    }

    @PatchMapping("/{id}/estado")
    public Rol cambiarEstado(@PathVariable Long id) {
        return rolService.cambiarEstado(id);
    }
}