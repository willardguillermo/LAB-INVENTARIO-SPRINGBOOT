package com.willard.inventario.service;

import com.willard.inventario.aop.Auditable;
import com.willard.inventario.models.UnidadMedida;
import com.willard.inventario.repository.UnidadMedidaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    public UnidadMedidaService(UnidadMedidaRepository unidadMedidaRepository) {
        this.unidadMedidaRepository = unidadMedidaRepository;
    }

    public List<UnidadMedida> listar() {
        return unidadMedidaRepository.findAll();
    }

    public UnidadMedida obtenerPorId(Long id) {
        return unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad de medida no encontrada"));
    }

    // RF-INV-17: Registrar unidades de medida
    @Transactional
    @Auditable(entidad = "UnidadMedida", operacion = "REGISTRAR")
    public UnidadMedida registrar(UnidadMedida unidadMedida) {
        unidadMedida.setId(null);
        if (unidadMedida.getEstado() == null) {
            unidadMedida.setEstado(true);
        }
        return unidadMedidaRepository.save(unidadMedida);
    }
}