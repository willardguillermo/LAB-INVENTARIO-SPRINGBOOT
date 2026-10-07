package com.willard.inventario.exception;

// Se lanza cuando no existe la entidad solicitada (producto, categoría, etc.). Responde 404.
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
