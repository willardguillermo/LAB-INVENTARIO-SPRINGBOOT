package com.willard.inventario.impl;

import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.model.Proveedor;
import com.willard.inventario.repository.ProductoRepository;
import com.willard.inventario.repository.ProveedorRepository;
import com.willard.inventario.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;

    @Override
    public Proveedor registrar(Proveedor proveedor) {
        proveedor.setId(null);
        return proveedorRepository.save(proveedor);
    }

    @Override
    public Proveedor modificar(Long id, Proveedor proveedor) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado con id " + id));

        existente.setRuc(proveedor.getRuc());
        existente.setRazonSocial(proveedor.getRazonSocial());
        existente.setContacto(proveedor.getContacto());
        existente.setTelefono(proveedor.getTelefono());
        existente.setEmail(proveedor.getEmail());
        existente.setDireccion(proveedor.getDireccion());
        existente.setEstado(proveedor.getEstado());

        return proveedorRepository.save(existente);
    }

    @Override
    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    @Override
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado con id " + id));
    }

    @Override
    public void eliminar(Long id) {
        Proveedor proveedor = buscarPorId(id);
        long cantidad = productoRepository.countByProveedorId(id);
        if (cantidad > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el proveedor: tiene " + cantidad
                            + " producto(s) asociado(s). Desactívelo en su lugar.");
        }
        proveedorRepository.delete(proveedor);
    }

    @Override
    public Proveedor cambiarEstado(Long id, String estado) {
        Proveedor proveedor = buscarPorId(id);
        proveedor.setEstado(estado);
        return proveedorRepository.save(proveedor);
    }

    @Override
    public List<ProductoEntity> listarProductos(Long id) {
        buscarPorId(id);
        return productoRepository.findByProveedorId(id);
    }
}