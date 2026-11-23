package backend.domain.gateway;

import backend.domain.model.IndicadorEconomico;

import java.util.Optional;

/** Puerto hacia la API oficial Banxico SIE (requiere token gratuito configurado en `BANXICO_TOKEN`). */
public interface BanxicoGateway {

    boolean estaConfigurado();

    Optional<IndicadorEconomico> obtenerSerieOportuna(
            String clave,
            String nombre,
            String unidad,
            String serieId);
}
