package com.willard.inventario.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Formato JSON estable para respuestas paginadas: no se serializa Page directamente
// porque su estructura interna puede cambiar entre versiones de Spring Data.
public record PaginaRespuesta<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long total,
        int totalPaginas) {

    public static <T> PaginaRespuesta<T> de(Page<T> page) {
        return new PaginaRespuesta<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
