package backend.external;

import backend.domain.gateway.FestivosGateway;
import backend.domain.model.DiaInhabil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adaptador HTTP hacia Nager.Date API (días festivos oficiales públicos de México, sin API key). */
@Component
public class NagerDateFestivosClient implements FestivosGateway {

    private static final String FUENTE = "Nager.Date";

    private final RestClient restClient;

    public NagerDateFestivosClient(
            RestClient.Builder restClientBuilder,
            @Value("${finanzas.external.nager.base-url:https://date.nager.at/api/v3}") String baseUrl) {
        this.restClient = restClientBuilder.clone().baseUrl(baseUrl).build();
    }

    @Override
    public List<DiaInhabil> obtenerFestivosMexico(int anio) {
        List<NagerHolidayResponse> respuesta = restClient.get()
                .uri("/PublicHolidays/{anio}/MX", anio)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (respuesta == null || respuesta.isEmpty()) {
            return List.of();
        }
        Instant ahora = Instant.now();
        Map<LocalDate, DiaInhabil> deduplicados = new LinkedHashMap<>();
        for (NagerHolidayResponse item : respuesta) {
            if (item == null || item.date() == null || item.date().isBlank()) {
                continue;
            }
            final LocalDate fecha;
            try {
                fecha = LocalDate.parse(item.date().trim());
            } catch (DateTimeParseException ex) {
                continue;
            }
            if (fecha.getYear() != anio) {
                continue;
            }
            String nombre = (item.localName() != null && !item.localName().isBlank())
                    ? item.localName().trim()
                    : (item.name() != null && !item.name().isBlank() ? item.name().trim() : "Día festivo oficial");
            if (nombre.length() > 140) {
                nombre = nombre.substring(0, 140);
            }
            deduplicados.putIfAbsent(
                    fecha,
                    new DiaInhabil(fecha, nombre, "OFICIAL_MX", anio, FUENTE, ahora));
        }
        return List.copyOf(deduplicados.values());
    }

    record NagerHolidayResponse(String date, String localName, String name) {
    }
}
