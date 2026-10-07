package com.willard.inventario.service;

import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.exception.RecursoNoEncontradoException;
import com.willard.inventario.exception.ReglaNegocioException;
import com.willard.inventario.model.Proveedor;
import com.willard.inventario.models.Categoria;
import com.willard.inventario.models.UnidadMedida;
import com.willard.inventario.repository.CategoriaRepository;
import com.willard.inventario.repository.ProductoRepository;
import com.willard.inventario.repository.ProveedorRepository;
import com.willard.inventario.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
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

    // RF-INV-01: Registrar producto
    @Transactional
    public ProductoEntity registrarProducto(ProductoEntity producto) {
        producto.setId(null);
        // Todo producto nace activo; el estado solo cambia con activar/desactivar (RF-INV-03)
        producto.setActivo(true);
        validarStock(producto);
        producto.setCategoria(resolverCategoria(producto.getCategoria(), null));
        producto.setUnidadMedida(resolverUnidadMedida(producto.getUnidadMedida(), null));
        producto.setProveedor(resolverProveedor(producto.getProveedor(), null));

        // saveAndFlush: los errores de la BD (ej. código duplicado) saltan dentro del método
        // y no al hacer commit, cuando la auditoría ya podría haber registrado la operación.
        return productoRepository.saveAndFlush(producto);
    }

    // RF-INV-02: Modificar producto. No cambia "activo": para eso están activar/desactivar.
    @Transactional
    public ProductoEntity modificarProducto(Long id, ProductoEntity datosProducto) {
        ProductoEntity producto = buscarPorId(id);

        validarStock(datosProducto);
        Categoria categoria = resolverCategoria(datosProducto.getCategoria(), producto.getCategoria());
        UnidadMedida unidadMedida =
                resolverUnidadMedida(datosProducto.getUnidadMedida(), producto.getUnidadMedida());
        Proveedor proveedor = resolverProveedor(datosProducto.getProveedor(), producto.getProveedor());

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

        producto.setCodigo(datosProducto.getCodigo());
        producto.setCodigoBarras(datosProducto.getCodigoBarras());

        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        producto.setProveedor(proveedor);

        return productoRepository.saveAndFlush(producto);
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

    // Regla de negocio: stock mínimo <= punto de reposición <= stock máximo.
    // Solo se comparan los valores que vienen informados.
    private void validarStock(ProductoEntity p) {
        Integer minimo = p.getStockMinimo();
        Integer maximo = p.getStockMaximo();
        Integer reposicion = p.getPuntoReposicion();

        if (minimo != null && maximo != null && minimo > maximo) {
            throw new ReglaNegocioException("El stock mínimo (" + minimo
                    + ") no puede ser mayor que el stock máximo (" + maximo + ")");
        }
        if (minimo != null && reposicion != null && reposicion < minimo) {
            throw new ReglaNegocioException("El punto de reposición (" + reposicion
                    + ") no puede ser menor que el stock mínimo (" + minimo + ")");
        }
        if (maximo != null && reposicion != null && reposicion > maximo) {
            throw new ReglaNegocioException("El punto de reposición (" + reposicion
                    + ") no puede ser mayor que el stock máximo (" + maximo + ")");
        }
    }

    // Las relaciones llegan solo con id; se buscan las entidades reales.
    // Si el producto ya tenía esa misma relación se conserva aunque hoy esté inactiva,
    // para no impedir editar otros datos; lo que no se permite es asignar una inactiva nueva.
    private Categoria resolverCategoria(Categoria enviada, Categoria actual) {
        if (enviada == null || enviada.getId() == null) {
            throw new ReglaNegocioException("La categoría es obligatoria");
        }
        if (actual != null && enviada.getId().equals(actual.getId())) {
            return actual;
        }
        Categoria categoria = categoriaRepository.findById(enviada.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada con id: " + enviada.getId()));
        if (!Boolean.TRUE.equals(categoria.getEstado())) {
            throw new ReglaNegocioException("La categoría '" + categoria.getNombre()
                    + "' está inactiva y no se puede asignar");
        }
        return categoria;
    }

    private UnidadMedida resolverUnidadMedida(UnidadMedida enviada, UnidadMedida actual) {
        if (enviada == null || enviada.getId() == null) {
            throw new ReglaNegocioException("La unidad de medida es obligatoria");
        }
        if (actual != null && enviada.getId().equals(actual.getId())) {
            return actual;
        }
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(enviada.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Unidad de medida no encontrada con id: " + enviada.getId()));
        if (!Boolean.TRUE.equals(unidadMedida.getEstado())) {
            throw new ReglaNegocioException("La unidad de medida '" + unidadMedida.getNombre()
                    + "' está inactiva y no se puede asignar");
        }
        return unidadMedida;
    }

    // El proveedor es opcional; su estado es un texto ("ACTIVO"/"INACTIVO")
    private Proveedor resolverProveedor(Proveedor enviado, Proveedor actual) {
        if (enviado == null || enviado.getId() == null) {
            return null;
        }
        if (actual != null && enviado.getId().equals(actual.getId())) {
            return actual;
        }
        Proveedor proveedor = proveedorRepository.findById(enviado.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor no encontrado con id: " + enviado.getId()));
        if (!"ACTIVO".equalsIgnoreCase(proveedor.getEstado())) {
            throw new ReglaNegocioException("El proveedor '" + proveedor.getRazonSocial()
                    + "' está inactivo y no se puede asignar");
        }
        return proveedor;
    }
}
