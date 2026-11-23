package backend.web.dto;

import backend.domain.model.TipoCambio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TipoCambioResponse(
        String monedaBase,
        String monedaDestino,
        LocalDate fecha,
        BigDecimal tasa,
        String fuente,
        boolean desdeCache
) {
    public static TipoCambioResponse desde(TipoCambio tc) {
        return new TipoCambioResponse(
                tc.monedaBase(),
                tc.monedaDestino(),
                tc.fecha(),
                tc.tasa(),
                tc.fuente(),
                tc.desdeCache()
        );
    }
}
