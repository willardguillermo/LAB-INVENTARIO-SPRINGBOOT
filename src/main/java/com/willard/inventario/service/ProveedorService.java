package com.willard.inventario.service;

import com.willard.inventario.model.Proveedor;
import java.util.List;

public interface ProveedorService {
    Proveedor registrar(Proveedor proveedor);          // RF-INV-20
    Proveedor modificar(Long id, Proveedor proveedor);  // RF-INV-21
    List<Proveedor> listar();                           // RF-INV-22
    Proveedor buscarPorId(Long id);                      // RF-INV-22
}