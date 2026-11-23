package backend.external;

import backend.domain.service.CalendarioService;
import backend.domain.service.DivisasService;
import backend.domain.service.IndicadoresService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Sincronización periódica de APIs públicas hacia PostgreSQL (deshabilitada en tests vía
 * `finanzas.scheduling.enabled=false`). Cada paso corre aislado para que un fallo en un
 * proveedor o en BD no aborte las demás sincronizaciones.
 */
@Component
public class SincronizacionMercadoScheduler {

    private static final Logger log = LoggerFactory.getLogger(SincronizacionMercadoScheduler.class);
    private static final ZoneId ZONA_MX = ZoneId.of("America/Mexico_City");

    private final DivisasService divisasService;
    private final IndicadoresService indicadoresService;
    private final CalendarioService calendarioService;

    public SincronizacionMercadoScheduler(
            DivisasService divisasService,
            IndicadoresService indicadoresService,
            CalendarioService calendarioService) {
        this.divisasService = divisasService;
        this.indicadoresService = indicadoresService;
        this.calendarioService = calendarioService;
    }

    /** Lunes a viernes 8:00 y 15:00 hora CDMX (después de publicación FIX de Banxico / BCE). */
    @Scheduled(cron = "0 0 8,15 * * MON-FRI", zone = "America/Mexico_City")
    public void sincronizarMercado() {
        log.info("Iniciando sincronización programada de divisas e indicadores");
        ejecutarSeguro("divisas", divisasService::sincronizarParesPrincipales);
        ejecutarSeguro("indicadores Banxico", indicadoresService::sincronizarBanxico);
    }

    /** Primer día de cada mes 3:00 AM CDMX: asegura días inhábiles del año actual y siguiente. */
    @Scheduled(cron = "0 0 3 1 * *", zone = "America/Mexico_City")
    public void sincronizarCalendarioAnual() {
        int anioActual = LocalDate.now(ZONA_MX).getYear();
        if (anioActual >= CalendarioService.ANIO_MIN && anioActual <= CalendarioService.ANIO_MAX) {
            ejecutarSeguro("calendario " + anioActual, () -> calendarioService.sincronizarAnio(anioActual));
        }
        int siguiente = anioActual + 1;
        if (siguiente >= CalendarioService.ANIO_MIN && siguiente <= CalendarioService.ANIO_MAX) {
            ejecutarSeguro("calendario " + siguiente, () -> calendarioService.sincronizarAnio(siguiente));
        }
    }

    private static void ejecutarSeguro(String nombreTarea, Runnable tarea) {
        try {
            tarea.run();
        } catch (RuntimeException ex) {
            log.error("Fallo en tarea programada [{}]: {}", nombreTarea, ex.getMessage());
        }
    }
}
