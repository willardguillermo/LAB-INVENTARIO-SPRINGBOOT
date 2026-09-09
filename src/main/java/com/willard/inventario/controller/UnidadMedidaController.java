package com.willard.inventario.controller;

import com.willard.inventario.models.UnidadMedida;
import com.willard.inventario.service.UnidadMedidaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")
public class UnidadMedidaController {

    private final UnidadMedidaService unidadMedidaService;

    public UnidadMedidaController(UnidadMedidaService unidadMedidaService) {
        this.unidadMedidaService = unidadMedidaService;
    }

    @GetMapping
    public List<UnidadMedida> listar() {
        return unidadMedidaService.listar();
    }

    @GetMapping("/{id}")
    public UnidadMedida obtenerPorId(@PathVariable Long id) {
        return unidadMedidaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<UnidadMedida> registrar(@RequestBody UnidadMedida unidadMedida) {
        return new ResponseEntity<>(unidadMedidaService.registrar(unidadMedida), HttpStatus.CREATED);
    }
}