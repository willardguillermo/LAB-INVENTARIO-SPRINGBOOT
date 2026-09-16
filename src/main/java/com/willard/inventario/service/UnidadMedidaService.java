package com.willard.inventario.service;

import org.springframework.stereotype.Service;
import com.willard.inventario.models.UnidadMedida;
import com.willard.inventario.repository.UnidadMedidaRepository;
import org.springframework.http.HttpStatus;
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
    public UnidadMedida registrar(UnidadMedida unidadMedida) {
        unidadMedida.setId(null);
        return unidadMedidaRepository.save(unidadMedida);
    }
}
