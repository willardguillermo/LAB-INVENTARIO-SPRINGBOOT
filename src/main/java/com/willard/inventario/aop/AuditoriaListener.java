package com.willard.inventario.aop;

import com.willard.inventario.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Guarda los eventos de auditoría solo cuando la transacción del negocio se confirmó.
// fallbackExecution = true: si el método auditado no tenía transacción, se guarda en el acto.
@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);

    private final AuditoriaService auditoriaService;

    public AuditoriaListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alConfirmarOperacion(AuditoriaEvento evento) {
        try {
            // AuditoriaService.registrar usa REQUIRES_NEW: la transacción del negocio ya terminó
            auditoriaService.registrar(evento);
        } catch (RuntimeException e) {
            // La operación ya está confirmada; un fallo al auditar se registra en el log y no
            // se propaga, para no devolver error al cliente por algo que sí se guardó.
            log.error("No se pudo guardar la auditoría {}", evento, e);
        }
    }
}
