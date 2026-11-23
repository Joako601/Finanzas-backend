package backend.web.dto;

import backend.domain.model.SimulacionAhorro;

import java.math.BigDecimal;

public record SimulacionAhorroResponse(
        BigDecimal montoInicial,
        BigDecimal aporteMensual,
        int meses,
        BigDecimal tasaAnualPorcentaje,
        BigDecimal totalAportado,
        BigDecimal rendimientoEstimado,
        BigDecimal montoFinal,
        String fuenteTasa
) {
    public static SimulacionAhorroResponse desde(SimulacionAhorro s) {
        return new SimulacionAhorroResponse(
                s.montoInicial(),
                s.aporteMensual(),
                s.meses(),
                s.tasaAnualPorcentaje(),
                s.totalAportado(),
                s.rendimientoEstimado(),
                s.montoFinal(),
                s.fuenteTasa()
        );
    }
}
