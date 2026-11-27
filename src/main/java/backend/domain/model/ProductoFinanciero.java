package backend.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Producto financiero público de referencia en México.
 * Alineado con el contrato `ProductoFinanciero` del módulo Android `:feature:nube`.
 *
 * <p>{@code verificado} indica que un administrador revisó los datos contra la fuente oficial;
 * solo los productos verificados son aptos para comparadores en la app.
 */
public record ProductoFinanciero(
        Long id,
        String nombre,
        String categoria,
        String entidad,
        long costoReferenciaPesos,
        String periodicidad,
        BigDecimal tasaAnualReferencia,
        String detalleReferencia,
        String fuenteReferencia,
        String imageUrl,
        boolean verificado,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
