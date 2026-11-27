package backend.web.dto;

import backend.domain.model.ProductoFinanciero;
import backend.web.validation.UrlHttpsSegura;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** DTO de entrada para alta o actualización de un producto del catálogo (solo rol ADMIN). */
public record GuardarProductoRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria.")
        @Size(max = 60, message = "La categoría no puede superar 60 caracteres.")
        String categoria,

        @NotBlank(message = "La entidad es obligatoria.")
        @Size(max = 80, message = "La entidad no puede superar 80 caracteres.")
        String entidad,

        @NotNull(message = "El costo de referencia es obligatorio.")
        @Min(value = 0, message = "El costo de referencia no puede ser negativo.")
        Long costoReferenciaPesos,

        @NotBlank(message = "La periodicidad es obligatoria.")
        @Size(max = 40, message = "La periodicidad no puede superar 40 caracteres.")
        String periodicidad,

        @DecimalMin(value = "0.00", message = "La tasa anual no puede ser negativa.")
        @DecimalMax(value = "500.00", message = "La tasa anual excede el rango permitido.")
        @Digits(integer = 4, fraction = 2, message = "La tasa anual admite hasta 4 enteros y 2 decimales.")
        BigDecimal tasaAnualReferencia,

        @NotBlank(message = "El detalle de referencia es obligatorio.")
        @Size(max = 280, message = "El detalle no puede superar 280 caracteres.")
        String detalleReferencia,

        @NotBlank(message = "La fuente de referencia es obligatoria.")
        @Size(max = 160, message = "La fuente no puede superar 160 caracteres.")
        String fuenteReferencia,

        @Size(max = 500, message = "La URL de imagen no puede superar 500 caracteres.")
        @UrlHttpsSegura
        String imageUrl,

        @Schema(description = "Marca el producto como revisado contra la fuente oficial. En el alta, "
                + "si se omite queda en false. En la edición, si se omite se conserva el valor actual.",
                nullable = true, example = "true")
        Boolean verificado
) {
    public ProductoFinanciero aDominio() {
        return new ProductoFinanciero(
                null,
                nombre,
                categoria,
                entidad,
                costoReferenciaPesos,
                periodicidad,
                tasaAnualReferencia,
                detalleReferencia,
                fuenteReferencia,
                imageUrl,
                Boolean.TRUE.equals(verificado),
                true,
                null,
                null
        );
    }
}
