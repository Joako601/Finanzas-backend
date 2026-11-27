package backend.web.dto;

import backend.domain.model.ProductoFinanciero;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Contrato JSON de producto financiero para `:feature:nube` en Android.
 * Conserva los 9 campos base de `ProductoFinanciero` más `tasaAnualReferencia` (opcional/nullable)
 * como referencia informativa para el simulador de intereses y {@code verificado}, que indica si
 * un administrador revisó los datos contra la fuente oficial (campo agregado al final; los demás
 * conservan nombre, tipo y significado).
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
        String imageUrl,
        @Schema(description = "Datos revisados contra la fuente oficial. Solo los productos verificados "
                + "son aptos para comparadores.", example = "false")
        boolean verificado
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
                p.imageUrl(),
                p.verificado()
        );
    }
}
