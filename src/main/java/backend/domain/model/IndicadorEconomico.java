package backend.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Indicador económico público de México (CETES 28d, Tasa Objetivo Banxico, UDI, USD FIX). */
public record IndicadorEconomico(
        String clave,
        String nombre,
        BigDecimal valor,
        String unidad,
        LocalDate fecha,
        String fuente,
        String serieExterna,
        Instant actualizadoEn,
        boolean desdeCache
) {
}
