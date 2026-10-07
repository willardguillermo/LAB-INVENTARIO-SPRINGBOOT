package com.willard.inventario.service;

import com.willard.inventario.aop.Auditable;
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
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Auditable(entidad = "Producto", operacion = "REGISTRAR")
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
    @Auditable(entidad = "Producto", operacion = "MODIFICAR")
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

    // RF-INV-03: Activar producto. Métodos separados de desactivar para que la auditoría
    // registre la operación por el nombre del método.
    @Transactional
    @Auditable(entidad = "Producto", operacion = "ACTIVAR")
    public ProductoEntity activarProducto(Long id) {
        return cambiarEstado(id, true);
    }

    // RF-INV-03: Desactivar producto (eliminación lógica)
    @Transactional
    @Auditable(entidad = "Producto", operacion = "DESACTIVAR", detalle = "Eliminación lógica")
    public ProductoEntity desactivarProducto(Long id) {
        return cambiarEstado(id, false);
    }

    private ProductoEntity cambiarEstado(Long id, boolean activo) {
        ProductoEntity producto = buscarPorId(id);

        if (producto.getActivo() != null && producto.getActivo() == activo) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El producto '" + producto.getNombre()
                    + "' ya está " + (activo ? "activo" : "inactivo"));
        }
        // Productos registrados antes de que categoría y unidad fueran obligatorias:
        // Hibernate rechazaría el flush, así que se avisa qué falta corregir.
        if (producto.getCategoria() == null) {
            throw new ReglaNegocioException("El producto '" + producto.getNombre()
                    + "' no tiene categoría asignada; edítalo antes de cambiar su estado");
        }
        if (producto.getUnidadMedida() == null) {
            throw new ReglaNegocioException("El producto '" + producto.getNombre()
                    + "' no tiene unidad de medida asignada; edítalo antes de cambiar su estado");
        }

        producto.setActivo(activo);
        return productoRepository.saveAndFlush(producto);
    }

    // RF-INV-14: Buscar y filtrar productos. Todos los filtros son opcionales y se combinan
    // con AND; sin filtros devuelve todos los productos.
    public List<ProductoEntity> buscarProductos(String nombre, String tipo, Boolean activo,
                                                Long categoriaId, Long proveedorId) {
        List<Specification<ProductoEntity>> filtros = new ArrayList<>();
        filtros.add(traerRelaciones());

        if (nombre != null && !nombre.isBlank()) {
            filtros.add(contieneIgnorandoMayusculas("nombre", nombre));
        }
        if (tipo != null && !tipo.isBlank()) {
            filtros.add(contieneIgnorandoMayusculas("tipoProducto", tipo));
        }
        if (activo != null) {
            filtros.add((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        if (categoriaId != null) {
            filtros.add((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        }
        if (proveedorId != null) {
            filtros.add((root, query, cb) -> cb.equal(root.get("proveedor").get("id"), proveedorId));
        }

        return productoRepository.findAll(Specification.allOf(filtros), Sort.by("id"));
    }

    // Consultar producto por ID
    public ProductoEntity buscarPorId(Long id) {
        return productoRepository.buscarConRelacionesPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + id));
    }

    // LEFT JOIN FETCH de las relaciones: una sola consulta (sin N+1) y sin depender de
    // open-in-view. Se fuerza LEFT porque con @NotNull Hibernate usaría INNER JOIN y
    // ocultaría productos antiguos sin categoría o unidad.
    private static Specification<ProductoEntity> traerRelaciones() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class) {   // no aplica a consultas count
                root.fetch("categoria", JoinType.LEFT);
                root.fetch("unidadMedida", JoinType.LEFT);
                root.fetch("proveedor", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    // Se escapan % y _ para que el texto del usuario se busque literal y no como comodín
    private static Specification<ProductoEntity> contieneIgnorandoMayusculas(String campo, String texto) {
        String literal = texto.trim().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String patron = "%" + literal + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(campo)), patron, '\\');
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
