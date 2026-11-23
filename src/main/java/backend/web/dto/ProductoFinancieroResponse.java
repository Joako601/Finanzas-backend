package backend.web.dto;

import backend.domain.model.ProductoFinanciero;

import java.math.BigDecimal;

/**
 * Contrato JSON de producto financiero para `:feature:nube` en Android.
 * Conserva los 9 campos base de `ProductoFinanciero` más `tasaAnualReferencia` (opcional/nullable)
 * como referencia informativa para el simulador de intereses.
 */
public record ProductoFinancieroResponse(
        Long id,
        String nombre,
        String categoria,
        String entidad,
        long costoReferenciaPesos,
        String periodicidad,
        BigDecimal tasaAnualReferencia,
        String detalleReferencia,
        String fuenteReferencia,
        String imageUrl
) {
    public static ProductoFinancieroResponse desde(ProductoFinanciero p) {
        return new ProductoFinancieroResponse(
                p.id(),
                p.nombre(),
                p.categoria(),
                p.entidad(),
                p.costoReferenciaPesos(),
                p.periodicidad(),
                p.tasaAnualReferencia(),
                p.detalleReferencia(),
                p.fuenteReferencia(),
                p.imageUrl()
        );
    }
}
