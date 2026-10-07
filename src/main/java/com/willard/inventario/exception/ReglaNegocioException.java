package com.willard.inventario.exception;

import org.springframework.http.HttpStatus;

// Se lanza cuando los datos son válidos en forma pero violan una regla de negocio
// (ej. stock mínimo mayor que el máximo). Por defecto responde 400; se puede indicar
// otro estado, como 409 cuando la operación choca con el estado actual del recurso.
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus status;

    public ReglaNegocioException(String mensaje) {
        this(HttpStatus.BAD_REQUEST, mensaje);
    }

    public ReglaNegocioException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
