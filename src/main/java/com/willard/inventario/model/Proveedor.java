package com.willard.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proveedor")
@Data
@NoArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Column(nullable = false, unique = true, length = 20)
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    private String contacto;

    private String telefono;

    private String email;

    private String direccion;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";
}