package backend.web.dto;

import backend.domain.model.DiaInhabil;

import java.time.LocalDate;

/** Contrato público de día inhábil en México (sin exponer metadatos internos de caché/fallback). */
public record DiaInhabilResponse(
        LocalDate fecha,
        String nombre,
        String tipo,
        int anio
) {
    public static DiaInhabilResponse desde(DiaInhabil dia) {
        return new DiaInhabilResponse(
                dia.fecha(),
                dia.nombre(),
                dia.tipo(),
                dia.anio()
        );
    }
}
