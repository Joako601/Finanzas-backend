package backend.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita tareas programadas solo cuando `finanzas.scheduling.enabled=true`
 * (desactivado en pruebas para evitar llamadas externas automáticas).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "finanzas.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
