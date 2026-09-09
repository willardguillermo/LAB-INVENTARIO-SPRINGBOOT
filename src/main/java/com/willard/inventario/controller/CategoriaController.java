package com.willard.inventario.controller;

import com.willard.inventario.models.Categoria;
import com.willard.inventario.service.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    public Categoria obtenerPorId(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Categoria> registrar(@RequestBody Categoria categoria) {
        return new ResponseEntity<>(categoriaService.registrar(categoria), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Categoria modificar(@PathVariable Long id, @RequestBody Categoria categoria) {
        return categoriaService.modificar(id, categoria);
    }
}