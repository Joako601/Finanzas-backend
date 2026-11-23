package backend.domain.model;

import java.time.LocalDate;

/** Evaluación de si una fecha es día hábil bancario en México y sus días hábiles adyacentes. */
public record DiaHabilResultado(
        LocalDate fechaSolicitada,
        boolean esDiaHabil,
        LocalDate siguienteDiaHabil,
        LocalDate anteriorDiaHabil,
        String motivoInhabil
) {
}
