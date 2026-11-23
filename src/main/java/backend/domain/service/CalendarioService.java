package backend.domain.service;

import backend.domain.exception.OperacionInvalidaException;
import backend.domain.gateway.FestivosGateway;
import backend.domain.model.DiaHabilResultado;
import backend.domain.model.DiaInhabil;
import backend.domain.repository.DiaInhabilRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Calendario de días inhábiles oficiales (Nager.Date / LFT) y bancarios CNBV de México.
 * Permite calcular el siguiente día hábil (fechas límite de tarjetas) y el día hábil anterior (quincenas).
 */
@Service
public class CalendarioService {

    public static final int ANIO_MIN = 2020;
    public static final int ANIO_MAX = 2035;

    private static final Logger log = LoggerFactory.getLogger(CalendarioService.class);
    private static final Duration TTL_OFICIAL = Duration.ofDays(30);
    private static final Duration TTL_FALLBACK = Duration.ofHours(6);
    private static final String FUENTE_FALLBACK = "LFT-Fallback";

    private final DiaInhabilRepository repository;
    private final FestivosGateway festivosGateway;

    public CalendarioService(DiaInhabilRepository repository, FestivosGateway festivosGateway) {
        this.repository = repository;
        this.festivosGateway = festivosGateway;
    }

    public List<DiaInhabil> listarInhabiles(int anio) {
        validarAnio(anio);
        List<DiaInhabil> enCache = repository.listarPorAnio(anio);
        Optional<Instant> ultima = repository.ultimaActualizacion(anio);
        boolean tieneFuenteOficial = enCache.stream()
                .anyMatch(d -> "OFICIAL_MX".equals(d.tipo()) && !FUENTE_FALLBACK.equals(d.fuente()));
        Duration ttlAplicable = tieneFuenteOficial ? TTL_OFICIAL : TTL_FALLBACK;

        if (!enCache.isEmpty() && ultima.isPresent() && ultima.get().isAfter(Instant.now().minus(ttlAplicable))) {
            return enCache;
        }

        sincronizarAnio(anio);
        return repository.listarPorAnio(anio);
    }

    public void sincronizarAnio(int anio) {
        validarAnio(anio);
        List<DiaInhabil> oficialesExternos = List.of();
        try {
            List<DiaInhabil> obtenidos = festivosGateway.obtenerFestivosMexico(anio);
            if (obtenidos != null) {
                oficialesExternos = obtenidos;
            }
        } catch (RuntimeException ex) {
            log.warn("Nager.Date no disponible para año {}, aplicando respaldo LFT/CNBV: {}", anio, ex.getMessage());
        }

        List<DiaInhabil> bancarios = diasBancariosCnbv(anio);
        if (!oficialesExternos.isEmpty()) {
            List<DiaInhabil> combinados = new ArrayList<>(oficialesExternos);
            agregarTransmisionEjecutivoSiFalta(combinados, anio, "LFT-MX");
            combinados.addAll(bancarios);
            repository.guardarOActualizar(combinados);
        } else {
            List<DiaInhabil> respaldo = new ArrayList<>(diasOficialesFijosFallback(anio));
            respaldo.addAll(bancarios);
            // En fallback usamos guardarSiNoExiste para nunca sobreescribir datos oficiales previos.
            repository.guardarSiNoExiste(respaldo);
        }
    }

    public DiaHabilResultado evaluarFechaHabil(LocalDate fecha) {
        if (fecha == null) {
            throw new OperacionInvalidaException("La fecha es obligatoria.");
        }
        validarAnio(fecha.getYear());

        Map<LocalDate, String> inhabiles = cargarMapaParaFecha(fecha);
        String motivo = motivoDeFecha(fecha, inhabiles);
        if (motivo == null) {
            return new DiaHabilResultado(fecha, true, fecha, fecha, null);
        }

        LocalDate siguiente = fecha.plusDays(1);
        while (motivoDeFecha(siguiente, inhabiles) != null) {
            siguiente = siguiente.plusDays(1);
        }

        LocalDate anterior = fecha.minusDays(1);
        while (motivoDeFecha(anterior, inhabiles) != null) {
            anterior = anterior.minusDays(1);
        }

        return new DiaHabilResultado(fecha, false, siguiente, anterior, motivo);
    }

    private Map<LocalDate, String> cargarMapaParaFecha(LocalDate fecha) {
        Map<LocalDate, String> mapa = new LinkedHashMap<>();
        int anio = fecha.getYear();
        agregarAnioAlMapa(mapa, anio);

        if (fecha.getMonthValue() == 1 && fecha.getDayOfMonth() <= 7) {
            agregarAnioAlMapa(mapa, anio - 1);
        } else if (fecha.getMonthValue() == 12 && fecha.getDayOfMonth() >= 25) {
            agregarAnioAlMapa(mapa, anio + 1);
        }
        return mapa;
    }

    private void agregarAnioAlMapa(Map<LocalDate, String> mapa, int anio) {
        List<DiaInhabil> dias;
        if (anio >= ANIO_MIN && anio <= ANIO_MAX) {
            dias = listarInhabiles(anio);
        } else {
            // Si el salto de año rebasa [ANIO_MIN, ANIO_MAX] (ej. 31 dic 2035 -> 1 ene 2036), resolvemos en memoria.
            dias = new ArrayList<>(diasOficialesFijosFallback(anio));
            dias.addAll(diasBancariosCnbv(anio));
        }
        for (DiaInhabil dia : dias) {
            mapa.putIfAbsent(dia.fecha(), dia.nombre());
        }
    }

    private static String motivoDeFecha(LocalDate fecha, Map<LocalDate, String> inhabiles) {
        String festivo = inhabiles.get(fecha);
        if (festivo != null) {
            return festivo;
        }
        DayOfWeek dow = fecha.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            return "Sábado (fin de semana)";
        }
        if (dow == DayOfWeek.SUNDAY) {
            return "Domingo (fin de semana)";
        }
        return null;
    }

    /** Días inhábiles bancarios CNBV no cubiertos por festivos LFT (Semana Santa, 2 nov y 12 dic). */
    static List<DiaInhabil> diasBancariosCnbv(int anio) {
        Instant ahora = Instant.now();
        LocalDate pascua = calcularDomingoPascua(anio);
        LocalDate juevesSanto = pascua.minusDays(3);
        LocalDate viernesSanto = pascua.minusDays(2);
        return List.of(
                new DiaInhabil(juevesSanto, "Jueves Santo (CNBV)", "BANCARIO_CNBV", anio, "CNBV", ahora),
                new DiaInhabil(viernesSanto, "Viernes Santo (CNBV)", "BANCARIO_CNBV", anio, "CNBV", ahora),
                new DiaInhabil(LocalDate.of(anio, 11, 2), "Día de Muertos (CNBV)", "BANCARIO_CNBV", anio, "CNBV", ahora),
                new DiaInhabil(LocalDate.of(anio, 12, 12), "Día del Empleado Bancario (CNBV)", "BANCARIO_CNBV", anio, "CNBV", ahora)
        );
    }

    /** Respaldo determinista de los feriados oficiales LFT de México si Nager.Date no responde. */
    private static List<DiaInhabil> diasOficialesFijosFallback(int anio) {
        Instant ahora = Instant.now();
        List<DiaInhabil> lista = new ArrayList<>(List.of(
                new DiaInhabil(LocalDate.of(anio, 1, 1), "Año Nuevo", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora),
                new DiaInhabil(primerLunesDe(anio, 2), "Día de la Constitución", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora),
                new DiaInhabil(tercerLunesDe(anio, 3), "Natalicio de Benito Juárez", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora),
                new DiaInhabil(LocalDate.of(anio, 5, 1), "Día del Trabajo", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora),
                new DiaInhabil(LocalDate.of(anio, 9, 16), "Día de la Independencia", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora),
                new DiaInhabil(tercerLunesDe(anio, 11), "Revolución Mexicana", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora),
                new DiaInhabil(LocalDate.of(anio, 12, 25), "Navidad", "OFICIAL_MX", anio, FUENTE_FALLBACK, ahora)
        ));
        agregarTransmisionEjecutivoSiFalta(lista, anio, FUENTE_FALLBACK);
        return List.copyOf(lista);
    }

    /** Art. 74 fracc. VII LFT vigente desde 2024: 1 de octubre cada 6 años (2024, 2030, ...). */
    private static void agregarTransmisionEjecutivoSiFalta(List<DiaInhabil> lista, int anio, String fuente) {
        if (anio >= 2024 && (anio - 2024) % 6 == 0) {
            LocalDate primeroOctubre = LocalDate.of(anio, 10, 1);
            boolean yaExiste = lista.stream().anyMatch(d -> primeroOctubre.equals(d.fecha()));
            if (!yaExiste) {
                lista.add(new DiaInhabil(
                        primeroOctubre,
                        "Transmisión del Poder Ejecutivo Federal",
                        "OFICIAL_MX",
                        anio,
                        fuente,
                        Instant.now()));
            }
        }
    }

    private static LocalDate primerLunesDe(int anio, int mes) {
        LocalDate d = LocalDate.of(anio, mes, 1);
        while (d.getDayOfWeek() != DayOfWeek.MONDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    private static LocalDate tercerLunesDe(int anio, int mes) {
        return primerLunesDe(anio, mes).plusWeeks(2);
    }

    /** Algoritmo de Meeus/Jones/Butcher para calcular el Domingo de Pascua gregoriano. */
    static LocalDate calcularDomingoPascua(int anio) {
        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(anio, mes, dia);
    }

    private static void validarAnio(int anio) {
        if (anio < ANIO_MIN || anio > ANIO_MAX) {
            throw new OperacionInvalidaException("El año debe estar entre " + ANIO_MIN + " y " + ANIO_MAX + ".");
        }
    }
}
