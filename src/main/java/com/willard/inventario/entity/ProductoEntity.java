package com.willard.inventario.entity;

import com.willard.inventario.models.Categoria;
import com.willard.inventario.models.UnidadMedida;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String marca;

    @Column(length = 150)
    private String fabricante;

    @Column(unique = true, length = 50)
    private String codigo;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @NotBlank(message = "El tipo de producto es obligatorio")
    @Column(name = "tipo_producto", nullable = false, length = 100)
    private String tipoProducto;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @PositiveOrZero(message = "El stock máximo no puede ser negativo")
    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @PositiveOrZero(message = "El punto de reposición no puede ser negativo")
    @Column(name = "punto_reposicion")
    private Integer puntoReposicion;

    @NotNull
    @Column(name = "maneja_lote")
    private Boolean manejaLote = false;

    @NotNull
    @Column(name = "maneja_vencimiento")
    private Boolean manejaVencimiento = false;

    @NotNull
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;
}