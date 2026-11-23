package backend.domain.gateway;

import backend.domain.model.DiaInhabil;

import java.util.List;

/** Puerto hacia API pública de días festivos oficiales en México (Nager.Date). */
public interface FestivosGateway {

    List<DiaInhabil> obtenerFestivosMexico(int anio);
}
