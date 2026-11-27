package backend.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** La migración V4 deja los 12 productos de ejemplo con su imagen pública de Supabase Storage. */
@SpringBootTest
@ActiveProfiles("test")
class MigracionImagenesCatalogoTest {

    private static final String BUCKET =
            "https://gqajsktcgxmavmgnthot.supabase.co/storage/v1/object/public/productos/";

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void todosLosProductosDeEjemploTienenImagenDelBucket() {
        Integer sinImagen = jdbc.queryForObject(
                "SELECT COUNT(*) FROM producto_financiero WHERE image_url IS NULL", Integer.class);
        Integer delBucket = jdbc.queryForObject(
                "SELECT COUNT(*) FROM producto_financiero WHERE image_url LIKE ? AND image_url LIKE '%.png'",
                Integer.class, BUCKET + "%");
        assertThat(sinImagen).isZero();
        assertThat(delBucket).isEqualTo(12);
    }
}
