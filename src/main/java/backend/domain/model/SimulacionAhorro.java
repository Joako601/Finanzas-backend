package backend.domain.model;

import java.math.BigDecimal;

/** Resultado de simular rendimiento compuesto mensual para una meta de ahorro. */
public record SimulacionAhorro(
        BigDecimal montoInicial,
        BigDecimal aporteMensual,
        int meses,
        BigDecimal tasaAnualPorcentaje,
        BigDecimal totalAportado,
        BigDecimal rendimientoEstimado,
        BigDecimal montoFinal,
        String fuenteTasa
) {
}
