package com.willard.inventario.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

// Intercepta los métodos anotados con @Auditable que terminan sin excepción y publica un
// AuditoriaEvento. No guarda directamente: AuditoriaListener lo persiste después del commit,
// así no queda registrada una operación que luego se revirtió.
@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);

    private final ApplicationEventPublisher publicador;
    private final UsuarioActualProvider usuarioActual;

    public AuditoriaAspect(ApplicationEventPublisher publicador, UsuarioActualProvider usuarioActual) {
        this.publicador = publicador;
        this.usuarioActual = usuarioActual;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "resultado")
    public void auditar(JoinPoint joinPoint, Auditable auditable, Object resultado) {
        try {
            // Usuario y fecha se capturan aquí, en el hilo de la petición, y no en el listener
            AuditoriaEvento evento = new AuditoriaEvento(
                    usuarioActual.obtenerUsuario(),
                    LocalDateTime.now(),
                    auditable.operacion(),
                    auditable.entidad(),
                    obtenerRegistroId(resultado, joinPoint.getArgs()),
                    auditable.detalle().isBlank() ? null : auditable.detalle());
            publicador.publishEvent(evento);
        } catch (RuntimeException e) {
            // La auditoría nunca debe hacer fallar la operación de negocio
            log.error("No se pudo preparar la auditoría de {}", joinPoint.getSignature().toShortString(), e);
        }
    }

    // Id del registro afectado: getId() del objeto devuelto o, si no, el primer argumento Long
    private Long obtenerRegistroId(Object resultado, Object[] argumentos) {
        if (resultado != null) {
            try {
                Method getId = resultado.getClass().getMethod("getId");
                Object id = getId.invoke(resultado);
                if (id instanceof Long idLong) {
                    return idLong;
                }
            } catch (ReflectiveOperationException e) {
                // El resultado no tiene getId(): se intenta con los argumentos
            }
        }
        for (Object argumento : argumentos) {
            if (argumento instanceof Long idLong) {
                return idLong;
            }
        }
        return null;
    }
}
