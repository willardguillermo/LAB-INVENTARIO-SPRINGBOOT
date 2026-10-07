package com.willard.inventario.service;

import com.willard.inventario.aop.Auditable;
import com.willard.inventario.models.Categoria;
import com.willard.inventario.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    // RF-INV-15: Registrar categorías
    @Transactional
    @Auditable(entidad = "Categoria", operacion = "REGISTRAR")
    public Categoria registrar(Categoria categoria) {
        categoria.setId(null);
        if (categoria.getEstado() == null) {
            categoria.setEstado(true);
        }
        return categoriaRepository.save(categoria);
    }

    // RF-INV-16: Modificar categorías
    @Transactional
    @Auditable(entidad = "Categoria", operacion = "MODIFICAR")
    public Categoria modificar(Long id, Categoria datos) {
        Categoria categoria = obtenerPorId(id);
        categoria.setNombre(datos.getNombre());
        categoria.setDescripcion(datos.getDescripcion());
        if (datos.getEstado() != null) {
            categoria.setEstado(datos.getEstado());
        }
        return categoriaRepository.save(categoria);
    }
}