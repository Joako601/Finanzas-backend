package backend.domain.service;

import backend.domain.exception.OperacionInvalidaException;
import backend.domain.exception.ServicioExternoNoDisponibleException;
import backend.domain.gateway.BanxicoGateway;
import backend.domain.gateway.DivisasGateway;
import backend.domain.model.ConversionDivisa;
import backend.domain.model.IndicadorEconomico;
import backend.domain.model.TipoCambio;
import backend.domain.repository.TipoCambioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Casos de uso de divisas con estrategia caché-primero y respaldo en PostgreSQL.
 * Las llamadas HTTP a APIs externas se ejecutan siempre fuera de transacciones de BD.
 */
@Service
public class DivisasService {

    private static final Logger log = LoggerFactory.getLogger(DivisasService.class);
    private static final Duration TTL_CACHE = Duration.ofHours(6);
    private static final ZoneId ZONA_MX = ZoneId.of("America/Mexico_City");
    private static final List<String> MONEDAS_SOPORTADAS = List.of("MXN", "USD", "EUR", "CAD", "GBP", "JPY");
    private static final BigDecimal MONTO_MINIMO = new BigDecimal("0.01");
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("100000000.00");
    private static final String SERIE_BANXICO_USD_FIX = "SF43718";

    private final TipoCambioRepository repository;
    private final DivisasGateway divisasGateway;
    private final BanxicoGateway banxicoGateway;

    public DivisasService(
            TipoCambioRepository repository,
            DivisasGateway divisasGateway,
            BanxicoGateway banxicoGateway) {
        this.repository = repository;
        this.divisasGateway = divisasGateway;
        this.banxicoGateway = banxicoGateway;
    }

    public TipoCambio obtenerActual(String baseRaw, String destinoRaw) {
        String base = validarMoneda(baseRaw);
        String destino = validarMoneda(destinoRaw);
        if (base.equals(destino)) {
            throw new OperacionInvalidaException("La moneda origen y destino deben ser distintas.");
        }

        Optional<TipoCambio> enCache = repository.buscarMasReciente(base, destino);
        if (enCache.isPresent() && esFresco(enCache.get().actualizadoEn())) {
            return comoDesdeCache(enCache.get());
        }

        Optional<TipoCambio> externo = consultarActualExterno(base, destino);
        if (externo.isPresent()) {
            TipoCambio obtenido = externo.get();
            try {
                repository.guardarOActualizar(obtenido);
            } catch (RuntimeException ex) {
                log.warn("No se pudo actualizar caché de divisas {}/{}: {}", base, destino, ex.getMessage());
            }
            return obtenido;
        }

        return enCache.map(DivisasService::comoDesdeCache)
                .orElseThrow(() -> new ServicioExternoNoDisponibleException(
                        "No se pudo obtener el tipo de cambio " + base + "/" + destino + " ni existe dato en caché."));
    }

    public List<TipoCambio> obtenerHistorico(String baseRaw, String destinoRaw, int dias) {
        String base = validarMoneda(baseRaw);
        String destino = validarMoneda(destinoRaw);
        if (base.equals(destino)) {
            throw new OperacionInvalidaException("La moneda origen y destino deben ser distintas.");
        }
        if (dias < 1 || dias > 90) {
            throw new OperacionInvalidaException("El rango histórico debe estar entre 1 y 90 días.");
        }

        LocalDate hasta = LocalDate.now(ZONA_MX);
        LocalDate desde = hasta.minusDays(dias);
        List<TipoCambio> enCache = repository.buscarHistorico(base, destino, desde, hasta);
        if (tieneCoberturaYFrescura(enCache, desde, hasta)) {
            return enCache.stream().map(DivisasService::comoDesdeCache).toList();
        }

        try {
            List<TipoCambio> externos = divisasGateway.obtenerHistorico(base, destino, desde, hasta);
            if (!externos.isEmpty()) {
                try {
                    repository.guardarOActualizarTodos(externos);
                } catch (RuntimeException ex) {
                    log.warn("No se pudo guardar histórico en caché {}/{}: {}", base, destino, ex.getMessage());
                }
                return externos;
            }
        } catch (RuntimeException ex) {
            log.warn("Fallo consultando histórico de divisas {}/{}: {}", base, destino, ex.getMessage());
        }

        if (!enCache.isEmpty()) {
            return enCache.stream().map(DivisasService::comoDesdeCache).toList();
        }
        throw new ServicioExternoNoDisponibleException(
                "No se pudo obtener el histórico " + base + "/" + destino + " en este momento.");
    }

    public ConversionDivisa convertir(BigDecimal monto, String deRaw, String aRaw) {
        if (monto == null
                || monto.compareTo(MONTO_MINIMO) < 0
                || monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new OperacionInvalidaException("El monto a convertir debe estar entre 0.01 y 100,000,000.00.");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new OperacionInvalidaException("El monto a convertir admite máximo 2 decimales.");
        }
        BigDecimal montoNormalizado = monto.setScale(2, RoundingMode.HALF_UP);
        TipoCambio cotizacion = obtenerActual(deRaw, aRaw);
        BigDecimal convertido = montoNormalizado.multiply(cotizacion.tasa()).setScale(2, RoundingMode.HALF_UP);
        return new ConversionDivisa(
                montoNormalizado,
                cotizacion.monedaBase(),
                convertido,
                cotizacion.monedaDestino(),
                cotizacion.tasa(),
                cotizacion.fecha(),
                cotizacion.fuente(),
                cotizacion.desdeCache()
        );
    }

    /** Refresca los pares principales para la tarea programada diaria. */
    public void sincronizarParesPrincipales() {
        List<String> bases = List.of("USD", "EUR", "CAD");
        for (String base : bases) {
            try {
                consultarActualExterno(base, "MXN").ifPresent(repository::guardarOActualizar);
            } catch (RuntimeException ex) {
                log.warn("Error sincronizando par {}/MXN: {}", base, ex.getMessage());
            }
        }
    }

    private Optional<TipoCambio> consultarActualExterno(String base, String destino) {
        if ("USD".equals(base) && "MXN".equals(destino) && banxicoGateway.estaConfigurado()) {
            try {
                Optional<IndicadorEconomico> fix = banxicoGateway.obtenerSerieOportuna(
                        "USD_FIX", "Tipo de cambio FIX (USD/MXN)", "MXN", SERIE_BANXICO_USD_FIX);
                if (fix.isPresent() && fix.get().valor().compareTo(BigDecimal.ZERO) > 0) {
                    IndicadorEconomico ind = fix.get();
                    return Optional.of(new TipoCambio(
                            "USD", "MXN", ind.fecha(), ind.valor(), "Banxico FIX", ind.actualizadoEn(), false));
                }
            } catch (RuntimeException ex) {
                log.warn("Banxico FIX no disponible, usando fallback Frankfurter: {}", ex.getMessage());
            }
        }
        try {
            return divisasGateway.obtenerActual(base, destino);
        } catch (RuntimeException ex) {
            log.warn("Frankfurter no disponible para {}/{}: {}", base, destino, ex.getMessage());
            return Optional.empty();
        }
    }

    private static boolean tieneCoberturaYFrescura(List<TipoCambio> enCache, LocalDate desde, LocalDate hasta) {
        if (enCache.isEmpty()) {
            return false;
        }
        TipoCambio primero = enCache.get(0);
        TipoCambio ultimo = enCache.get(enCache.size() - 1);
        if (!esFresco(ultimo.actualizadoEn())) {
            return false;
        }
        // Tolerancia de 4 días naturales por fines de semana y puentes bancarios.
        if (primero.fecha().isAfter(desde.plusDays(4)) || ultimo.fecha().isBefore(hasta.minusDays(4))) {
            return false;
        }
        int diasHabiles = contarDiasHabiles(desde, hasta);
        int minimoEsperado = Math.max(1, (int) Math.ceil(diasHabiles * 0.7));
        return enCache.size() >= minimoEsperado;
    }

    private static int contarDiasHabiles(LocalDate desde, LocalDate hasta) {
        int conteo = 0;
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                conteo++;
            }
        }
        return conteo;
    }

    private static String validarMoneda(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new OperacionInvalidaException("El código de moneda ISO-4217 es obligatorio.");
        }
        String codigo = raw.trim().toUpperCase(Locale.ROOT);
        if (!MONEDAS_SOPORTADAS.contains(codigo)) {
            throw new OperacionInvalidaException(
                    "Moneda no soportada. Monedas permitidas: " + String.join(", ", MONEDAS_SOPORTADAS) + ".");
        }
        return codigo;
    }

    private static boolean esFresco(Instant actualizadoEn) {
        return actualizadoEn != null && actualizadoEn.isAfter(Instant.now().minus(TTL_CACHE));
    }

    private static TipoCambio comoDesdeCache(TipoCambio tc) {
        return new TipoCambio(
                tc.monedaBase(),
                tc.monedaDestino(),
                tc.fecha(),
                tc.tasa(),
                tc.fuente(),
                tc.actualizadoEn(),
                true
        );
    }
}
