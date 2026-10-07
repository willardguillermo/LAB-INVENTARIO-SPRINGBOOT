package com.willard.inventario.aop;

import java.time.LocalDateTime;

// Datos de una operación auditada. Se arma en el aspecto (usuario y fecha del momento de la
// operación) y se guarda recién cuando la transacción del negocio confirma (AuditoriaListener).
public record AuditoriaEvento(
        String usuario,
        LocalDateTime fechaHora,
        String operacion,
        String entidad,
        Long registroId,
        String detalle) {
}
