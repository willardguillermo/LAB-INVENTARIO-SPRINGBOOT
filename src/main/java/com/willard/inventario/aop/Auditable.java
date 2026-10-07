package com.willard.inventario.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método de servicio para que AuditoriaAspect registre la operación en la bitácora
 * cuando termina sin excepción.
 *
 * <pre>
 * &#64;Auditable(entidad = "Producto", operacion = "DESACTIVAR", detalle = "Eliminación lógica")
 * public ProductoEntity desactivarProducto(Long id) { ... }
 * </pre>
 *
 * El id del registro se toma del objeto devuelto (su getId()) o, si no lo tiene, del primer
 * argumento de tipo Long. El método debe ser público y llamarse desde otro bean (por ejemplo
 * el controlador): las llamadas internas dentro de la misma clase no pasan por el aspecto.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    // Entidad afectada, ej. "Producto", "Categoria", "Proveedor"
    String entidad();

    // Operación, ej. "REGISTRAR", "MODIFICAR", "ACTIVAR", "DESACTIVAR"
    String operacion();

    // Texto opcional que se guarda en la columna detalle
    String detalle() default "";
}
