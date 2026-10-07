package com.willard.inventario.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Mensajes de MySQL que se traducen a algo entendible para el usuario
    private static final Pattern DUPLICADO = Pattern.compile("Duplicate entry '(.*)' for key");
    private static final Pattern MUY_LARGO = Pattern.compile("Data too long for column '(\\w+)'");
    private static final Pattern NULO = Pattern.compile("Column '(\\w+)' cannot be null");

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException ex) {
        return respuesta(ex.getStatus(), ex.getMessage());
    }

    // Usada por los servicios de Categoría, Unidad de medida y Proveedor: sin este handler
    // Spring responde con su formato por defecto y omite el mensaje (reason).
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String mensaje = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return respuesta(status, mensaje);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> cuerpo = cuerpoError(HttpStatus.BAD_REQUEST, "Datos inválidos");
        cuerpo.put("errores", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    // Bean Validation que Hibernate ejecuta al hacer flush (no pasa por @Valid). Ocurre con
    // registros antiguos que no cumplen las reglas actuales, ej. un producto sin categoría.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<?> violacion : ex.getConstraintViolations()) {
            errores.putIfAbsent(violacion.getPropertyPath().toString(), violacion.getMessage());
        }

        Map<String, Object> cuerpo = cuerpoError(HttpStatus.BAD_REQUEST,
                "El registro no cumple las validaciones: " + String.join("; ", errores.values()));
        cuerpo.put("errores", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    // Ej: GET /api/productos/abc o ?activo=quizas
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Valor inválido '" + ex.getValue() + "' para el parámetro '" + ex.getName() + "'");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleParametroFaltante(MissingServletRequestParameterException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
    }

    // JSON mal formado o con tipos incorrectos (ej. "stockMinimo": "diez")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene valores con tipo incorrecto");
    }

    // La causa real viene en el mensaje del driver; se distingue para no mostrar siempre
    // "valor duplicado" cuando el problema es otro (longitud, nulo, clave foránea).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String causa = String.valueOf(ex.getMostSpecificCause().getMessage());

        Matcher m = DUPLICADO.matcher(causa);
        if (m.find()) {
            return respuesta(HttpStatus.CONFLICT, "Ya existe un registro con el valor '" + m.group(1) + "'");
        }
        m = MUY_LARGO.matcher(causa);
        if (m.find()) {
            return respuesta(HttpStatus.BAD_REQUEST,
                    "El valor del campo '" + m.group(1) + "' excede la longitud permitida");
        }
        m = NULO.matcher(causa);
        if (m.find()) {
            return respuesta(HttpStatus.BAD_REQUEST, "El campo '" + m.group(1) + "' es obligatorio");
        }
        if (causa.contains("foreign key constraint fails")) {
            return respuesta(HttpStatus.CONFLICT,
                    "La operación viola una relación con otro registro (clave foránea)");
        }
        return respuesta(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de datos");
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(cuerpoError(status, mensaje));
    }

    private Map<String, Object> cuerpoError(HttpStatus status, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", Instant.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("message", mensaje);
        return cuerpo;
    }
}
