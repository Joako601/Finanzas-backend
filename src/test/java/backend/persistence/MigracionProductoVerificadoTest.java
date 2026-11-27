package backend.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La migración V3 agrega {@code verificado} con {@code NOT NULL DEFAULT FALSE}; el contexto arranca
 * con {@code ddl-auto=validate}, por lo que la entidad JPA y el esquema coinciden.
 */
@SpringBootTest
@ActiveProfiles("test")
class MigracionProductoVerificadoTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migracionV3AplicadaYColumnaNoNulaConDefaultFalse() {
        Integer aplicada = jdbc.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = '3' AND \"success\" = TRUE",
                Integer.class);
        assertThat(aplicada).isEqualTo(1);

        String nulable = jdbc.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE LOWER(table_name) = 'producto_financiero' AND LOWER(column_name) = 'verificado'",
                String.class);
        assertThat(nulable).isEqualToIgnoringCase("NO");
    }

    @Test
    void catalogoDeEjemploQuedaSinVerificar() {
        Integer verificados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM producto_financiero WHERE verificado = TRUE", Integer.class);
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM producto_financiero", Integer.class);
        assertThat(total).isEqualTo(12);
        assertThat(verificados).isZero();
    }
}
