package backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndRunsFlywayMigrations() {
        Integer totalProductos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM producto_financiero WHERE activo = TRUE",
                Integer.class);
        assertEquals(12, totalProductos);
    }
}
