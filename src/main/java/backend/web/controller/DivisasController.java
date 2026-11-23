package backend.web.controller;

import backend.domain.service.DivisasService;
import backend.web.config.OpenApiConfig;
import backend.web.dto.ConversionDivisaResponse;
import backend.web.dto.TipoCambioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/divisas")
@Tag(name = "Divisas", description = "Tipo de cambio oficial/BCE con caché en PostgreSQL y conversión")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class DivisasController {

    private static final String PATRON_ISO = "^[A-Za-z]{3}$";

    private final DivisasService divisasService;

    public DivisasController(DivisasService divisasService) {
        this.divisasService = divisasService;
    }

    @GetMapping("/actual")
    @Operation(summary = "Consultar tipo de cambio vigente (por defecto USD -> MXN)")
    public ResponseEntity<TipoCambioResponse> actual(
            @RequestParam(defaultValue = "USD") @Pattern(regexp = PATRON_ISO) String base,
            @RequestParam(defaultValue = "MXN") @Pattern(regexp = PATRON_ISO) String destino) {
        return ResponseEntity.ok(TipoCambioResponse.desde(divisasService.obtenerActual(base, destino)));
    }

    @GetMapping("/historico")
    @Operation(summary = "Consultar histórico diario reciente de un par de divisas (1 a 90 días)")
    public ResponseEntity<List<TipoCambioResponse>> historico(
            @RequestParam(defaultValue = "USD") @Pattern(regexp = PATRON_ISO) String base,
            @RequestParam(defaultValue = "MXN") @Pattern(regexp = PATRON_ISO) String destino,
            @RequestParam(defaultValue = "30") @Min(1) @Max(90) int dias) {
        List<TipoCambioResponse> respuesta = divisasService.obtenerHistorico(base, destino, dias)
                .stream()
                .map(TipoCambioResponse::desde)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/convertir")
    @Operation(summary = "Convertir un monto entre dos divisas usando el tipo de cambio más reciente")
    public ResponseEntity<ConversionDivisaResponse> convertir(
            @RequestParam @DecimalMin("0.01") @DecimalMax("100000000.00") @Digits(integer = 10, fraction = 2) BigDecimal monto,
            @RequestParam(defaultValue = "USD") @Pattern(regexp = PATRON_ISO) String de,
            @RequestParam(defaultValue = "MXN") @Pattern(regexp = PATRON_ISO) String a) {
        return ResponseEntity.ok(ConversionDivisaResponse.desde(divisasService.convertir(monto, de, a)));
    }
}
