package com.willard.inventario.controller;

import com.willard.inventario.dto.PaginaRespuesta;
import com.willard.inventario.entity.Auditoria;
import com.willard.inventario.service.AuditoriaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// Bitácora de auditoría (P2). Solo lectura: los registros los crea AuditoriaAspect.
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // Ej: GET /api/auditoria?entidad=Producto&operacion=DESACTIVAR&usuario=sistema
    //         &desde=2026-10-01&hasta=2026-10-06&pagina=0&tamanio=50
    @GetMapping
    public ResponseEntity<PaginaRespuesta<Auditoria>> consultar(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String operacion,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + AuditoriaService.TAMANIO_POR_DEFECTO) int tamanio) {

        return ResponseEntity.ok(
                auditoriaService.consultar(entidad, operacion, usuario, desde, hasta, pagina, tamanio)
        );
    }
}
