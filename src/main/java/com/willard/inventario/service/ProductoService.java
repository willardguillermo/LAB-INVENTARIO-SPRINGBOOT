package com.willard.inventario.service;

import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    // Inyección de dependencias por constructor
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // RF-INV-01: Registrar producto
    public ProductoEntity registrarProducto(ProductoEntity producto) {
        return productoRepository.save(producto);
    }

    // RF-INV-02: Modificar producto
    public ProductoEntity modificarProducto(Long id, ProductoEntity datosProducto) {

        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado con id: " + id));

        producto.setNombre(datosProducto.getNombre());
        producto.setDescripcion(datosProducto.getDescripcion());
        producto.setMarca(datosProducto.getMarca());
        producto.setFabricante(datosProducto.getFabricante());
        producto.setTipoProducto(datosProducto.getTipoProducto());

        producto.setStockMinimo(datosProducto.getStockMinimo());
        producto.setStockMaximo(datosProducto.getStockMaximo());
        producto.setPuntoReposicion(datosProducto.getPuntoReposicion());

        producto.setManejaLote(datosProducto.getManejaLote());
        producto.setManejaVencimiento(datosProducto.getManejaVencimiento());
        producto.setActivo(datosProducto.getActivo());

        producto.setCodigo(datosProducto.getCodigo());
        producto.setCodigoBarras(datosProducto.getCodigoBarras());

        return productoRepository.save(producto);
    }

    // Consultar todos los productos
    public List<ProductoEntity> listarProductos() {
        return productoRepository.findAll();
    }

    // Consultar producto por ID
    public ProductoEntity buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado con id: " + id));
    }

    // RF-INV-14: Buscar por nombre
    public List<ProductoEntity> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // RF-INV-14: Filtrar por tipo
    public List<ProductoEntity> filtrarPorTipo(String tipoProducto) {
        return productoRepository.findByTipoProductoIgnoreCase(tipoProducto);
    }

    // RF-INV-14: Filtrar por estado
    public List<ProductoEntity> filtrarPorEstado(Boolean activo) {
        return productoRepository.findByActivo(activo);
    }

    // RF-INV-14: Buscar por nombre y tipo
    public List<ProductoEntity> buscarPorNombreYTipo(
            String nombre,
            String tipoProducto) {

        return productoRepository
                .findByNombreContainingIgnoreCaseAndTipoProductoIgnoreCase(
                        nombre,
                        tipoProducto
                );
    }
}