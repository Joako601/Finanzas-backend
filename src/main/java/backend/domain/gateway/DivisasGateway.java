package backend.domain.gateway;

import backend.domain.model.TipoCambio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto hacia API pública de tipos de cambio (Frankfurter). */
public interface DivisasGateway {

    Optional<TipoCambio> obtenerActual(String base, String destino);

    List<TipoCambio> obtenerHistorico(String base, String destino, LocalDate desde, LocalDate hasta);
}
