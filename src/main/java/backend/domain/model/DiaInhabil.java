package backend.domain.model;

import java.time.Instant;
import java.time.LocalDate;

/** Día inhábil oficial (LFT) o bancario (CNBV) en México. */
public record DiaInhabil(
        LocalDate fecha,
        String nombre,
        String tipo,
        int anio,
        String fuente,
        Instant actualizadoEn
) {
}
