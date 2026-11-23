package backend.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/** Endpoint público de salud del servicio para monitoreo, Docker y despliegue en la nube. */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Salud", description = "Estado operativo del backend")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verificar estado del servicio", description = "Endpoint público sin autenticación.")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("OK", "backend", Instant.now()));
    }

    public record HealthResponse(String estado, String servicio, Instant timestamp) {
    }
}
