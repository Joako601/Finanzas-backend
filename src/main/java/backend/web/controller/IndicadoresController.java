package backend.web.controller;

import backend.domain.service.IndicadoresService;
import backend.web.config.OpenApiConfig;
import backend.web.dto.IndicadorEconomicoResponse;
import backend.web.dto.SimulacionAhorroResponse;
import backend.web.dto.SimularAhorroRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/indicadores")
@Tag(name = "Indicadores económicos", description = "CETES, UDI, tasa objetivo Banxico y simulador de ahorro")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
public class IndicadoresController {

    private final IndicadoresService indicadoresService;

    public IndicadoresController(IndicadoresService indicadoresService) {
        this.indicadoresService = indicadoresService;
    }

    @GetMapping("/actual")
    @Operation(summary = "Listar los últimos indicadores económicos de México disponibles")
    public ResponseEntity<List<IndicadorEconomicoResponse>> actuales() {
        List<IndicadorEconomicoResponse> respuesta = indicadoresService.listarActuales()
                .stream()
                .map(IndicadorEconomicoResponse::desde)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/simular-ahorro")
    @Operation(summary = "Simular rendimiento compuesto mensual de una meta de ahorro (con CETES o tasa propia)")
    public ResponseEntity<SimulacionAhorroResponse> simularAhorro(@Valid @RequestBody SimularAhorroRequest request) {
        return ResponseEntity.ok(SimulacionAhorroResponse.desde(indicadoresService.simularAhorro(
                request.montoInicial(),
                request.aporteMensual(),
                request.meses(),
                request.tasaAnualPorcentaje())));
    }
}
