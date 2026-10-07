package com.willard.inventario.service;

import com.willard.inventario.aop.AuditoriaEvento;
import com.willard.inventario.dto.PaginaRespuesta;
import com.willard.inventario.entity.Auditoria;
import com.willard.inventario.exception.ReglaNegocioException;
import com.willard.inventario.repository.AuditoriaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {

    public static final int TAMANIO_POR_DEFECTO = 50;
    public static final int TAMANIO_MAXIMO = 200;

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

    // Consulta de la bitácora con filtros opcionales combinados con AND, del más reciente al
    // más antiguo. El rango de fechas es inclusivo: hasta=2026-10-06 incluye todo ese día.
    @Transactional(readOnly = true)
    public PaginaRespuesta<Auditoria> consultar(String entidad, String operacion, String usuario,
                                                LocalDate desde, LocalDate hasta,
                                                int pagina, int tamanio) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' (" + desde
                    + ") no puede ser posterior a la fecha 'hasta' (" + hasta + ")");
        }
        if (pagina < 0) {
            throw new ReglaNegocioException("La página no puede ser negativa");
        }
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new ReglaNegocioException("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO);
        }

        List<Specification<Auditoria>> filtros = new ArrayList<>();
        if (entidad != null && !entidad.isBlank()) {
            String valor = entidad.trim().toLowerCase();
            filtros.add((root, query, cb) -> cb.equal(cb.lower(root.get("entidad")), valor));
        }
        if (operacion != null && !operacion.isBlank()) {
            String valor = operacion.trim().toLowerCase();
            filtros.add((root, query, cb) -> cb.equal(cb.lower(root.get("operacion")), valor));
        }
        if (usuario != null && !usuario.isBlank()) {
            // % y _ se escapan para buscarlos como texto literal
            String valor = "%" + usuario.trim().toLowerCase()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            filtros.add((root, query, cb) -> cb.like(cb.lower(root.get("usuario")), valor, '\\'));
        }
        if (desde != null) {
            filtros.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaHora"), desde.atStartOfDay()));
        }
        if (hasta != null) {
            filtros.add((root, query, cb) ->
                    cb.lessThan(root.get("fechaHora"), hasta.plusDays(1).atStartOfDay()));
        }

        // Desempate por id: varias operaciones pueden caer en el mismo instante
        Sort orden = Sort.by(Sort.Order.desc("fechaHora"), Sort.Order.desc("id"));
        return PaginaRespuesta.de(auditoriaRepository.findAll(
                Specification.allOf(filtros), PageRequest.of(pagina, tamanio, orden)));
    }
}
