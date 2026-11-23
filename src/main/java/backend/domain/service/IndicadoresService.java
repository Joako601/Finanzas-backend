package backend.domain.service;

import backend.domain.exception.OperacionInvalidaException;
import backend.domain.gateway.BanxicoGateway;
import backend.domain.model.IndicadorEconomico;
import backend.domain.model.SimulacionAhorro;
import backend.domain.repository.IndicadorEconomicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Indicadores económicos oficiales de México (Banxico SIE con caché en PostgreSQL)
 * y simulador de interés compuesto mensual para metas de ahorro.
 */
@Service
public class IndicadoresService {

    private static final Logger log = LoggerFactory.getLogger(IndicadoresService.class);
    private static final Duration TTL_CACHE = Duration.ofHours(12);
    private static final BigDecimal TASA_REFERENCIA_DEFECTO = new BigDecimal("9.50");
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal DOCE = new BigDecimal("12");
    private static final BigDecimal MAX_INICIAL = new BigDecimal("100000000.00");
    private static final BigDecimal MAX_MENSUAL = new BigDecimal("10000000.00");
    private static final BigDecimal CERO_DOS_DECIMALES = new BigDecimal("0.00");

    private record DefinicionSerie(String clave, String nombre, String unidad, String serieId) {
    }

    private static final List<DefinicionSerie> SERIES_BANXICO = List.of(
            new DefinicionSerie("CETES_28", "CETES 28 días", "%", "SF43936"),
            new DefinicionSerie("TASA_OBJETIVO", "Tasa objetivo Banxico", "%", "SF61745"),
            new DefinicionSerie("UDI", "Valor de la UDI", "MXN", "SP68257"),
            new DefinicionSerie("USD_FIX", "Tipo de cambio FIX (USD/MXN)", "MXN", "SF43718")
    );

    private final IndicadorEconomicoRepository repository;
    private final BanxicoGateway banxicoGateway;

    public IndicadoresService(
            IndicadorEconomicoRepository repository,
            BanxicoGateway banxicoGateway) {
        this.repository = repository;
        this.banxicoGateway = banxicoGateway;
    }

    public List<IndicadorEconomico> listarActuales() {
        Map<String, IndicadorEconomico> resultadoPorClave = new LinkedHashMap<>();
        for (IndicadorEconomico cached : repository.listarUltimos()) {
            resultadoPorClave.put(cached.clave(), comoDesdeCache(cached));
        }

        if (banxicoGateway.estaConfigurado()) {
            for (DefinicionSerie serie : SERIES_BANXICO) {
                IndicadorEconomico existente = resultadoPorClave.get(serie.clave());
                if (existente != null && esFresco(existente.actualizadoEn())) {
                    continue;
                }
                try {
                    Optional<IndicadorEconomico> vivo = banxicoGateway.obtenerSerieOportuna(
                            serie.clave(), serie.nombre(), serie.unidad(), serie.serieId());
                    if (vivo.isPresent()) {
                        IndicadorEconomico obtenido = vivo.get();
                        try {
                            repository.guardarOActualizar(obtenido);
                        } catch (RuntimeException ex) {
                            log.warn("No se pudo guardar indicador {} en caché: {}", serie.clave(), ex.getMessage());
                        }
                        resultadoPorClave.put(obtenido.clave(), obtenido);
                    }
                } catch (RuntimeException ex) {
                    log.warn("Error consultando serie Banxico {}: {}", serie.clave(), ex.getMessage());
                }
            }
        }

        List<IndicadorEconomico> ordenados = new ArrayList<>(resultadoPorClave.values());
        ordenados.sort(Comparator.comparing(IndicadorEconomico::clave));
        return List.copyOf(ordenados);
    }

    public void sincronizarBanxico() {
        if (!banxicoGateway.estaConfigurado()) {
            return;
        }
        for (DefinicionSerie serie : SERIES_BANXICO) {
            try {
                banxicoGateway.obtenerSerieOportuna(serie.clave(), serie.nombre(), serie.unidad(), serie.serieId())
                        .ifPresent(repository::guardarOActualizar);
            } catch (RuntimeException ex) {
                log.warn("Error sincronizando serie Banxico {}: {}", serie.clave(), ex.getMessage());
            }
        }
    }

    /**
     * Calcula interés compuesto mensual exacto con [BigDecimal] (aportaciones al final de cada mes).
     * Si [tasaAnualOpcional] es `null`, usa el último `CETES_28` válido disponible en caché o la referencia por defecto.
     */
    public SimulacionAhorro simularAhorro(
            BigDecimal montoInicial,
            BigDecimal aporteMensual,
            int meses,
            BigDecimal tasaAnualOpcional) {
        BigDecimal inicial = (montoInicial == null ? CERO_DOS_DECIMALES : montoInicial).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mensual = (aporteMensual == null ? CERO_DOS_DECIMALES : aporteMensual).setScale(2, RoundingMode.HALF_UP);

        if (inicial.compareTo(BigDecimal.ZERO) < 0 || mensual.compareTo(BigDecimal.ZERO) < 0) {
            throw new OperacionInvalidaException("Los montos inicial y mensual no pueden ser negativos.");
        }
        if (inicial.compareTo(MAX_INICIAL) > 0 || mensual.compareTo(MAX_MENSUAL) > 0) {
            throw new OperacionInvalidaException("Los montos enviados exceden los límites permitidos.");
        }
        if (inicial.compareTo(BigDecimal.ZERO) == 0 && mensual.compareTo(BigDecimal.ZERO) == 0) {
            throw new OperacionInvalidaException("Indica un monto inicial o un aporte mensual mayor a cero.");
        }
        if (meses < 1 || meses > 360) {
            throw new OperacionInvalidaException("El plazo debe estar entre 1 y 360 meses.");
        }

        BigDecimal tasaAnual;
        String fuenteTasa;
        if (tasaAnualOpcional != null) {
            if (tasaAnualOpcional.compareTo(BigDecimal.ZERO) < 0
                    || tasaAnualOpcional.compareTo(CIEN) > 0) {
                throw new OperacionInvalidaException("La tasa anual debe estar entre 0% y 100%.");
            }
            tasaAnual = tasaAnualOpcional.setScale(2, RoundingMode.HALF_UP);
            fuenteTasa = "Tasa personalizada";
        } else {
            Optional<IndicadorEconomico> cetes = repository.buscarMasReciente("CETES_28")
                    .filter(i -> i.valor() != null
                            && i.valor().compareTo(BigDecimal.ZERO) >= 0
                            && i.valor().compareTo(CIEN) <= 0);
            if (cetes.isPresent()) {
                tasaAnual = cetes.get().valor().setScale(2, RoundingMode.HALF_UP);
                fuenteTasa = "CETES 28 días (" + cetes.get().fecha() + ")";
            } else {
                tasaAnual = TASA_REFERENCIA_DEFECTO;
                fuenteTasa = "Referencia por defecto (sin dato Banxico en caché)";
            }
        }

        BigDecimal tasaMensual = tasaAnual
                .divide(CIEN, MathContext.DECIMAL128)
                .divide(DOCE, MathContext.DECIMAL128);
        BigDecimal factorMensual = BigDecimal.ONE.add(tasaMensual, MathContext.DECIMAL128);

        BigDecimal saldo = inicial;
        for (int mes = 0; mes < meses; mes++) {
            saldo = saldo.multiply(factorMensual, MathContext.DECIMAL128)
                    .add(mensual, MathContext.DECIMAL128);
        }

        BigDecimal totalAportado = inicial.add(mensual.multiply(BigDecimal.valueOf(meses)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoFinal = saldo.setScale(2, RoundingMode.HALF_UP);
        BigDecimal rendimiento = montoFinal.subtract(totalAportado)
                .max(CERO_DOS_DECIMALES)
                .setScale(2, RoundingMode.HALF_UP);

        return new SimulacionAhorro(
                inicial,
                mensual,
                meses,
                tasaAnual,
                totalAportado,
                rendimiento,
                montoFinal,
                fuenteTasa
        );
    }

    private static boolean esFresco(Instant actualizadoEn) {
        return actualizadoEn != null && actualizadoEn.isAfter(Instant.now().minus(TTL_CACHE));
    }

    private static IndicadorEconomico comoDesdeCache(IndicadorEconomico ind) {
        return new IndicadorEconomico(
                ind.clave(),
                ind.nombre(),
                ind.valor(),
                ind.unidad(),
                ind.fecha(),
                ind.fuente(),
                ind.serieExterna(),
                ind.actualizadoEn(),
                true
        );
    }
}
