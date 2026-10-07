package com.willard.inventario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Bitácora de auditoría (P2). Solo se inserta: no hay endpoints para modificar ni borrar registros.
@Entity
@Table(name = "auditoria", indexes = {
        @Index(name = "idx_auditoria_entidad_registro", columnList = "entidad, registro_id"),
        @Index(name = "idx_auditoria_fecha_hora", columnList = "fecha_hora")
})
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    // REGISTRAR, MODIFICAR, ACTIVAR, DESACTIVAR...
    @Column(nullable = false, length = 30)
    private String operacion;

    // Nombre de la entidad afectada, ej. "Producto"
    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(length = 255)
    private String detalle;
}
