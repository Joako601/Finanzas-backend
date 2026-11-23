package backend.web.dto;

import backend.domain.model.ConversionDivisa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConversionDivisaResponse(
        BigDecimal montoOrigen,
        String monedaOrigen,
        BigDecimal montoDestino,
        String monedaDestino,
        BigDecimal tasa,
        LocalDate fechaTasa,
        String fuente,
        boolean desdeCache
) {
    public static ConversionDivisaResponse desde(ConversionDivisa c) {
        return new ConversionDivisaResponse(
                c.montoOrigen(),
                c.monedaOrigen(),
                c.montoDestino(),
                c.monedaDestino(),
                c.tasa(),
                c.fechaTasa(),
                c.fuente(),
                c.desdeCache()
        );
    }
}
