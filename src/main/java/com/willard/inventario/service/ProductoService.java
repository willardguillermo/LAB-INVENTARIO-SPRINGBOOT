package com.willard.inventario.service;

import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.exception.RecursoNoEncontradoException;
import com.willard.inventario.model.Proveedor;
import com.willard.inventario.models.Categoria;
import com.willard.inventario.models.UnidadMedida;
import com.willard.inventario.repository.CategoriaRepository;
import com.willard.inventario.repository.ProductoRepository;
import com.willard.inventario.repository.ProveedorRepository;
import com.willard.inventario.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final ProveedorRepository proveedorRepository;

    // Inyección de dependencias por constructor
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            UnidadMedidaRepository unidadMedidaRepository,
            ProveedorRepository proveedorRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.proveedorRepository = proveedorRepository;
    }

    // Resuelve la categoría y unidad de medida enviadas (solo con id) a las entidades reales,
    // validando que existan antes de asociarlas al producto.
    private void resolverRelaciones(ProductoEntity producto) {
        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(producto.getCategoria().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Categoría no encontrada con id: " + producto.getCategoria().getId()));
            producto.setCategoria(categoria);
        } else {
            producto.setCategoria(null);
        }

        if (producto.getUnidadMedida() != null && producto.getUnidadMedida().getId() != null) {
            UnidadMedida unidadMedida = unidadMedidaRepository.findById(producto.getUnidadMedida().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Unidad de medida no encontrada con id: " + producto.getUnidadMedida().getId()));
            producto.setUnidadMedida(unidadMedida);
        } else {
            producto.setUnidadMedida(null);
        }

        if (producto.getProveedor() != null && producto.getProveedor().getId() != null) {
            Proveedor proveedor = proveedorRepository.findById(producto.getProveedor().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Proveedor no encontrado con id: " + producto.getProveedor().getId()));
            producto.setProveedor(proveedor);
        } else {
            producto.setProveedor(null);
        }
    }

    // RF-INV-01: Registrar producto
    public ProductoEntity registrarProducto(ProductoEntity producto) {
    
        producto.setId(null);
        resolverRelaciones(producto);
        return productoRepository.save(producto);
    }

    // RF-INV-02: Modificar producto
    public ProductoEntity modificarProducto(Long id, ProductoEntity datosProducto) {

        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + id));

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

        producto.setCategoria(datosProducto.getCategoria());
        producto.setUnidadMedida(datosProducto.getUnidadMedida());
        producto.setProveedor(datosProducto.getProveedor());
        resolverRelaciones(producto);

        return productoRepository.save(producto);
    }

    // Consultar todos los productos
    public List<ProductoEntity> listarProductos() {
        return productoRepository.findAll();
    }

    // Consultar producto por ID
    public ProductoEntity buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + id));
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