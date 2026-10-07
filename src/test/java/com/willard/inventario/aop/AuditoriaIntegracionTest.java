package com.willard.inventario.aop;

import com.willard.inventario.entity.Auditoria;
import com.willard.inventario.entity.ProductoEntity;
import com.willard.inventario.models.Categoria;
import com.willard.inventario.models.UnidadMedida;
import com.willard.inventario.repository.AuditoriaRepository;
import com.willard.inventario.repository.CategoriaRepository;
import com.willard.inventario.repository.ProductoRepository;
import com.willard.inventario.repository.UnidadMedidaRepository;
import com.willard.inventario.service.ProductoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Prueba de integración de la auditoría: contexto completo de Spring con H2 en memoria
// (src/test/resources/application.properties), aspecto y listener reales.
//
// NO es @Transactional a propósito: la transacción de un test @Transactional se revierte en vez
// de confirmarse, y el listener AFTER_COMMIT nunca se ejecutaría. Por eso cada operación hace
// su propio commit y los datos se limpian en @AfterEach.
@SpringBootTest
class AuditoriaIntegracionTest {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired
    private AuditoriaRepository auditoriaRepository;

    private Categoria categoria;
    private UnidadMedida unidad;

    @BeforeEach
    void crearCatalogos() {
        categoria = new Categoria();
        categoria.setNombre("Medicamentos");
        categoria.setEstado(true);
        categoria = categoriaRepository.save(categoria);

        unidad = new UnidadMedida();
        unidad.setNombre("Caja");
        unidad.setAbreviatura("CJA");
        unidad.setEstado(true);
        unidad = unidadMedidaRepository.save(unidad);
    }

    @AfterEach
    void limpiar() {
        auditoriaRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
        unidadMedidaRepository.deleteAll();
    }

    private ProductoEntity nuevoProducto(String codigo) {
        Categoria refCategoria = new Categoria();
        refCategoria.setId(categoria.getId());
        UnidadMedida refUnidad = new UnidadMedida();
        refUnidad.setId(unidad.getId());

        ProductoEntity p = new ProductoEntity();
        p.setNombre("Paracetamol 500 mg");
        p.setTipoProducto("Medicamento");
        p.setCodigo(codigo);
        p.setCategoria(refCategoria);
        p.setUnidadMedida(refUnidad);
        return p;
    }

    @Test
    void registrarProductoGeneraSuRegistroDeAuditoria() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        ProductoEntity registrado = productoService.registrarProducto(nuevoProducto("MED-001"));

        List<Auditoria> registros = auditoriaRepository.findAll();
        assertThat(registros).hasSize(1);
        Auditoria auditoria = registros.get(0);
        assertThat(auditoria.getOperacion()).isEqualTo("REGISTRAR");
        assertThat(auditoria.getEntidad()).isEqualTo("Producto");
        assertThat(auditoria.getRegistroId()).isEqualTo(registrado.getId());
        assertThat(auditoria.getUsuario()).isEqualTo(UsuarioActualProvider.USUARIO_SISTEMA);
        assertThat(auditoria.getFechaHora()).isAfter(antes).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(auditoria.getDetalle()).isNull();
    }

    @Test
    void desactivarSeAuditaComoEliminacionLogica() {
        ProductoEntity registrado = productoService.registrarProducto(nuevoProducto("MED-001"));

        productoService.desactivarProducto(registrado.getId());

        assertThat(auditoriaRepository.findAll())
                .extracting(Auditoria::getOperacion, Auditoria::getRegistroId, Auditoria::getDetalle)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("REGISTRAR", registrado.getId(), null),
                        org.assertj.core.groups.Tuple.tuple("DESACTIVAR", registrado.getId(), "Eliminación lógica"));
    }

    @Test
    void unaOperacionFallidaNoSeAudita() {
        productoService.registrarProducto(nuevoProducto("MED-001"));

        // Código duplicado: la BD rechaza el INSERT y no debe quedar auditoría de este intento
        assertThatThrownBy(() -> productoService.registrarProducto(nuevoProducto("MED-001")))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(auditoriaRepository.findAll())
                .extracting(Auditoria::getOperacion)
                .containsExactly("REGISTRAR");
    }
}
