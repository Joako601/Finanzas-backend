package backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Resultado de convertir un monto de una divisa origen a una divisa destino. */
public record ConversionDivisa(
        BigDecimal montoOrigen,
        String monedaOrigen,
        BigDecimal montoDestino,
        String monedaDestino,
        BigDecimal tasa,
        LocalDate fechaTasa,
        String fuente,
        boolean desdeCache
) {
}
