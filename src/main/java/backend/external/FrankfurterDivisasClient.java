package backend.external;

import backend.domain.gateway.DivisasGateway;
import backend.domain.model.TipoCambio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador HTTP hacia Frankfurter API (datos públicos del Banco Central Europeo, sin API key).
 * Clona el `RestClient.Builder` inyectado para aislar completamente su configuración.
 */
@Component
public class FrankfurterDivisasClient implements DivisasGateway {

    private static final String FUENTE = "Frankfurter (BCE)";

    private final RestClient restClient;

    public FrankfurterDivisasClient(
            RestClient.Builder restClientBuilder,
            @Value("${finanzas.external.frankfurter.base-url:https://api.frankfurter.dev/v1}") String baseUrl) {
        this.restClient = restClientBuilder.clone().baseUrl(baseUrl).build();
    }

    @Override
    public Optional<TipoCambio> obtenerActual(String base, String destino) {
        FrankfurterActualResponse respuesta = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest")
                        .queryParam("base", base)
                        .queryParam("symbols", destino)
                        .build())
                .retrieve()
                .body(FrankfurterActualResponse.class);

        if (respuesta == null || respuesta.date() == null || respuesta.date().isBlank() || respuesta.rates() == null) {
            return Optional.empty();
        }
        BigDecimal tasa = respuesta.rates().get(destino);
        if (tasa == null || tasa.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        LocalDate fecha = parsearFechaSegura(respuesta.date());
        if (fecha == null) {
            return Optional.empty();
        }
        return Optional.of(new TipoCambio(
                base,
                destino,
                fecha,
                tasa.setScale(6, RoundingMode.HALF_UP),
                FUENTE,
                Instant.now(),
                false
        ));
    }

    @Override
    public List<TipoCambio> obtenerHistorico(String base, String destino, LocalDate desde, LocalDate hasta) {
        String rango = "/" + desde + ".." + hasta;
        FrankfurterHistoricoResponse respuesta = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(rango)
                        .queryParam("base", base)
                        .queryParam("symbols", destino)
                        .build())
                .retrieve()
                .body(FrankfurterHistoricoResponse.class);

        if (respuesta == null || respuesta.rates() == null || respuesta.rates().isEmpty()) {
            return List.of();
        }
        Instant ahora = Instant.now();
        List<TipoCambio> resultado = new ArrayList<>();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : respuesta.rates().entrySet()) {
            LocalDate fecha = parsearFechaSegura(entry.getKey());
            Map<String, BigDecimal> monedas = entry.getValue();
            if (fecha == null || monedas == null) {
                continue;
            }
            BigDecimal tasa = monedas.get(destino);
            if (tasa == null || tasa.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            resultado.add(new TipoCambio(
                    base,
                    destino,
                    fecha,
                    tasa.setScale(6, RoundingMode.HALF_UP),
                    FUENTE,
                    ahora,
                    false
            ));
        }
        resultado.sort(Comparator.comparing(TipoCambio::fecha));
        return List.copyOf(resultado);
    }

    private static LocalDate parsearFechaSegura(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    record FrankfurterActualResponse(String base, String date, Map<String, BigDecimal> rates) {
    }

    record FrankfurterHistoricoResponse(String base, Map<String, Map<String, BigDecimal>> rates) {
    }
}
