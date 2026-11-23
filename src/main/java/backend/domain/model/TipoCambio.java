package backend.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Cotización de un par de divisas (ej. USD -> MXN) para una fecha y fuente específica. */
public record TipoCambio(
        String monedaBase,
        String monedaDestino,
        LocalDate fecha,
        BigDecimal tasa,
        String fuente,
        Instant actualizadoEn,
        boolean desdeCache
) {
}
