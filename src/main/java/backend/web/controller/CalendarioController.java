package backend.web.controller;

import backend.domain.service.CalendarioService;
import backend.web.config.OpenApiConfig;
import backend.web.dto.DiaHabilResponse;
import backend.web.dto.DiaInhabilResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/calendario")
@Tag(name = "Calendario bancario MX", description = "Días inhábiles oficiales/CNBV y cálculo de días hábiles bancarios")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class CalendarioController {

    private static final ZoneId ZONA_MX = ZoneId.of("America/Mexico_City");

    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping("/inhabiles")
    @Operation(summary = "Listar los días inhábiles oficiales y bancarios (CNBV) de un año en México")
    public ResponseEntity<List<DiaInhabilResponse>> inhabiles(
            @RequestParam(required = false) @Min(CalendarioService.ANIO_MIN) @Max(CalendarioService.ANIO_MAX) Integer anio) {
        int objetivo = anio != null ? anio : LocalDate.now(ZONA_MX).getYear();
        List<DiaInhabilResponse> respuesta = calendarioService.listarInhabiles(objetivo)
                .stream()
                .map(DiaInhabilResponse::desde)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/dia-habil")
    @Operation(summary = "Evaluar si una fecha es día hábil bancario y obtener el siguiente y anterior día hábil")
    public ResponseEntity<DiaHabilResponse> diaHabil(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(DiaHabilResponse.desde(calendarioService.evaluarFechaHabil(fecha)));
    }
}
