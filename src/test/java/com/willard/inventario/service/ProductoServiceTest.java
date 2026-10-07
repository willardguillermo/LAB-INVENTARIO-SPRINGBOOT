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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de ProductoService: los repositorios son mocks, no se necesita MySQL.
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private UnidadMedidaRepository unidadMedidaRepository;
    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProductoService productoService;

    // ---------- datos de prueba ----------

    private static Categoria categoria(long id, boolean activa) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre("Categoría " + id);
        c.setEstado(activa);
        return c;
    }

    private static UnidadMedida unidad(long id, boolean activa) {
        UnidadMedida u = new UnidadMedida();
        u.setId(id);
        u.setNombre("Unidad " + id);
        u.setAbreviatura("U" + id);
        u.setEstado(activa);
        return u;
    }

    private static Proveedor proveedor(long id, String estado) {
        Proveedor p = new Proveedor();
        p.setId(id);
        p.setRuc("2060000000" + id);
        p.setRazonSocial("Proveedor " + id);
        p.setEstado(estado);
        return p;
    }

    // Referencias tal como llegan en el JSON: solo con id
    private static Categoria refCategoria(long id) {
        Categoria c = new Categoria();
        c.setId(id);
        return c;
    }

    private static UnidadMedida refUnidad(long id) {
        UnidadMedida u = new UnidadMedida();
        u.setId(id);
        return u;
    }

    private static Proveedor refProveedor(long id) {
        Proveedor p = new Proveedor();
        p.setId(id);
        return p;
    }

    private static ProductoEntity productoNuevo() {
        ProductoEntity p = new ProductoEntity();
        p.setNombre("Paracetamol 500 mg");
        p.setTipoProducto("Medicamento");
        p.setStockMinimo(10);
        p.setPuntoReposicion(20);
        p.setStockMaximo(100);
        p.setCategoria(refCategoria(1));
        p.setUnidadMedida(refUnidad(1));
        return p;
    }

    private static ProductoEntity productoGuardado(long id, boolean activo) {
        ProductoEntity p = productoNuevo();
        p.setId(id);
        p.setActivo(activo);
        p.setCategoria(categoria(1, true));
        p.setUnidadMedida(unidad(1, true));
        return p;
    }

    private void stubCategoriaYUnidadActivas() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1, true)));
        when(unidadMedidaRepository.findById(1L)).thenReturn(Optional.of(unidad(1, true)));
    }

    private void stubGuardarDevuelveArgumento() {
        when(productoRepository.saveAndFlush(any(ProductoEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    // ---------- RF-INV-01 ----------

    @Nested
    class RegistrarProducto {

        @Test
        void registraConRelacionesResueltasYSiempreActivo() {
            stubCategoriaYUnidadActivas();
            when(proveedorRepository.findById(2L)).thenReturn(Optional.of(proveedor(2, "ACTIVO")));
            stubGuardarDevuelveArgumento();

            ProductoEntity entrada = productoNuevo();
            entrada.setId(99L);          // el id enviado por el cliente se ignora
            entrada.setActivo(false);    // todo producto nace activo
            entrada.setProveedor(refProveedor(2));

            ProductoEntity resultado = productoService.registrarProducto(entrada);

            assertThat(resultado.getId()).isNull();
            assertThat(resultado.getActivo()).isTrue();
            assertThat(resultado.getCategoria().getNombre()).isEqualTo("Categoría 1");
            assertThat(resultado.getUnidadMedida().getNombre()).isEqualTo("Unidad 1");
            assertThat(resultado.getProveedor().getRazonSocial()).isEqualTo("Proveedor 2");
            verify(productoRepository).saveAndFlush(entrada);
        }

        @Test
        void proveedorEsOpcional() {
            stubCategoriaYUnidadActivas();
            stubGuardarDevuelveArgumento();

            ProductoEntity resultado = productoService.registrarProducto(productoNuevo());

            assertThat(resultado.getProveedor()).isNull();
            verify(proveedorRepository, never()).findById(anyLong());
        }

        @Test
        void rechazaSinCategoria() {
            ProductoEntity entrada = productoNuevo();
            entrada.setCategoria(null);

            assertThatThrownBy(() -> productoService.registrarProducto(entrada))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("La categoría es obligatoria");
            verify(productoRepository, never()).saveAndFlush(any());
        }

        @Test
        void rechazaCategoriaSinId() {
            ProductoEntity entrada = productoNuevo();
            entrada.setCategoria(new Categoria());   // JSON "categoria": {}

            assertThatThrownBy(() -> productoService.registrarProducto(entrada))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("La categoría es obligatoria");
        }

        @Test
        void rechazaSinUnidadDeMedida() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1, true)));
            ProductoEntity entrada = productoNuevo();
            entrada.setUnidadMedida(null);

            assertThatThrownBy(() -> productoService.registrarProducto(entrada))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("La unidad de medida es obligatoria");
        }

        @Test
        void categoriaInexistenteDevuelve404() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.registrarProducto(productoNuevo()))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessage("Categoría no encontrada con id: 1");
        }

        @Test
        void rechazaCategoriaInactiva() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1, false)));

            assertThatThrownBy(() -> productoService.registrarProducto(productoNuevo()))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("está inactiva");
            verify(productoRepository, never()).saveAndFlush(any());
        }

        @Test
        void rechazaUnidadInactiva() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1, true)));
            when(unidadMedidaRepository.findById(1L)).thenReturn(Optional.of(unidad(1, false)));

            assertThatThrownBy(() -> productoService.registrarProducto(productoNuevo()))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("está inactiva");
        }

        @Test
        void rechazaProveedorInactivo() {
            stubCategoriaYUnidadActivas();
            when(proveedorRepository.findById(2L)).thenReturn(Optional.of(proveedor(2, "INACTIVO")));
            ProductoEntity entrada = productoNuevo();
            entrada.setProveedor(refProveedor(2));

            assertThatThrownBy(() -> productoService.registrarProducto(entrada))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("está inactivo");
        }
    }

    // ---------- Regla de stock ----------

    @Nested
    class ReglaDeStock {

        private void registrarConStock(Integer minimo, Integer reposicion, Integer maximo) {
            ProductoEntity entrada = productoNuevo();
            entrada.setStockMinimo(minimo);
            entrada.setPuntoReposicion(reposicion);
            entrada.setStockMaximo(maximo);
            productoService.registrarProducto(entrada);
        }

        @Test
        void minimoMayorQueMaximo() {
            assertThatThrownBy(() -> registrarConStock(50, null, 10))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El stock mínimo (50) no puede ser mayor que el stock máximo (10)");
        }

        @Test
        void reposicionMenorQueMinimo() {
            assertThatThrownBy(() -> registrarConStock(10, 5, 100))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El punto de reposición (5) no puede ser menor que el stock mínimo (10)");
        }

        @Test
        void reposicionMayorQueMaximo() {
            assertThatThrownBy(() -> registrarConStock(10, 150, 100))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El punto de reposición (150) no puede ser mayor que el stock máximo (100)");
        }

        @Test
        void valoresIgualesYNulosSonValidos() {
            stubCategoriaYUnidadActivas();
            stubGuardarDevuelveArgumento();

            registrarConStock(10, 10, 10);
            registrarConStock(null, null, null);
            registrarConStock(null, 30, null);

            verify(productoRepository, times(3)).saveAndFlush(any());
        }

        @Test
        void tambienSeValidaAlModificar() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, true)));
            ProductoEntity datos = productoNuevo();
            datos.setStockMinimo(50);
            datos.setStockMaximo(10);

            assertThatThrownBy(() -> productoService.modificarProducto(7L, datos))
                    .isInstanceOf(ReglaNegocioException.class);
            verify(productoRepository, never()).saveAndFlush(any());
        }
    }

    // ---------- RF-INV-02 ----------

    @Nested
    class ModificarProducto {

        @Test
        void productoInexistenteDevuelve404() {
            when(productoRepository.buscarConRelacionesPorId(7L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.modificarProducto(7L, productoNuevo()))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessage("Producto no encontrado con id: 7");
        }

        @Test
        void actualizaDatosEIgnoraActivo() {
            ProductoEntity existente = productoGuardado(7, false);
            when(productoRepository.buscarConRelacionesPorId(7L)).thenReturn(Optional.of(existente));
            stubGuardarDevuelveArgumento();

            ProductoEntity datos = productoNuevo();
            datos.setNombre("Paracetamol 1 g");
            datos.setActivo(true);   // el PUT no debe reactivar el producto

            ProductoEntity resultado = productoService.modificarProducto(7L, datos);

            assertThat(resultado.getNombre()).isEqualTo("Paracetamol 1 g");
            assertThat(resultado.getActivo()).isFalse();
        }

        @Test
        void conservaLaCategoriaActualAunqueEsteInactiva() {
            ProductoEntity existente = productoGuardado(7, true);
            existente.setCategoria(categoria(1, false));
            when(productoRepository.buscarConRelacionesPorId(7L)).thenReturn(Optional.of(existente));
            stubGuardarDevuelveArgumento();

            ProductoEntity resultado = productoService.modificarProducto(7L, productoNuevo());

            assertThat(resultado.getCategoria().getEstado()).isFalse();
            verify(categoriaRepository, never()).findById(anyLong());
        }

        @Test
        void cambiarAUnaCategoriaInactivaSeRechaza() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, true)));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria(3, false)));
            ProductoEntity datos = productoNuevo();
            datos.setCategoria(refCategoria(3));

            assertThatThrownBy(() -> productoService.modificarProducto(7L, datos))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("está inactiva");
        }

        @Test
        void cambiaAOtraCategoriaActiva() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, true)));
            when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria(3, true)));
            stubGuardarDevuelveArgumento();
            ProductoEntity datos = productoNuevo();
            datos.setCategoria(refCategoria(3));

            ProductoEntity resultado = productoService.modificarProducto(7L, datos);

            assertThat(resultado.getCategoria().getId()).isEqualTo(3L);
        }
    }

    // ---------- RF-INV-03 ----------

    @Nested
    class ActivarYDesactivar {

        @Test
        void desactivaUnProductoActivo() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, true)));
            stubGuardarDevuelveArgumento();

            ProductoEntity resultado = productoService.desactivarProducto(7L);

            assertThat(resultado.getActivo()).isFalse();
            verify(productoRepository).saveAndFlush(resultado);
        }

        @Test
        void activaUnProductoInactivo() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, false)));
            stubGuardarDevuelveArgumento();

            assertThat(productoService.activarProducto(7L).getActivo()).isTrue();
        }

        @Test
        void desactivarUnoYaInactivoDevuelve409() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, false)));

            assertThatThrownBy(() -> productoService.desactivarProducto(7L))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El producto 'Paracetamol 500 mg' ya está inactivo")
                    .extracting(e -> ((ReglaNegocioException) e).getStatus())
                    .isEqualTo(HttpStatus.CONFLICT);
            verify(productoRepository, never()).saveAndFlush(any());
        }

        @Test
        void activarUnoYaActivoDevuelve409() {
            when(productoRepository.buscarConRelacionesPorId(7L))
                    .thenReturn(Optional.of(productoGuardado(7, true)));

            assertThatThrownBy(() -> productoService.activarProducto(7L))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El producto 'Paracetamol 500 mg' ya está activo")
                    .extracting(e -> ((ReglaNegocioException) e).getStatus())
                    .isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        void productoAntiguoSinCategoriaPideEditarlo() {
            ProductoEntity legado = productoGuardado(7, true);
            legado.setCategoria(null);
            when(productoRepository.buscarConRelacionesPorId(7L)).thenReturn(Optional.of(legado));

            assertThatThrownBy(() -> productoService.desactivarProducto(7L))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("El producto 'Paracetamol 500 mg' no tiene categoría asignada; "
                            + "edítalo antes de cambiar su estado")
                    .extracting(e -> ((ReglaNegocioException) e).getStatus())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void productoInexistenteDevuelve404() {
            when(productoRepository.buscarConRelacionesPorId(7L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.activarProducto(7L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    // ---------- RF-INV-14 ----------

    @Test
    @SuppressWarnings("unchecked")
    void buscarProductosOrdenaPorId() {
        ProductoEntity p = productoGuardado(7, true);
        when(productoRepository.findAll(any(Specification.class), eq(Sort.by("id")))).thenReturn(List.of(p));

        List<ProductoEntity> resultado =
                productoService.buscarProductos("para", "medic", true, 1L, null);

        assertThat(resultado).containsExactly(p);
    }
}
