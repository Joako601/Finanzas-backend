package backend.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración CORS explícita y controlada por propiedad (`finanzas.cors.allowed-origins`).
 * Rechaza comodines abiertos (`*`) para evitar exposiciones accidentales en producción.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${finanzas.cors.allowed-origins:http://localhost:3000,http://localhost:8090}")
            List<String> allowedOrigins) {
        List<String> origenesLimpios = allowedOrigins.stream()
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList();
        if (origenesLimpios.stream().anyMatch("*"::equals)) {
            throw new IllegalStateException("finanzas.cors.allowed-origins no puede contener el comodín '*'.");
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesLimpios);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
