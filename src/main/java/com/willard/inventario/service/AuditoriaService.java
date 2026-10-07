package com.willard.inventario.service;

import com.willard.inventario.aop.AuditoriaEvento;
import com.willard.inventario.entity.Auditoria;
import com.willard.inventario.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // Transacción propia (REQUIRES_NEW): se llama después del commit de la operación auditada
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrar(AuditoriaEvento evento) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(evento.usuario());
        auditoria.setFechaHora(evento.fechaHora());
        auditoria.setOperacion(evento.operacion());
        auditoria.setEntidad(evento.entidad());
        auditoria.setRegistroId(evento.registroId());
        auditoria.setDetalle(evento.detalle());
        return auditoriaRepository.save(auditoria);
    }
}
