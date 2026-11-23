package backend.external;

import backend.domain.gateway.BanxicoGateway;
import backend.domain.model.IndicadorEconomico;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador HTTP hacia la API oficial de Banxico SIE (Sistema de Información Económica).
 * Clona su propio `RestClient.Builder` para aislar la cabecera `Bmx-Token` de otros clientes HTTP.
 */
@Component
public class BanxicoSieClient implements BanxicoGateway {

    private static final String FUENTE = "Banxico SIE";
    private static final DateTimeFormatter FORMATO_FECHA_MX = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;
    private final String token;

    public BanxicoSieClient(
            RestClient.Builder restClientBuilder,
            @Value("${finanzas.external.banxico.base-url:https://www.banxico.org.mx/SieAPIRest/service/v1}") String baseUrl,
            @Value("${finanzas.external.banxico.token:}") String token) {
        this.token = token == null ? "" : token.trim();
        RestClient.Builder builder = restClientBuilder.clone().baseUrl(baseUrl);
        if (!this.token.isEmpty()) {
            builder = builder.defaultHeader("Bmx-Token", this.token);
        }
        this.restClient = builder.build();
    }

    @Override
    public boolean estaConfigurado() {
        return !token.isEmpty();
    }

    @Override
    public Optional<IndicadorEconomico> obtenerSerieOportuna(
            String clave,
            String nombre,
            String unidad,
            String serieId) {
        if (!estaConfigurado()) {
            return Optional.empty();
        }
        BanxicoResponse respuesta = restClient.get()
                .uri("/series/{serie}/datos/oportuno", serieId)
                .retrieve()
                .body(BanxicoResponse.class);

        if (respuesta == null || respuesta.bmx() == null || respuesta.bmx().series() == null
                || respuesta.bmx().series().isEmpty()) {
            return Optional.empty();
        }
        BanxicoSerie serie = respuesta.bmx().series().get(0);
        if (serie == null || serie.datos() == null || serie.datos().isEmpty()) {
            return Optional.empty();
        }

        // Busca en reversa el dato más reciente válido (omitiendo "N/E" o filas malformadas).
        for (int i = serie.datos().size() - 1; i >= 0; i--) {
            BanxicoDato item = serie.datos().get(i);
            if (item == null || item.dato() == null || item.dato().isBlank()
                    || "N/E".equalsIgnoreCase(item.dato().trim())
                    || item.fecha() == null || item.fecha().isBlank()) {
                continue;
            }
            try {
                BigDecimal valor = new BigDecimal(item.dato().trim().replace(",", ""))
                        .setScale(6, RoundingMode.HALF_UP);
                LocalDate fecha = LocalDate.parse(item.fecha().trim(), FORMATO_FECHA_MX);
                return Optional.of(new IndicadorEconomico(
                        clave,
                        nombre,
                        valor,
                        unidad,
                        fecha,
                        FUENTE,
                        serieId,
                        Instant.now(),
                        false
                ));
            } catch (NumberFormatException | DateTimeParseException ignored) {
                // Intenta con la observación previa si la más reciente vino corrupta.
            }
        }
        return Optional.empty();
    }

    record BanxicoResponse(BanxicoBmx bmx) {
    }

    record BanxicoBmx(List<BanxicoSerie> series) {
    }

    record BanxicoSerie(String idSerie, String titulo, List<BanxicoDato> datos) {
    }

    record BanxicoDato(String fecha, String dato) {
    }
}
