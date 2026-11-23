package backend.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Entrada validada para simular rendimiento compuesto de una meta de ahorro. */
public record SimularAhorroRequest(
        @NotNull(message = "El monto inicial es obligatorio (puede ser 0).")
        @DecimalMin(value = "0.00", message = "El monto inicial no puede ser negativo.")
        @DecimalMax(value = "100000000.00", message = "El monto inicial excede el límite permitido.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal montoInicial,

        @NotNull(message = "El aporte mensual es obligatorio (puede ser 0).")
        @DecimalMin(value = "0.00", message = "El aporte mensual no puede ser negativo.")
        @DecimalMax(value = "10000000.00", message = "El aporte mensual excede el límite permitido.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal aporteMensual,

        @Min(value = 1, message = "El plazo mínimo es 1 mes.")
        @Max(value = 360, message = "El plazo máximo es 360 meses (30 años).")
        int meses,

        @DecimalMin(value = "0.00", message = "La tasa anual no puede ser negativa.")
        @DecimalMax(value = "100.00", message = "La tasa anual no puede superar 100%.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal tasaAnualPorcentaje
) {
}
