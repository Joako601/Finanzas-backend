package backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndRunsFlywayMigrations() {
        Integer totalProductos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM producto_financiero WHERE activo = TRUE",
                Integer.class);
        assertEquals(12, totalProductos);
    }

    @Test
    void perfilDePruebasNoProgramaTareas() {
        assertTrue(context.getBeansOfType(ScheduledTaskHolder.class).isEmpty(),
                "finanzas.scheduling.enabled=false debe evitar tareas programadas en pruebas");
    }
}
