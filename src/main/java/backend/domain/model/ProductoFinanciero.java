package backend.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Producto financiero público de referencia en México.
 * Alineado con el contrato `ProductoFinanciero` del módulo Android `:feature:nube`.
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
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
