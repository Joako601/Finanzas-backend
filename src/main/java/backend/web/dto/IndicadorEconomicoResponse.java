package backend.web.dto;

import backend.domain.model.IndicadorEconomico;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Contrato público de indicador económico (omite identificadores internos de series externas). */
public record IndicadorEconomicoResponse(
        String clave,
        String nombre,
        BigDecimal valor,
        String unidad,
        LocalDate fecha,
        String fuente,
        boolean desdeCache
) {
    public static IndicadorEconomicoResponse desde(IndicadorEconomico ind) {
        return new IndicadorEconomicoResponse(
                ind.clave(),
                ind.nombre(),
                ind.valor(),
                ind.unidad(),
                ind.fecha(),
                ind.fuente(),
                ind.desdeCache()
        );
    }
}
