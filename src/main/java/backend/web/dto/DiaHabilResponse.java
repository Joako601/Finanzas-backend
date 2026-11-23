package backend.web.dto;

import backend.domain.model.DiaHabilResultado;

import java.time.LocalDate;

public record DiaHabilResponse(
        LocalDate fechaSolicitada,
        boolean esDiaHabil,
        LocalDate siguienteDiaHabil,
        LocalDate anteriorDiaHabil,
        String motivoInhabil
) {
    public static DiaHabilResponse desde(DiaHabilResultado r) {
        return new DiaHabilResponse(
                r.fechaSolicitada(),
                r.esDiaHabil(),
                r.siguienteDiaHabil(),
                r.anteriorDiaHabil(),
                r.motivoInhabil()
        );
    }
}
